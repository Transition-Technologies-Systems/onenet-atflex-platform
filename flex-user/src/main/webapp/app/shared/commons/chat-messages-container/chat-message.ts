export interface ChatMessage {
  id: number;
  content?: string;
  createdBy: string;
  createdDate: string;
  userId: number;
  myCompanyMessage?: boolean;
  read?: boolean;

  automat?: boolean;
  attachments?: { id: number; name: string }[];
  attachedFileName?: string;
}

export interface ChatSendMessage {
  content: string | undefined;
  attachments: File[];
}
