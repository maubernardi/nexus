import { useState, type FormEvent } from 'react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { apiError } from '@/config/api/errors';
import { CheckboxGroup } from '@/molecules/CheckboxGroup/CheckboxGroup';

type ChecklistFormProps = {
  legend: string;
  hint?: string;
  options: readonly { value: string; label: string }[];
  initial: readonly string[];
  /** Salva la scelta; un errore del server diventa un messaggio sopra le caselle. */
  onSave: (ids: string[]) => Promise<void>;
  onCancel: () => void;
};

/** Scelta di più voci di un catalogo (US-115: progetti di una sede, tipi ammessi da un progetto). */
export const ChecklistForm = ({ legend, hint, options, initial, onSave, onCancel }: ChecklistFormProps) => {
  const { t } = useTranslation();
  const [value, setValue] = useState<string[]>([...initial]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const submit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      await onSave(value);
    } catch (e) {
      const body = apiError(e);
      setError(body?.fieldErrors?.[0]?.message ?? body?.message ?? t('form.errors.generic'));
    } finally {
      setSaving(false);
    }
  };

  return (
    <form noValidate onSubmit={(e) => void submit(e)} className="space-y-4">
      {error && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {error}
        </p>
      )}
      <CheckboxGroup
        legend={legend}
        hint={hint}
        options={options}
        value={value}
        onChange={setValue}
        gridClassName="grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
      />
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={saving}>
          {t('catalog.save')}
        </Button>
        <Button type="button" size="lg" variant="outline" onClick={onCancel}>
          {t('catalog.cancel')}
        </Button>
      </div>
    </form>
  );
};
