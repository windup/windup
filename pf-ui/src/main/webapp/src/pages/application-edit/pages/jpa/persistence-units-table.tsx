import React, { useMemo, useState } from "react";

import { useSelectionState } from "@migtools/lib-ui";
import { Stack, StackItem } from "@patternfly/react-core";
import {
  Caption,
  IAction,
  ICell,
  IRow,
  IRowData,
  TableComposable,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
} from "@patternfly/react-table";


import { DatasourceDto } from "@app/api/jpa";
import { useJPAsQuery } from "@app/queries/jpa";
import { SimpleTableWithToolbar } from "@app/shared/components";
import { useTable, useTableControls } from "@app/shared/hooks";

export interface TableData {
  path: string;
  version: string;

  name: string;
  properties: { [key: string]: string };
  datasources: DatasourceDto[];
}

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Name",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "JPA Configuration",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "JPA Version",
    transforms: [],
    cellTransforms: [],
  },
];

const getRow = (rowData: IRowData): TableData => {
  return rowData[DataKey];
};

export interface IPersistenceUnitTableProps {
  applicationId: string;
}

export const PersistenceUnitTable: React.FC<IPersistenceUnitTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allJPAsQuery = useJPAsQuery();

  const tableData = useMemo(() => {
    return (
      allJPAsQuery.data
        ?.find((f) => f.applicationId === applicationId)
        ?.jpaConfigurations.flatMap((jpaConfig) => {
          return jpaConfig.persistentUnits.map((persistenceUnit) => {
            const data: TableData = {
              path: jpaConfig.path,
              version: jpaConfig.version,
              name: persistenceUnit.name,
              properties: persistenceUnit.properties,
              datasources: persistenceUnit.datasources,
            };
            return data;
          });
        }) || []
    );
  }, [allJPAsQuery.data, applicationId]);

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<TableData>({
    items: tableData,
    isEqual: (a, b) => a.name === b.name,
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<TableData>({
    items: tableData,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: () => true,
  });

  const itemsToRow = (items: TableData[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const isExpanded = isRowExpanded(item);

      rows.push({
        [DataKey]: item,
        isOpen: isExpanded,
        cells: [
          {
            title: item.name,
          },
          {
            title: item.path,
          },
          {
            title: item.version,
          },
        ],
      });

      // Expanded area
      if (isExpanded) {
        rows.push({
          parent: rows.length - 1,
          fullWidth: true,
          cells: [
            {
              title: (
                <div className="pf-u-m-md">
                  <Stack hasGutter>
                    <StackItem>
                      <TableComposable variant="compact">
                        <Caption>Persistence unit properties</Caption>
                        <Thead>
                          <Tr>
                            <Th>Key</Th>
                            <Th>Value</Th>
                          </Tr>
                        </Thead>
                        <Tbody>
                          {Object.entries(item.properties).map(
                            ([key, value]) => (
                              <Tr key={key}>
                                <Td>{key}</Td>
                                <Td>{value}</Td>
                              </Tr>
                            )
                          )}
                        </Tbody>
                      </TableComposable>
                    </StackItem>
                    <StackItem>
                      <TableComposable variant="compact">
                        <Caption>Datasources</Caption>
                        <Thead>
                          <Tr>
                            <Th>JNDI locations</Th>
                            <Th>Type</Th>
                            <Th>JTA</Th>
                          </Tr>
                        </Thead>
                        <Tbody>
                          {item.datasources.map((datasource, index) => (
                            <Tr key={index}>
                              <Td>{datasource.jndiLocation}</Td>
                              <Td>{datasource.databaseTypeName}</Td>
                              <Td>{datasource.isXA}</Td>
                            </Tr>
                          ))}
                        </Tbody>
                      </TableComposable>
                    </StackItem>
                  </Stack>
                </div>
              ),
            },
          ],
        });
      }
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
      // Expand
      onCollapse={(_event, _rowIndex, _isOpen, rowData) => {
        const issue = getRow(rowData);
        toggleRowExpanded(issue);
      }}
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
