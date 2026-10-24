#!/usr/bin/env bash
# Controllo delle migrazioni Flyway di una PR rispetto al branch di destinazione (EN-5, decisione del PO del 24/10).
# Fallisce se la PR:
#   - aggiunge una migrazione V<n> con n non superiore alla più alta già sul branch di destinazione;
#   - contiene due migrazioni con lo stesso numero;
#   - modifica, rinomina o cancella una migrazione già presente sul branch di destinazione (già applicata altrove).
# Uso: check-migrations.sh <ref-di-destinazione>   (es. origin/main)
set -euo pipefail

base="${1:?indicare il ref di destinazione, es. origin/main}"
dir="backend/src/main/resources/db/migration"

versions() { sed -n 's/^V\([0-9][0-9]*\)__.*\.sql$/\1/p'; }

base_files=$(git ls-tree --name-only "$base" "$dir/" | xargs -r -n1 basename | grep -E '^V[0-9]+__.*\.sql$' | sort || true)
head_files=$(find "$dir" -maxdepth 1 -name 'V*__*.sql' -printf '%f\n' | sort)
max=$(printf '%s\n' "$base_files" | versions | sort -n | tail -n1)
max=${max:-0}
failed=0

for n in $(printf '%s\n' "$head_files" | versions | sort -n | uniq -d); do
  echo "::error::Due migrazioni hanno lo stesso numero V$n: rinumerane una."
  failed=1
done

# migrazioni nuove = file assenti sul branch di destinazione (confronto per nome: un branch rimasto indietro può avere
# una V<n> diversa da quella con lo stesso numero già unita)
new_files=$(comm -13 <(printf '%s\n' "$base_files") <(printf '%s\n' "$head_files"))
for file in $new_files; do
  n=$(printf '%s\n' "$file" | versions)
  if [ "$n" -le "$max" ]; then
    echo "::error file=$dir/$file::La migrazione V$n non è successiva all'ultima già sul branch di destinazione (V$max): rinominala in V$((max + 1))__…"
    failed=1
  fi
done

changed=$(git diff --name-only --diff-filter=MDR "$base"...HEAD -- "$dir/")
for file in $changed; do
  echo "::error file=$file::Le migrazioni già presenti sul branch di destinazione non si modificano: crea una nuova migrazione."
  failed=1
done

if [ "$failed" -eq 0 ]; then
  added=$(printf '%s\n' $new_files | versions | sort -n | sed 's/^/V/' | paste -sd' ' -)
  echo "Migrazioni ok (ultima sul branch di destinazione: V$max; nuove nella PR: ${added:-nessuna})."
fi
exit "$failed"
