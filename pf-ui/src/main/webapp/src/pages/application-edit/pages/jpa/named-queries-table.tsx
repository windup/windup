import React, { useMemo, useState } from "react";

import { IAction, ICell, IRow } from "@patternfly/react-table";


import { JPANamedQueryDto } from "@app/api/jpa";
import { useJPAsQuery } from "@app/queries/jpa";
import { SimpleTableWithToolbar } from "@app/shared/components";
import { useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Name",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Query",
    transforms: [],
    cellTransforms: [],
  },
];

export interface INamedQueriesTableProps {
  applicationId: string;
}

export const NamedQueriesTable: React.FC<INamedQueriesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allJPAsQuery = useJPAsQuery();

  const tableData = useMemo(() => {
    return (
      allJPAsQuery.data?.find((f) => f.applicationId === applicationId)
        ?.namesQueries || []
    );
  }, [allJPAsQuery.data, applicationId]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<JPANamedQueryDto>({
    items: tableData,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: () => true,
  });

  const itemsToRow = (items: JPANamedQueryDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.queryName,
          },
          {
            title: item.query,
          },
        ],
      });
    });

    return rows;
  };

  const rows: IRow[] = itemsToRow(pageItems);
  const actions: IAction[] = [];

  return (
    <SimpleTableWithToolbar
      hasTopPagination
      hasBottomPagination
      totalCount={filteredItems.length}
      // Sorting
      sortBy={currentSortBy || { index: undefined, defaultDirection: "asc" }}
      onSort={onChangeSortBy}
      // Pagination
      currentPage={currentPage}
      onPageChange={onPageChange}
      // Table
      rows={rows}
      cells={columns}
      actions={actions}
      // Fech data
      isLoading={allJPAsQuery.isFetching}
      loadingVariant="skeleton"
      fetchError={allJPAsQuery.isError}
      // Toolbar filters
      filtersApplied={filterText.trim().length > 0}
    />
  );
};
