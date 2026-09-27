#!/usr/bin/env bash
# Regole inviolabili del progetto (vedi CLAUDE.md):
#  1. tutti i merge li fa l'utente -> qualsiasi merge viene bloccato;
#  2. push su main solo con permesso esplicito -> ogni push che può toccare main chiede conferma.
set -uo pipefail

input=$(cat)
cmd=$(jq -r '.tool_input.command // ""' <<<"$input")
cwd=$(jq -r '.cwd // "."' <<<"$input")

decide() {
  jq -n --arg d "$1" --arg r "$2" \
    '{hookSpecificOutput: {hookEventName: "PreToolUse", permissionDecision: $d, permissionDecisionReason: $r}}'
  exit 0
}

sep='(^|[;&|(`[:space:]])'

# 1. Merge: git merge, gh pr merge, merge di PR via API
if grep -Eq "${sep}git([[:space:]]+-C[[:space:]]+[^[:space:]]+)?[[:space:]]+merge([[:space:]]|$)" <<<"$cmd" \
  || grep -Eq "${sep}gh[[:space:]]+pr[[:space:]]+merge([[:space:]]|$)" <<<"$cmd" \
  || grep -Eq "${sep}gh[[:space:]]+api[[:space:]].*pulls/[0-9]+/merge" <<<"$cmd"; then
  decide deny "Regola inviolabile NEXUS: tutti i merge li esegue l'utente. Chiedigli di fare il merge."
fi

# 2. Push che può toccare main
if grep -Eq "${sep}git([[:space:]]+-C[[:space:]]+[^[:space:]]+)?[[:space:]]+push([[:space:]]|$)" <<<"$cmd"; then
  branch=$(git -C "$cwd" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
  if grep -Eq '(^|[[:space:]:+])(main|master)([[:space:]]|$)|--all|--mirror|refs/heads/(main|master)' <<<"$cmd" \
    || [[ "$branch" == "main" || "$branch" == "master" ]]; then
    decide ask "Regola inviolabile NEXUS: push su main solo con permesso esplicito dell'utente (branch corrente: ${branch:-?})."
  fi
fi

exit 0
