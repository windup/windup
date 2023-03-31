import { useMemo } from 'react';

import { SortByDirection } from '@patternfly/react-table';

import { Page, SortBy } from '../../common/types';

// Hook

interface HookArgs<T> {
  items?: T[];

  currentSortBy?: SortBy;
  compareToByColumn: (a: T, b: T, columnIndex?: number) => number;

  currentPage: Page;
  filterItem: (value: T) => boolean;
}

interface HookState<T> {
  pageItems: T[];
  filteredItems: T[];
}

export const useTable = <T>({
  items,
  currentSortBy,
  currentPage,
  filterItem,
  compareToByColumn,
}: HookArgs<T>): HookState<T> => {
  const state: HookState<T> = useMemo(() => {
    const allItems = [...(items || [])];

    // Filter
    const filteredItems = allItems.filter(filterItem);

    //  Sort
    let orderChanged = false;

    let sortedItems: T[];
    sortedItems = [...filteredItems].sort((a, b) => {
      const comparisonResult = compareToByColumn(a, b, currentSortBy?.index);
      if (comparisonResult !== 0) {
        orderChanged = true;
      }
      return comparisonResult;
    });

    if (orderChanged && currentSortBy?.direction === SortByDirection.desc) {
      sortedItems = sortedItems.reverse();
    }

    // Paginate
    const pageItems = sortedItems.slice(
      (currentPage.page - 1) * currentPage.perPage,
      currentPage.page * currentPage.perPage
    );

    return {
      pageItems,
      filteredItems,
    };
  }, [items, currentPage, currentSortBy, compareToByColumn, filterItem]);

  return state;
};
