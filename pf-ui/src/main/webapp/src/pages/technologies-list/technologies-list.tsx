import React, { useCallback, useMemo, useState } from "react";
import { useMatch, useNavigate } from "react-router-dom";

import {
  Bullseye,
  Divider,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  PageSection,
  PageSectionVariants,
  SearchInput,
  SelectVariant,
  Text,
  TextContent,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarFilter,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import ArrowUpIcon from "@patternfly/react-icons/dist/esm/icons/arrow-up-icon";
import FilterIcon from "@patternfly/react-icons/dist/esm/icons/filter-icon";
import {
  ICell,
  IExtraData,
  IRow,
  IRowData,
  TableComposable,
  Tbody,
  Td,
  Tr,
  compoundExpand,
  sortable,
} from "@patternfly/react-table";

import { ApplicationDto } from "@app/api/application";
import { TechnologyGroup } from "@app/api/technologies";
import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { Context, SimpleContextSelector } from "@app/context/simple-context";
import {
  TechnologyGroupValueProcessed,
  TechnologyGroupsProcessed,
} from "@app/models/api-enriched";
import {
  SimpleTableWithToolbar,
  OptionWithValue,
  SimpleSelect,
} from "@app/shared/components";
import {
  useTable,
  useTableControls,
  useCellSelectionState,
  useTechnologiesData,
} from "@app/shared/hooks";

interface RowData {
  application: ApplicationDto;
  technologyGroups: TechnologyGroupsProcessed;
}

const getTechnologyEntriesSorted = (val: TechnologyGroupValueProcessed) => {
  return Object.entries(val).sort(([a], [b]) => a.localeCompare(b));
};

const DataKey = "DataKey";

const getRow = (rowData: IRowData): RowData => {
  return rowData[DataKey];
};

export const TechnologiesList: React.FC = () => {
  const matchTechnologiesPage = useMatch("/technologies");
  const matchAllTechnologiesPage = useMatch("/technologies/applications");
  const matchSingleTechnologyPage = useMatch(
    "/technologies/applications/:applicationId"
  );

  const applicationId = matchTechnologiesPage
    ? undefined
    : matchAllTechnologiesPage
    ? ALL_APPLICATIONS_ID
    : matchSingleTechnologyPage?.params.applicationId;

  const navigate = useNavigate();

  const onContextChange = (context: Context) => {
    navigate("/technologies/applications/" + context.key);
  };

  // Filters
  const [filterText, setFilterText] = useState("");
  const [technologyGroup, setTechnologyGroup] =
    useState<TechnologyGroup>("View");

  // Data
  const { applications, categoryOptions, allApplications, allTechnologies } =
    useTechnologiesData({
      applicationId: applicationId,
    });

  // Columns
  const columnKeys: string[] = useMemo(() => {
    if (applications.length > 0) {
      return Object.entries(applications[0].technologyGroups)
        .map(([groupName, groupValue]) =>
          Object.keys(groupValue).map((technologyName) => {
            return technologyName;
          })
        )
        .flatMap((e) => e);
    } else {
      return [];
    }
  }, [applications]);

  const columns: ICell[] = useMemo(() => {
    let result: ICell[] = [
      {
        title: "Application",
        transforms: [sortable],
        cellTransforms: [],
      },
    ];

    if (applications.length > 0) {
      const technologies = applications[0].technologyGroups[technologyGroup];
      const techColumns = getTechnologyEntriesSorted(technologies).map(
        ([technologyName, technologyValue]) => {
          const colum: ICell = {
            title: technologyName,
            transforms: [sortable],
            cellTransforms: [compoundExpand],
            data: technologyName,
          };
          return colum;
        }
      );
      result = [...result, ...techColumns];
    }

    return result;
  }, [technologyGroup, applications]);

  const getColumn = useCallback(
    (colIndex: number): string => {
      return columns[colIndex].data;
    },
    [columns]
  );

  // Rows
  const { isCellSelected, isSomeCellSelected, toggleCellSelected } =
    useCellSelectionState<string, string>({
      rows: applications.map((f) => f.application.id),
      columns: columnKeys,
    });

  const compareByColumnIndex = (
    a: RowData,
    b: RowData,
    columnIndex?: number
  ) => {
    switch (columnIndex) {
      case 0:
        return a.application.name.localeCompare(b.application.name);
      default:
        if (columnIndex !== undefined) {
          const [, v1] = getTechnologyEntriesSorted(
            a.technologyGroups[technologyGroup]
          )[columnIndex - 1];
          const [, v2] = getTechnologyEntriesSorted(
            b.technologyGroups[technologyGroup]
          )[columnIndex - 1];

          return v1.total - v2.total;
        }

        return 0;
    }
  };

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<RowData>({
    items: applications,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => {
      let isFilterTextFilterCompliant = true;
      if (filterText && filterText.trim().length > 0) {
        isFilterTextFilterCompliant =
          item.application.name
            .toLowerCase()
            .indexOf(filterText.toLowerCase()) !== -1;
      }

      return isFilterTextFilterCompliant;
    },
  });

  const itemsToRow = (items: RowData[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const technologies = getTechnologyEntriesSorted(
        item.technologyGroups[technologyGroup]
      );
      const numberOfTechnologies = Object.keys(technologies).length;

      const cells: ICell[] = technologies.map(
        ([technologyName, technologyValue]) => {
          return {
            title: technologyValue.total,
            props: {
              isOpen: isCellSelected(item.application.id, technologyName),
            },
          };
        }
      );

      rows.push({
        [DataKey]: item,
        isOpen: isSomeCellSelected(item.application.id, columnKeys),
        cells: [
          {
            title: item.application.name,
          },
          ...cells,
        ],
      });

      const parentIndex = rows.length - 1;

      // Expanded area
      technologies.forEach(([technologyName, { total, tags }], index) => {
        rows.push({
          parent: parentIndex,
          compoundParent: 1 + index,
          cells: [
            {
              title: (
                <div>
                  <TableComposable
                    aria-label="Simple table"
                    variant="compact"
                    isStriped
                  >
                    <Tbody>
                      {Object.entries(tags).map(([k, v], index) => (
                        <Tr key={index}>
                          <Td width={30} dataLabel={`key-${index}`}>
                            {k}
                          </Td>
                          <Td dataLabel={`val-${index}`}>{v}</Td>
                        </Tr>
                      ))}
                      <Tr>
                        <Td width={30} dataLabel="Total">
                          <strong>Total</strong>
                        </Td>
                        <Td width={70}>
                          <strong>{total}</strong>
                        </Td>
                      </Tr>
                    </Tbody>
                  </TableComposable>
                </div>
              ),
              props: {
                colSpan: numberOfTechnologies + 1,
                className: "pf-m-no-padding",
              },
            },
          ],
        });
      });
    });

    return rows;
  };

  const rows: IRow[] = itemsToRow(pageItems);

  return (
    <>
      <PageSection padding={{ default: "noPadding" }}>
        <Toolbar>
          <ToolbarContent>
            <ToolbarItem>Application:</ToolbarItem>
            <ToolbarItem>
              <SimpleContextSelector
                contextKeyFromURL={applicationId}
                onChange={onContextChange}
              />
            </ToolbarItem>
          </ToolbarContent>
        </Toolbar>
      </PageSection>
      <Divider />
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Technologies</Text>
          <Text component="small">
            This report is a statistic of technologies occurrences in the input
            applications. It shows how the technologies are distributed and is
            mostly useful when analysing many applications.
          </Text>
        </TextContent>
      </PageSection>
      <PageSection variant={PageSectionVariants.default}>
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
              onExpand={(
                event: React.MouseEvent,
                rowIndex: number,
                colIndex: number,
                isOpen: boolean,
                rowData: IRowData,
                extraData: IExtraData
              ) => {
                const row = getRow(rowData);
                const columnKey = getColumn(colIndex);
                toggleCellSelected(row.application.id, columnKey);
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
              // Fech data
              isLoading={
                allApplications.isFetching || allTechnologies.isFetching
              }
              loadingVariant="skeleton"
              fetchError={allApplications.isError || allTechnologies.isError}
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
                  <ToolbarGroup variant="filter-group">
                    <ToolbarFilter categoryName="Category">
                      <SimpleSelect
                        width={250}
                        maxHeight={300}
                        toggleIcon={<FilterIcon />}
                        variant={SelectVariant.single}
                        aria-label="category"
                        aria-labelledby="category"
                        placeholderText="Category"
                        value={categoryOptions.find(
                          (e) => e.value === technologyGroup
                        )}
                        options={categoryOptions}
                        onChange={(option) => {
                          const optionValue =
                            option as OptionWithValue<TechnologyGroup>;
                          setTechnologyGroup(optionValue.value);
                        }}
                      />
                    </ToolbarFilter>
                  </ToolbarGroup>
                </>
              }
            />
          )}
        </>
      </PageSection>
    </>
  );
};
