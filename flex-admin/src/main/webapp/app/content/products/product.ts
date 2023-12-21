export interface Tab {
  label: string;
  type: TabType;
}

export type TabType = 'list' | 'forecasted-prices';
