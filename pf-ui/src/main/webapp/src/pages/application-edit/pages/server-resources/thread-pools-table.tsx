import React, { useMemo, useState } from "react";

import { List, ListItem } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";


import { ThreadPoolDto } from "@app/api/server-resources";
import { useServerResourcesQuery } from "@app/queries/server-resources";
import { SimpleTableWithToolbar } from "@app/shared/components";
import { useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Pool name",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Max size",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Min size",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Links",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IThreadPoolsTableProps {
  applicationId: string;
}

export const ThreadPoolsTable: React.FC<IThreadPoolsTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allServerResourcesQuery = useServerResourcesQuery();

  const threadPools = useMemo(() => {
    return (
      allServerResourcesQuery.data?.find(
        (f) => f.applicationId === applicationId
      )?.threadPools || []
    );
  }, [allServerResourcesQuery.data, applicationId]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<ThreadPoolDto>({
    items: threadPools,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: ThreadPoolDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.poolName,
          },
          {
            title: item.maxPoolSize,
          },
          {
            title: item.minPoolSize,
          },
          {
            title: (
              <List isPlain>
                {item.links?.map((f, index) => (
                  <ListItem key={index}>
                    <a href={f.link}>{f.description}</a>
                  </ListItem>
                ))}
              </List>
            ),
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
