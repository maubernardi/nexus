import { useRef } from 'react';
import { Plus, Trash2 } from 'lucide-react';
import { useFieldArray, type Control, type FieldErrors, type UseFormRegister } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { NativeSelect } from '@/components/ui/native-select';
import type { ReferenceItem } from '@/config/api/referenceApi';
import { FormField } from '@/molecules/FormField/FormField';
import {
  EMAIL_PATTERN,
  MAX_CONTACTS,
  PHONE_PATTERN,
  type CompanyFormValues,
  type ContactPart,
} from '@/organisms/CompanyForm/companyFormValues';

type ContactsFieldsProps = {
  control: Control<CompanyFormValues>;
  register: UseFormRegister<CompanyFormValues>;
  errors: FieldErrors<CompanyFormValues>;
  /** Ruoli attivi più quelli che i referenti dell'azienda hanno già. */
  roles: ReferenceItem[];
  /** Catalogo dei ruoli caricato: prima i menu sono disattivati. */
  rolesLoaded: boolean;
  fieldId: (path: string) => string;
};

/**
 * Referenti dell'azienda (US-510): un gruppo per referente, con nome, ruolo e recapiti; serve il nome o il ruolo.
 * Aggiungendo un referente il focus va al suo nome, togliendolo va su «Aggiungi referente».
 */
export const ContactsFields = ({
  control,
  register,
  errors,
  roles,
  rolesLoaded,
  fieldId,
}: ContactsFieldsProps) => {
  const { t } = useTranslation();
  const rows = useFieldArray({ control, name: 'contacts' });
  const addRef = useRef<HTMLButtonElement>(null);
  const max = (n: number) => ({ value: n, message: t('form.errors.maxLength', { max: n }) });
  const label = (part: ContactPart, n: number): string => t(`company.contacts.${part}`, { n });
  const listError = errors.contacts?.message ?? errors.contacts?.root?.message;

  const add = (): void => {
    rows.append(
      { name: '', roleId: '', phone: '', mobile: '', email: '' },
      { focusName: `contacts.${rows.fields.length}.name` },
    );
  };

  const remove = (k: number): void => {
    rows.remove(k);
    // il gruppo tolto non c'è più: il focus va su un punto stabile, invece di perdersi nella pagina
    addRef.current?.focus();
  };

  return (
    <fieldset
      id={fieldId('contacts')}
      tabIndex={-1}
      aria-describedby={listError ? `${fieldId('contacts')}-error` : undefined}
      className="space-y-4"
    >
      <legend className="text-base font-semibold">{t('company.contacts.heading')}</legend>
      <p className="text-sm text-muted-foreground">{t('company.contacts.intro')}</p>
      {listError && (
        <p id={`${fieldId('contacts')}-error`} className="text-sm font-medium text-destructive">
          {listError}
        </p>
      )}
      {rows.fields.length === 0 && <p>{t('company.contacts.none')}</p>}
      {rows.fields.map((row, k) => {
        const n = k + 1;
        const rowErrors = errors.contacts?.[k];
        const roleField = `contacts.${k}.roleId` as const;
        return (
          <fieldset key={row.id} className="space-y-4 rounded-md border border-border p-4">
            <legend className="px-1 font-semibold">{t('company.contacts.group', { n })}</legend>
            <div className="grid gap-4 md:grid-cols-2">
              <FormField
                id={fieldId(`contacts.${k}.name`)}
                label={label('name', n)}
                error={rowErrors?.name?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    autoComplete="off"
                    {...register(`contacts.${k}.name` as const, {
                      maxLength: max(200),
                      validate: (name, values) =>
                        name.trim() !== '' ||
                        values.contacts[k]?.roleId !== '' ||
                        t('company.errors.contactNameOrRole'),
                    })}
                  />
                )}
              </FormField>
              <FormField
                id={fieldId(roleField)}
                label={label('roleId', n)}
                error={rowErrors?.roleId?.message}
              >
                {(p) => (
                  <NativeSelect
                    {...p}
                    disabled={!rolesLoaded}
                    {...register(roleField, { deps: [`contacts.${k}.name`] })}
                  >
                    <option value="">{t('company.contacts.noRole')}</option>
                    {roles.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.name}
                      </option>
                    ))}
                  </NativeSelect>
                )}
              </FormField>
              <FormField
                id={fieldId(`contacts.${k}.phone`)}
                label={label('phone', n)}
                error={rowErrors?.phone?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    type="tel"
                    autoComplete="off"
                    {...register(`contacts.${k}.phone` as const, {
                      maxLength: max(30),
                      pattern: { value: PHONE_PATTERN, message: t('company.errors.phone') },
                    })}
                  />
                )}
              </FormField>
              <FormField
                id={fieldId(`contacts.${k}.mobile`)}
                label={label('mobile', n)}
                error={rowErrors?.mobile?.message}
              >
                {(p) => (
                  <Input
                    {...p}
                    type="tel"
                    autoComplete="off"
                    {...register(`contacts.${k}.mobile` as const, {
                      maxLength: max(30),
                      pattern: { value: PHONE_PATTERN, message: t('company.errors.phone') },
                    })}
                  />
                )}
              </FormField>
              <FormField
                id={fieldId(`contacts.${k}.email`)}
                label={label('email', n)}
                error={rowErrors?.email?.message}
                className="md:col-span-2"
              >
                {(p) => (
                  <Input
                    {...p}
                    type="email"
                    autoComplete="off"
                    {...register(`contacts.${k}.email` as const, {
                      maxLength: max(254),
                      pattern: { value: EMAIL_PATTERN, message: t('company.errors.email') },
                    })}
                  />
                )}
              </FormField>
            </div>
            <Button
              type="button"
              variant="outline"
              className="h-auto min-h-9 py-2 whitespace-normal"
              onClick={() => remove(k)}
            >
              <Trash2 aria-hidden="true" />
              {t('company.contacts.remove', { n })}
            </Button>
          </fieldset>
        );
      })}
      <div className="space-y-1">
        <Button
          ref={addRef}
          type="button"
          variant="outline"
          disabled={rows.fields.length >= MAX_CONTACTS}
          aria-describedby={rows.fields.length >= MAX_CONTACTS ? `${fieldId('contacts')}-max` : undefined}
          onClick={add}
        >
          <Plus aria-hidden="true" />
          {t('company.contacts.add')}
        </Button>
        {rows.fields.length >= MAX_CONTACTS && (
          <p id={`${fieldId('contacts')}-max`} className="text-sm text-muted-foreground">
            {t('company.contacts.max', { max: MAX_CONTACTS })}
          </p>
        )}
      </div>
    </fieldset>
  );
};
