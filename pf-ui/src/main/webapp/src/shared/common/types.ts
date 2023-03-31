export interface Page {
  page: number;
  perPage: number;
}

export interface SortBy {
  index: number;
  direction: 'asc' | 'desc';
}
