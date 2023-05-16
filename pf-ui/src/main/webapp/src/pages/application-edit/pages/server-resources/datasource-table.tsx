import React, { useMemo, useState } from "react";

import { List, ListItem } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";


import { DatasourceDto } from "@app/api/server-resources";
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
  {
    title: "Database",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Links",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IDatasourcesTableProps {
  applicationId: string;
}

export const DatasourcesTable: React.FC<IDatasourcesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allServerResourcesQuery = useServerResourcesQuery();

  const datasources = useMemo(() => {
    return (
      allServerResourcesQuery.data?.find(
        (f) => f.applicationId === applicationId
      )?.datasources || []
    );
  }, [allServerResourcesQuery.data, applicationId]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<DatasourceDto>({
    items: datasources,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: () => true,
  });

  const itemsToRow = (items: DatasourceDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.jndiLocation,
          },
          {
            title: (
              <div>
                {item.databaseTypeName && <span>{item.databaseTypeName}</span>}{" "}
                {item.databaseTypeVersion && (
                  <span>{item.databaseTypeVersion}</span>
                )}
              </div>
            ),
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
