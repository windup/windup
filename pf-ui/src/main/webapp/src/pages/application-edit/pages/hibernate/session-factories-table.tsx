import React, { useMemo, useState } from "react";

import { useSelectionState } from "@migtools/lib-ui";
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

import { useHibernateQuery } from "@app/queries/hibernate";
import { SimpleTableWithToolbar } from "@app/shared/components";
import { useTable, useTableControls } from "@app/shared/hooks";


export interface TableData {
  path: string;
  properties: { [key: string]: string };
}

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Session factory",
    transforms: [],
    cellTransforms: [],
  },
];

const getRow = (rowData: IRowData): TableData => {
  return rowData[DataKey];
};

export interface ISessionFactoriesTableProps {
  applicationId: string;
}

export const SessionFactoriesTable: React.FC<ISessionFactoriesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allHibernateQuery = useHibernateQuery();

  const tableData = useMemo(() => {
    return (
      allHibernateQuery.data
        ?.find((f) => f.applicationId === applicationId)
        ?.hibernateConfigurations.flatMap((jpaConfig) => {
          return jpaConfig.sessionFactories.map((sessionFactory) => {
            const data: TableData = {
              path: jpaConfig.path,
              properties: sessionFactory.properties,
            };
            return data;
          });
        }) || []
    );
  }, [allHibernateQuery.data, applicationId]);

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<TableData>({
    items: tableData,
    isEqual: (a, b) => a.path === b.path,
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
            title: item.path,
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
                  <TableComposable variant="compact">
                    <Caption>Session factory properties</Caption>
                    <Thead>
                      <Tr>
                        <Th>Key</Th>
                        <Th>Value</Th>
                      </Tr>
                    </Thead>
                    <Tbody>
                      {Object.entries(item.properties).map(([key, value]) => (
                        <Tr key={key}>
                          <Td>{key}</Td>
                          <Td>{value}</Td>
                        </Tr>
                      ))}
                    </Tbody>
                  </TableComposable>
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
      isLoading={allHibernateQuery.isFetching}
      loadingVariant="skeleton"
      fetchError={allHibernateQuery.isError}
      // Toolbar filters
      filtersApplied={filterText.trim().length > 0}
    />
  );
};
