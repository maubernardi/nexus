import { useEffect, useRef, useState } from 'react';
import { Plus } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { CatalogItem, CatalogKind } from '@/config/api/catalogApi';
import { apiError } from '@/config/api/errors';
import {
  useCatalog,
  useCreateCatalogItem,
  useRenameCatalogItem,
  useSetCatalogItemActive,
} from '@/config/hooks/useCatalogs';
import { useErrorSummaryFocus } from '@/hooks/useErrorSummaryFocus';
import { ErrorSummary } from '@/molecules/ErrorSummary/ErrorSummary';
import { FormField } from '@/molecules/FormField/FormField';

type Outcome = { text: string; error?: boolean };

type CatalogSectionProps = {
  kind: CatalogKind;
};

/**
 * Un catalogo per l'ADMIN (US-106): elenco con stato in testo, aggiunta, nuovo nome, disattivazione e riattivazione.
 * Le voci non si cancellano: disattivate non sono più selezionabili ma restano sui dati esistenti.
 */
export const CatalogSection = ({ kind }: CatalogSectionProps) => {
  const { t } = useTranslation();
  const items = useCatalog(kind);
  const setActive = useSetCatalogItemActive();
  const [editing, setEditing] = useState<CatalogItem | 'new' | null>(null);
  const [outcome, setOutcome] = useState<Outcome | null>(null);
  const formHeadingRef = useRef<HTMLHeadingElement>(null);
  const outcomeRef = useRef<HTMLDivElement>(null);
  const headingId = `catalog-${kind}-heading`;
  const captionId = `catalog-${kind}-caption`;

  // all'apertura del modulo il focus va al suo titolo; dopo un'azione, all'esito
  useEffect(() => {
    if (editing) formHeadingRef.current?.focus();
  }, [editing]);
  useEffect(() => {
    if (outcome) outcomeRef.current?.focus();
  }, [outcome]);

  const open = (target: CatalogItem | 'new'): void => {
    setOutcome(null);
    setEditing(target);
  };
  const close = (result: Outcome): void => {
    setEditing(null);
    setOutcome(result);
  };

  const toggle = (item: CatalogItem): void => {
    setOutcome(null);
    setActive.mutate(
      { kind, id: item.id, active: !item.active },
      {
        onSuccess: (i) =>
          setOutcome({
            text: t(i.active ? 'catalog.outcome.activated' : 'catalog.outcome.deactivated', { name: i.name }),
          }),
        onError: (error) => {
          const body = apiError(error);
          setOutcome({
            text:
              body?.status === 409
                ? t('catalog.outcome.refused', { reason: body.message })
                : t('form.errors.generic'),
            error: true,
          });
        },
      },
    );
  };

  const th = 'px-3 py-2 text-left text-sm font-semibold whitespace-nowrap';
  const td = 'px-3 py-2 align-top';

  return (
    <section aria-labelledby={headingId} className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h2 id={headingId} className="text-lg font-semibold text-primary">
          {t(`catalog.${kind}.heading`)}
        </h2>
        {editing === null && (
          <Button size="lg" onClick={() => open('new')}>
            <Plus aria-hidden="true" />
            {t(`catalog.${kind}.add`)}
          </Button>
        )}
      </div>
      <p className="text-muted-foreground">{t(`catalog.${kind}.intro`)}</p>

      {outcome && (
        <div
          ref={outcomeRef}
          tabIndex={-1}
          className={
            outcome.error
              ? 'rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive'
              : 'rounded-md border-l-4 border-primary bg-card p-4 font-medium'
          }
        >
          {outcome.text}
        </div>
      )}

      {editing && (
        <div className="space-y-4 rounded-xl border border-border bg-card p-4">
          <h3 ref={formHeadingRef} tabIndex={-1} className="text-base font-semibold">
            {editing === 'new'
              ? t(`catalog.${kind}.newHeading`)
              : t('catalog.renameHeading', { name: editing.name })}
          </h3>
          <CatalogItemForm
            key={editing === 'new' ? 'new' : editing.id}
            kind={kind}
            item={editing === 'new' ? undefined : editing}
            onDone={(item, created) =>
              close({
                text: t(created ? 'catalog.outcome.created' : 'catalog.outcome.renamed', {
                  name: item.name,
                  code: item.code,
                }),
              })
            }
            onCancel={() => setEditing(null)}
          />
        </div>
      )}

      {items.isError && !items.data ? (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('catalog.loadError')}
        </p>
      ) : !items.data ? (
        <p>{t('common.loading')}</p>
      ) : (
        <div
          role="region"
          aria-labelledby={captionId}
          // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex -- regione scorrevole: deve essere raggiungibile da tastiera
          tabIndex={0}
          className="relative max-h-[28rem] overflow-auto rounded-xl border border-border bg-card focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
        >
          <table className="w-full border-collapse text-sm max-lg:w-max max-lg:whitespace-nowrap">
            <caption id={captionId} className="sr-only">
              {t(`catalog.${kind}.caption`, { count: items.data.length })}
            </caption>
            <thead className="sticky top-0 border-b border-border bg-muted">
              <tr>
                <th scope="col" className={th}>
                  {t('catalog.columns.name')}
                </th>
                <th scope="col" className={th}>
                  {t('catalog.columns.code')}
                </th>
                <th scope="col" className={th}>
                  {t('catalog.columns.status')}
                </th>
                <th scope="col" className={th}>
                  {t('catalog.columns.actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {items.data.map((item) => (
                <tr key={item.id} className="border-b border-border last:border-b-0">
                  <th scope="row" className={`${td} text-left font-semibold`}>
                    {item.name}
                  </th>
                  <td className={`${td} font-mono text-xs`}>{item.code}</td>
                  <td className={td}>{t(item.active ? 'catalog.active' : 'catalog.inactive')}</td>
                  <td className={`${td} whitespace-nowrap`}>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => open(item)}
                        disabled={editing !== null}
                      >
                        {t('catalog.rename')}
                        {/* testo nascosto dopo uno spazio a sé: nome accessibile "Rinomina Firenze Nord" */}{' '}
                        <span className="sr-only">{item.name}</span>
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => toggle(item)}
                        disabled={setActive.isPending}
                      >
                        {t(item.active ? 'catalog.deactivate' : 'catalog.activate')}{' '}
                        <span className="sr-only">{item.name}</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

type CatalogItemFormProps = {
  kind: CatalogKind;
  item?: CatalogItem;
  onDone: (item: CatalogItem, created: boolean) => void;
  onCancel: () => void;
};

/** Nome della voce: alla creazione il codice si ricava dal nome, poi non cambia. */
const CatalogItemForm = ({ kind, item, onDone, onCancel }: CatalogItemFormProps) => {
  const { t } = useTranslation();
  const create = useCreateCatalogItem();
  const rename = useRenameCatalogItem();
  const [genericError, setGenericError] = useState(false);
  const fieldId = `catalog-${kind}-name`;
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting, submitCount },
  } = useForm<{ name: string }>({
    defaultValues: { name: item?.name ?? '' },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
    shouldFocusError: false,
  });
  const { summaryRef, requestSummaryFocus } = useErrorSummaryFocus(submitCount, errors);
  const label = t('catalog.columns.name');

  const onValid = async ({ name }: { name: string }): Promise<void> => {
    setGenericError(false);
    try {
      const saved = item
        ? await rename.mutateAsync({ kind, id: item.id, name: name.trim() })
        : await create.mutateAsync({ kind, name: name.trim() });
      onDone(saved, !item);
    } catch (error) {
      const message = apiError(error)?.fieldErrors?.find((fe) => fe.field === 'name')?.message;
      if (message) {
        setError('name', { type: 'server', message });
      } else {
        setGenericError(true);
      }
      requestSummaryFocus();
    }
  };

  return (
    <form noValidate onSubmit={handleSubmit(onValid, requestSummaryFocus)} className="space-y-4">
      {submitCount > 0 && (
        <ErrorSummary
          ref={summaryRef}
          errors={errors.name?.message ? [{ fieldId, message: `${label}: ${errors.name.message}` }] : []}
        />
      )}
      {genericError && (
        <p
          role="alert"
          className="rounded-md border-2 border-destructive bg-card p-4 font-medium text-destructive"
        >
          {t('form.errors.generic')}
        </p>
      )}
      <FormField
        id={fieldId}
        label={label}
        hint={item ? t('catalog.renameHint') : t('catalog.nameHint')}
        required
        error={errors.name?.message}
        className="max-w-md"
      >
        {(p) => (
          <Input
            {...p}
            autoComplete="off"
            {...register('name', {
              maxLength: { value: 200, message: t('form.errors.maxLength', { max: 200 }) },
              validate: (v) => v.trim() !== '' || t('form.errors.required'),
            })}
          />
        )}
      </FormField>
      <div className="flex flex-wrap gap-3">
        <Button type="submit" size="lg" disabled={isSubmitting}>
          {t(item ? 'catalog.save' : 'catalog.create')}
        </Button>
        <Button type="button" size="lg" variant="outline" onClick={onCancel}>
          {t('catalog.cancel')}
        </Button>
      </div>
    </form>
  );
};
