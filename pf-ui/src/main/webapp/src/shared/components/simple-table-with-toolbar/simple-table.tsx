import React from 'react';

import { Bullseye, Spinner, Skeleton } from '@patternfly/react-core';
import { Table, TableHeader, TableBody, IRow, TableProps } from '@patternfly/react-table';

import { SimpleStateError } from './simple-state-error';
import { SimpleStateNoData } from './simple-state-no-data';
import { SimpleStateNoResults } from './simple-state-no-results';

export interface ISimpleTableProps extends TableProps {
  isLoading: boolean;
  loadingVariant?: 'skeleton' | 'spinner' | 'none';
  fetchError?: any;

  filtersApplied: boolean;
  noDataState?: any;
  noSearchResultsState?: any;
  errorState?: any;
}

export const SimpleTable: React.FC<ISimpleTableProps> = ({
  cells,
  rows,
  'aria-label': ariaLabel = 'main-table',

  isLoading,
  fetchError,
  loadingVariant = 'skeleton',

  filtersApplied,
  noDataState,
  noSearchResultsState,
  errorState,

  ...rest
}) => {
  if (isLoading && loadingVariant !== 'none') {
    let rows: IRow[] = [];
    if (loadingVariant === 'skeleton') {
      rows = [...Array(10)].map(() => {
        return {
          cells: [...Array(cells.length)].map(() => ({
            title: <Skeleton />,
          })),
        };
      });
    } else if (loadingVariant === 'spinner') {
      rows = [
        {
          heightAuto: true,
          cells: [
            {
              props: { colSpan: 8 },
              title: (
                <Bullseye>
                  <Spinner size="xl" />
                </Bullseye>
              ),
            },
          ],
        },
      ];
    } else {
      throw new Error('Can not determine the loading state of table');
    }

    return (
      <Table aria-label={ariaLabel} cells={cells} rows={rows}>
        <TableHeader />
        <TableBody />
      </Table>
    );
  }

  if (fetchError) {
    return (
      <>
        <Table aria-label={ariaLabel} cells={cells} rows={[]}>
          <TableHeader />
          <TableBody />
        </Table>
        {errorState ? errorState : <SimpleStateError />}
      </>
    );
  }

  if (rows.length === 0) {
    return filtersApplied ? (
      <>
        <Table aria-label={ariaLabel} cells={cells} rows={[]}>
          <TableHeader />
          <TableBody />
        </Table>
        {noSearchResultsState ? noSearchResultsState : <SimpleStateNoResults />}
      </>
    ) : (
      <>
        <Table aria-label={ariaLabel} cells={cells} rows={[]}>
          <TableHeader />
          <TableBody />
        </Table>
        {noDataState ? noDataState : <SimpleStateNoData />}
      </>
    );
  }

  return (
    <Table aria-label={ariaLabel} cells={cells} rows={rows} {...rest}>
      <TableHeader />
      <TableBody />
    </Table>
  );
};
