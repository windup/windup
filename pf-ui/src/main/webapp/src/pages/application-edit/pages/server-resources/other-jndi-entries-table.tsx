import React, { useMemo, useState } from "react";

import { IAction, ICell, IRow } from "@patternfly/react-table";


import { OtherJndiEntryDto } from "@app/api/server-resources";
import { useServerResourcesQuery } from "@app/queries/server-resources";
import { SimpleTableWithToolbar } from "@app/shared/components";
import { useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "JNDI location",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IOtherJndiEntriesTableProps {
  applicationId: string;
}

export const OtherJndiEntriesTable: React.FC<IOtherJndiEntriesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allServerResourcesQuery = useServerResourcesQuery();

  const otherJndiEntries = useMemo(() => {
    return (
      allServerResourcesQuery.data?.find(
        (f) => f.applicationId === applicationId
      )?.otherJndiEntries || []
    );
  }, [allServerResourcesQuery.data, applicationId]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<OtherJndiEntryDto>({
    items: otherJndiEntries,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: OtherJndiEntryDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.jndiLocation,
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
      isLoading={allServerResourcesQuery.isFetching}
      loadingVariant="skeleton"
      fetchError={allServerResourcesQuery.isError}
      // Toolbar filters
      filtersApplied={filterText.trim().length > 0}
    />
  );
};
