import React, { useMemo, useState } from "react";

import {
  Bullseye,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  SearchInput,
  Spinner,
  Title,
  ToolbarItem,
} from "@patternfly/react-core";
import { ArrowUpIcon } from "@patternfly/react-icons";
import {
  IAction,
  ICell,
  IRow,
  breakWord,
  sortable,
} from "@patternfly/react-table";
import {
  ConditionalRender,
  SimpleTableWithToolbar,
  useTable,
  useTableControls,
} from "@project-openubl/lib-ui";

import { IgnoredFileDto } from "@app/api/application-ignored-files";
import { useIgnoredFilesQuery } from "@app/queries/ignored-files";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Name",
    transforms: [sortable],
    cellTransforms: [breakWord],
  },
  {
    title: "Path",
    transforms: [],
    cellTransforms: [breakWord],
  },
  {
    title: "Ignored reason",
    transforms: [],
    cellTransforms: [breakWord],
  },
];

const compareByColumnIndex = (
  a: IgnoredFileDto,
  b: IgnoredFileDto,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 1: // name
      return a.fileName.localeCompare(b.fileName);
    default:
      return 0;
  }
};

export interface IIgnoredFilesTableProps {
  applicationId?: string;
}

export const IgnoredFilesTable: React.FC<IIgnoredFilesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText, setFilterText] = useState("");

  // Queries
  const allIgnoredFiles = useIgnoredFilesQuery();

  const dependencies = useMemo(() => {
    if (applicationId === "") {
      return [...(allIgnoredFiles.data || [])].flatMap((e) => e.ignoredFiles);
    }

    return (
      allIgnoredFiles.data?.find((f) => f.applicationId === applicationId)
        ?.ignoredFiles || []
    );
  }, [allIgnoredFiles.data, applicationId]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<IgnoredFileDto>({
    items: dependencies,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => {
      let isFilterTextFilterCompliant = true;
      if (filterText && filterText.trim().length > 0) {
        isFilterTextFilterCompliant =
          item.fileName.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;
      }

      return isFilterTextFilterCompliant;
    },
  });

  const itemsToRow = (items: IgnoredFileDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.fileName,
          },
          {
            title: item.filePath,
          },
          {
            title: item.reason,
          },
        ],
      });
    });

    return rows;
  };

  const rows: IRow[] = itemsToRow(pageItems);
  const actions: IAction[] = [];

  return (
    <ConditionalRender
      when={allIgnoredFiles.isLoading}
      then={
        <Bullseye>
          <Spinner />
        </Bullseye>
      }
    >
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
          isLoading={allIgnoredFiles.isFetching}
          loadingVariant="skeleton"
          fetchError={allIgnoredFiles.isError}
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
    </ConditionalRender>
  );
};
