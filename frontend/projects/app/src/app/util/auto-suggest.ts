export interface SuggestOption {
  value: string;
  view: any;
}

export interface SuggestGroup {
  label: string;
  options: SuggestOption[];
}
