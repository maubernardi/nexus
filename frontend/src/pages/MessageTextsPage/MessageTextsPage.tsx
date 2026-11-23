import { useTranslation } from 'react-i18next';

import { PageHeading } from '@/atoms/PageHeading/PageHeading';
import { Button } from '@/components/ui/button';
import type { MessageText } from '@/config/api/messageTextApi';
import { useMessageTexts } from '@/config/hooks/useMessageTexts';
import { MessageTextPartForm } from '@/organisms/MessageTextPartForm/MessageTextPartForm';

/**
 * Testi dei messaggi (US-1204, PO 22/11): per ogni notifica il testo nell'app, la parte distintiva dell'oggetto e il
 * testo dell'email, distinti; per le email del cambio di indirizzo oggetto e testo. Solo l'ADMIN.
 */
export const MessageTextsPage = () => {
  const { t } = useTranslation();
  const texts = useMessageTexts();

  if (texts.isError && !texts.data) {
    return (
      <div className="space-y-4">
        <PageHeading>{t('pages.messageTexts.heading')}</PageHeading>
        <div role="alert" className="space-y-3 rounded-md border-2 border-destructive bg-card p-4">
          <p className="font-medium text-destructive">{t('common.loadError')}</p>
          <Button variant="outline" onClick={() => void texts.refetch()}>
            {t('common.retry')}
          </Button>
        </div>
      </div>
    );
  }

  const groups: { id: 'notifications' | 'emailChange'; items: MessageText[] }[] = [
    { id: 'notifications', items: (texts.data ?? []).filter((m) => m.notification) },
    { id: 'emailChange', items: (texts.data ?? []).filter((m) => !m.notification) },
  ];

  return (
    <div className="space-y-8">
      <PageHeading>{t('pages.messageTexts.heading')}</PageHeading>
      <p className="text-muted-foreground">{t('pages.messageTexts.intro')}</p>
      {!texts.data ? (
        <p role="status">{t('messageTexts.loading')}</p>
      ) : (
        groups.map((group) => (
          <section key={group.id} aria-labelledby={`message-group-${group.id}`} className="space-y-3">
            <h2 id={`message-group-${group.id}`} className="text-xl font-semibold text-primary">
              {t(`messageTexts.groups.${group.id}`)}
            </h2>
            {group.items.map((message) => (
              <details key={message.key} className="rounded-xl border border-border bg-card p-4">
                <summary className="cursor-pointer">
                  <span className="font-semibold">{t(`messageTexts.messages.${message.key}.title`)}</span>
                  {message.parts.some((p) => p.custom) && (
                    <span className="ml-2 text-sm font-medium text-primary">{t('messageTexts.changed')}</span>
                  )}
                  <span className="block text-sm text-muted-foreground">
                    {t(`messageTexts.messages.${message.key}.when`)}
                  </span>
                </summary>
                {/* i campi del messaggio sono un gruppo: le etichette si ripetono da un messaggio all'altro */}
                <fieldset className="mt-4 space-y-6">
                  <legend className="sr-only">{t(`messageTexts.messages.${message.key}.title`)}</legend>
                  {message.parts.map((part) => (
                    <MessageTextPartForm key={part.part} message={message} part={part} />
                  ))}
                </fieldset>
              </details>
            ))}
          </section>
        ))
      )}
    </div>
  );
};
