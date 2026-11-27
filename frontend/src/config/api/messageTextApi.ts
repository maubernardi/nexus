import { axiosInstance } from '@/config/api/axiosInstance';
import { ENDPOINTS } from '@/config/api/Endpoint';

/** Parti di un messaggio (US-1204): testo nell'app, parte distintiva dell'oggetto, testo dell'email. */
export type MessagePart = 'APP' | 'EMAIL_SUBJECT' | 'EMAIL_BODY';

export type MessageTextPart = {
  part: MessagePart;
  text: string;
  defaultText: string;
  /** Cambiato dall'ADMIN: si può ripristinare il predefinito. */
  custom: boolean;
};

/** Messaggi con i testi modificabili, nell'ordine della pagina (US-1204). */
export const MESSAGE_KEYS = [
  'CATEGORY_CHANGED',
  'CATEGORY_APPROVED',
  'PROPOSAL_SENT',
  'PROPOSAL_ACCEPTED',
  'APPOINTMENT_SET',
  'APPOINTMENT_FAILED',
  'PROPOSAL_REJECTED',
  'TIMER_REMINDER',
  'TIMER_EXPIRED',
  'BOARD_RELAUNCH_REQUESTED',
  'FAST_TRACK_REJECTED',
  'FAST_TRACK_DROPPED',
  'INTERNSHIP_STARTED',
  'CLOSURE_REPORT_SUBMITTED',
  'TICKET_TRANSFERRED',
  'INTERNSHIP_ENDING',
  'INTERNSHIP_OVERDUE',
  'TRAINING_SHIP_PROPOSED',
  'CONTACT_REQUEST_CLOSED',
  'EMAIL_CHANGE_CODE',
  'EMAIL_CHANGE_NOTICE',
] as const;

export type MessageKey = (typeof MESSAGE_KEYS)[number];

export type MessageText = {
  key: MessageKey;
  notification: boolean;
  /** Segnaposto ammessi, senza graffe (es. «numero»). */
  placeholders: string[];
  parts: MessageTextPart[];
};

export const fetchMessageTexts = async (): Promise<MessageText[]> => {
  const { data } = await axiosInstance.get<MessageText[]>(ENDPOINTS.messageTexts);
  return data;
};

export const updateMessageText = async (args: {
  key: MessageKey;
  part: MessagePart;
  text: string;
}): Promise<MessageText> => {
  const { data } = await axiosInstance.put<MessageText>(
    `${ENDPOINTS.messageTexts}/${args.key}/${args.part}`,
    {
      text: args.text,
    },
  );
  return data;
};

export const resetMessageText = async (args: {
  key: MessageKey;
  part: MessagePart;
}): Promise<MessageText> => {
  const { data } = await axiosInstance.delete<MessageText>(
    `${ENDPOINTS.messageTexts}/${args.key}/${args.part}`,
  );
  return data;
};
