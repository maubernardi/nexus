import { useEffect, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useTranslation } from 'react-i18next';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { apiError } from '@/config/api/errors';
import type { MessagePart, MessageText, MessageTextPart } from '@/config/api/messageTextApi';
import { useResetMessageText, useUpdateMessageText } from '@/config/hooks/useMessageTexts';
import { FormField } from '@/molecules/FormField/FormField';

/** Segnaposto noti all'interfaccia, con il valore d'esempio dell'anteprima. */
const EXAMPLES = { numero: '12', codice: '123456', ora: '11:30' } as const;
type Placeholder = keyof typeof EXAMPLES;
const isKnown = (name: string): name is Placeholder => name in EXAMPLES;

const fill = (text: string): string =>
  text.replace(/\{([^{}]*)\}/g, (match, name: string) => (isKnown(name) ? EXAMPLES[name] : match));

type MessageTextPartFormProps = {
  message: MessageText;
  part: MessageTextPart;
};

type Values = { text: string };

/**
 * Una parte di un messaggio (US-1204): il testo da cambiare, con l'anteprima composta come la manda NEXUS (parti fisse
 * comprese), il salvataggio e il ritorno al testo predefinito.
 */
export const MessageTextPartForm = ({ message, part }: MessageTextPartFormProps) => {
  const { t } = useTranslation();
  const update = useUpdateMessageText();
  const reset = useResetMessageText();
  const [outcome, setOutcome] = useState<string | null>(null);
  const id = `message-${message.key}-${part.part}`;
  const title = t(`messageTexts.messages.${message.key}.title`);
  const partLabel = t(`messageTexts.parts.${part.part}`);
  const {
    register,
    handleSubmit,
    setError,
    reset: resetForm,
    control,
    formState: { errors, isSubmitting },
  } = useForm<Values>({ defaultValues: { text: part.text } });
  const text = useWatch({ control, name: 'text' });

  // dopo un salvataggio o un ripristino il campo mostra il testo in uso
  useEffect(() => {
    resetForm({ text: part.text });
  }, [part.text, resetForm]);

  const placeholders =
    message.placeholders.length > 0
      ? t('messageTexts.placeholders', {
          list: message.placeholders
            .filter(isKnown)
            .map((p) => `{${p}} ${t(`messageTexts.placeholderNames.${p}`)}`)
            .join(', '),
        })
      : t('messageTexts.noPlaceholders');
  const hint = `${t(`messageTexts.hints.${part.part}`)} ${placeholders}`;

  const preview = (kind: MessagePart, value: string): string => {
    const filled = fill(value);
    if (kind === 'EMAIL_SUBJECT') {
      return message.notification
        ? t('messageTexts.previewSubjectNotification', { number: EXAMPLES.numero, text: filled })
        : t('messageTexts.previewSubject', { text: filled });
    }
    if (kind === 'EMAIL_BODY') {
      return `${filled}\n\n${t(message.notification ? 'messageTexts.footerNotification' : 'messageTexts.footerEmailChange')}`;
    }
    return filled;
  };

  const onValid = async (values: Values): Promise<void> => {
    setOutcome(null);
    try {
      await update.mutateAsync({ key: message.key, part: part.part, text: values.text });
      setOutcome(t('messageTexts.saved'));
    } catch (error) {
      const fieldError = apiError(error)?.fieldErrors?.[0];
      if (fieldError) {
        setError('text', { type: 'server', message: fieldError.message });
      } else {
        setOutcome(t('form.errors.generic'));
      }
    }
  };

  const onReset = async (): Promise<void> => {
    setOutcome(null);
    try {
      await reset.mutateAsync({ key: message.key, part: part.part });
      setOutcome(t('messageTexts.resetDone'));
    } catch {
      setOutcome(t('form.errors.generic'));
    }
  };

  const field = register('text', { required: t('messageTexts.required') });

  return (
    <form noValidate onSubmit={handleSubmit(onValid)} className="space-y-2">
      <FormField id={id} label={partLabel} hint={hint} error={errors.text?.message}>
        {(p) =>
          part.part === 'EMAIL_SUBJECT' ? (
            <Input {...p} {...field} />
          ) : (
            <Textarea {...p} {...field} rows={part.part === 'EMAIL_BODY' ? 8 : 3} />
          )
        }
      </FormField>
      <div className="rounded-md bg-muted p-3 text-sm">
        <p className="font-medium">{t('messageTexts.preview')}</p>
        <p className="whitespace-pre-wrap">{preview(part.part, text ?? '')}</p>
      </div>
      <div className="flex flex-wrap items-center gap-2">
        <Button
          type="submit"
          disabled={isSubmitting}
          aria-label={t('messageTexts.saveLabel', { part: partLabel, title })}
        >
          {t('messageTexts.save')}
        </Button>
        {part.custom && (
          <Button
            type="button"
            variant="outline"
            disabled={reset.isPending}
            onClick={() => void onReset()}
            aria-label={t('messageTexts.resetLabel', { part: partLabel, title })}
          >
            {t('messageTexts.reset')}
          </Button>
        )}
        <p role="status" className="text-sm font-medium">
          {outcome}
        </p>
      </div>
    </form>
  );
};
