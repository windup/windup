import React, { useMemo, useState, useEffect } from "react";

import { useSelectionState } from "@migtools/lib-ui";
import {
  Bullseye,
  DescriptionList,
  DescriptionListDescription,
  DescriptionListGroup,
  DescriptionListTerm,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  List,
  ListItem,
  SearchInput,
  Title,
  ToolbarItem,
} from "@patternfly/react-core";
import ArrowUpIcon from "@patternfly/react-icons/dist/esm/icons/arrow-up-icon";
import {
  IAction,
  ICell,
  IRow,
  IRowData,
  cellWidth,
  sortable,
  truncate,
} from "@patternfly/react-table";


import { DependencyDto } from "@app/api/dependencies";
import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { useApplicationsQuery } from "@app/queries/applications";
import { useDependenciesQuery } from "@app/queries/dependencies";
import { SimpleTableWithToolbar } from "@app/shared/components";
import { useTable, useTableControls } from "@app/shared/hooks";

const areDependenciesEquals = (a: DependencyDto, b: DependencyDto) => {
  return a.name === b.name && a.version === b.version && a.sha1 === b.sha1;
};

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Name",
    transforms: [cellWidth(100), sortable],
    cellTransforms: [truncate],
  },
];

const compareByColumnIndex = (
  a: DependencyDto,
  b: DependencyDto,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 1: // name
      return a.name.localeCompare(b.name);
    default:
      return 0;
  }
};

const getRow = (rowData: IRowData): DependencyDto => {
  return rowData[DataKey];
};

export interface IDependenciesTableProps {
  applicationId?: string;
}

export const DependenciesTable: React.FC<IDependenciesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText, setFilterText] = useState("");

  // Queries
  const allApplicationsQuery = useApplicationsQuery();
  const allDependenciesQuery = useDependenciesQuery();

  const dependencies = useMemo(() => {
    if (applicationId === ALL_APPLICATIONS_ID) {
      return [...(allDependenciesQuery.data || [])]
        .filter((e) => {
          const application = allApplicationsQuery.data?.find(
            (app) => app.id === e.applicationId
          );
          return !application?.isVirtual;
        })
        .flatMap((e) => e.dependencies)
        .reduce((prev, current) => {
          const duplicateDependency = prev.find((f) => {
            return areDependenciesEquals(f, current);
          });

          if (duplicateDependency) {
            return [
              ...prev.filter((f) => !areDependenciesEquals(f, current)),
              {
                ...current,
                foundPaths: [
                  ...duplicateDependency.foundPaths,
                  ...current.foundPaths,
                ],
              },
            ];
          } else {
            return [...prev, current];
          }
        }, [] as DependencyDto[]);
    } else {
      return (
        allDependenciesQuery.data?.find(
          (f) => f.applicationId === applicationId
        )?.dependencies || []
      );
    }
  }, [allDependenciesQuery.data, allApplicationsQuery.data, applicationId]);

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<DependencyDto>({
    items: dependencies,
    isEqual: areDependenciesEquals,
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<DependencyDto>({
    items: dependencies,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => {
      let isFilterTextFilterCompliant = true;
      if (filterText && filterText.trim().length > 0) {
        isFilterTextFilterCompliant =
          item.name.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;
      }

      return isFilterTextFilterCompliant;
    },
  });

  const itemsToRow = (items: DependencyDto[]) => {
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
                  <DescriptionList isHorizontal>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Maven URL</DescriptionListTerm>
                      <DescriptionListDescription>
                        <a
                          target="_blank"
                          rel="noreferrer"
                          href={`http://search.maven.org/?eh#search|ga|1|1:"${item.sha1}"`}
                        >
                          Maven Central Link
                        </a>
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>SHA1 hash</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.sha1}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    {item.version && (
                      <DescriptionListGroup>
                        <DescriptionListTerm>Version</DescriptionListTerm>
                        <DescriptionListDescription>
                          {item.version}
                        </DescriptionListDescription>
                      </DescriptionListGroup>
                    )}
                    {item.organization && (
                      <DescriptionListGroup>
                        <DescriptionListTerm>Organization</DescriptionListTerm>
                        <DescriptionListDescription>
                          {item.organization}
                        </DescriptionListDescription>
                      </DescriptionListGroup>
                    )}
                    <DescriptionListGroup>
                      <DescriptionListTerm>Found at path</DescriptionListTerm>
                      <DescriptionListDescription>
                        <List>
                          {item.foundPaths.map((path, index) => (
                            <ListItem key={index}>{path}</ListItem>
                          ))}
                        </List>
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                  </DescriptionList>
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

  // Reset pagination when application change
  useEffect(() => {
    onPageChange({ page: 1, perPage: currentPage.perPage });
  }, [applicationId, onPageChange, currentPage.perPage]);

  return (
    <>
      {applicationId === undefined ? (
        <Bullseye>
          <EmptyState>
            <EmptyStateIcon icon={ArrowUpIcon} />
            <Title headingLevel="h4" size="lg">
              Select an application
            </Title>
            <EmptyStateBody>
              Select an application whose data you want to get access to.
            </EmptyStateBody>
          </EmptyState>
        </Bullseye>
      ) : (
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
          sortBy={
            currentSortBy || { index: undefined, defaultDirection: "asc" }
          }
          onSort={onChangeSortBy}
          // Pagination
          currentPage={currentPage}
          onPageChange={onPageChange}
          // Table
          rows={rows}
          cells={columns}
          actions={actions}
          // Fech data
          isLoading={allDependenciesQuery.isFetching}
          loadingVariant="skeleton"
          fetchError={allDependenciesQuery.isError}
          // Toolbar filters
          filtersApplied={filterText.trim().length > 0}
          toolbarToggle={
            <>
              <ToolbarItem variant="search-filter">
                <SearchInput
                  value={filterText}
                  onChange={setFilterText}
                  onClear={() => setFilterText("")}
                />
              </ToolbarItem>
            </>
          }
        />
      )}
    </>
  );
};
