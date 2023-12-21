export interface Dictionary<T = string | number | boolean> {
  value: T;
  label?: string;
  prompt?: string;
  italic?: boolean;
  first?: boolean;

  [key: string]: any;
}
