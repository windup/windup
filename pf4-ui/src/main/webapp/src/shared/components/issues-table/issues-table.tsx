import React, { useEffect, useMemo, useState } from "react";

import { useSelectionState } from "@migtools/lib-ui";
import {
  Bullseye,
  Button,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Modal,
  SearchInput,
  SelectVariant,
  Spinner,
  Title,
  ToolbarChip,
  ToolbarChipGroup,
  ToolbarFilter,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { ArrowUpIcon } from "@patternfly/react-icons";
import {
  IAction,
  ICell,
  IExtraData,
  IRow,
  IRowData,
  cellWidth,
  sortable,
  truncate,
} from "@patternfly/react-table";
import {
  ConditionalRender,
  OptionWithValue,
  SimpleSelect,
  SimpleTableWithToolbar,
  useModal,
  useTable,
  useTableControls,
  useToolbar,
} from "@project-openubl/lib-ui";

import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { useProcessedQueriesContext } from "@app/context/processed-queries-context";
import { IssueProcessed } from "@app/models/api-enriched";
import { useApplicationsQuery } from "@app/queries/applications";
import { useFilesQuery } from "@app/queries/files";
import { useIssuesQuery } from "@app/queries/issues";
import { FileEditor, RuleEditor } from "@app/shared/components";
import { technologiesToArray } from "@app/utils/rule-utils";

import { IssueOverview } from "./components/issue-overview";
import { Technologies } from "./components/technologies";

const toOption = (option: string | ToolbarChip): OptionWithValue => {
  if (typeof option === "string") {
    const toStringFn = () => option;
    return {
      value: option,
      toString: toStringFn,
      compareTo: (other: string | OptionWithValue) => {
        return typeof other === "string"
          ? toStringFn().toLowerCase().includes(other.toLocaleLowerCase())
          : option === other.value;
      },
    };
  } else {
    const toStringFn = () => option.node as string;
    return {
      value: option.key,
      toString: toStringFn,
      compareTo: (other: string | OptionWithValue) => {
        return typeof other === "string"
          ? toStringFn().toLowerCase().includes(other.toLowerCase())
          : option.key === other.value;
      },
    };
  }
};

const toToolbarChip = (option: OptionWithValue): ToolbarChip => {
  return {
    key: option.value,
    node: option.toString(),
  };
};

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Issue",
    transforms: [cellWidth(35), sortable],
    cellTransforms: [],
  },
  {
    title: "Category",
    transforms: [cellWidth(10)],
  },
  {
    title: "Source technologies",
    transforms: [cellWidth(10)],
  },
  {
    title: "Target technologies",
    transforms: [cellWidth(10)],
  },
  {
    title: "Level of effort",
    transforms: [cellWidth(15)],
    cellTransforms: [truncate],
  },
  {
    title: "Total incidents",
    transforms: [cellWidth(10)],
  },
  {
    title: "Total storypoints",
    transforms: [cellWidth(10)],
  },
];

export const compareByColumnIndex = (
  a: IssueProcessed,
  b: IssueProcessed,
  columnIndex?: number
) => {
  switch (columnIndex) {
    case 1: // name
      return a.name.localeCompare(b.name);
    default:
      return 0;
  }
};

const getRow = (rowData: IRowData): IssueProcessed => {
  return rowData[DataKey];
};

export interface IIssuesTableProps {
  applicationId?: string;
}

export const IssuesTable: React.FC<IIssuesTableProps> = ({ applicationId }) => {
  // Filters
  const [filterText, setFilterText] = useState("");
  const { filters, setFilter, removeFilter, clearAllFilters } = useToolbar<
    "category" | "levelOfEffort" | "sourceTechnology" | "targetTechnology",
    ToolbarChip
  >();

  // Queries
  const allApplications = useApplicationsQuery();
  const allIssues = useIssuesQuery();
  const allFiles = useFilesQuery();

  const { technologies, rulesByIssueId } = useProcessedQueriesContext();

  // Modal
  const issueModal = useModal<"showRule", IssueProcessed>();
  const issueModalMappedRule = useMemo(() => {
    return issueModal.data != null
      ? rulesByIssueId.get(issueModal.data.id)
      : undefined;
  }, [rulesByIssueId, issueModal.data]);

  const fileModal = useModal<"showFile", string>();
  const fileModalMappedFile = useMemo(() => {
    return allFiles.data?.find((file) => file.id === fileModal.data);
  }, [allFiles.data, fileModal.data]);

  const issues = useMemo(() => {
    if (applicationId === ALL_APPLICATIONS_ID) {
      return [...(allIssues.data || [])].flatMap((e) => e.issues);
    }

    return (
      allIssues.data?.find((f) => f.applicationId === applicationId)?.issues ||
      []
    );
  }, [allIssues.data, applicationId]);

  //
  const categories = useMemo(() => {
    const allCategories = (allIssues.data || [])
      .flatMap((f) => f.issues)
      .map((e) => e.category);
    return Array.from(new Set(allCategories)).sort((a, b) =>
      a.localeCompare(b)
    );
  }, [allIssues.data]);

  const levelOfEfforts = useMemo(() => {
    const allLevelOfEfforts = (allIssues.data || [])
      .flatMap((f) => f.issues)
      .map((e) => e.effort.description);
    return Array.from(new Set(allLevelOfEfforts)).sort((a, b) =>
      a.localeCompare(b)
    );
  }, [allIssues.data]);

  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<IssueProcessed>({
    items: issues,
    isEqual: (a, b) => a.id === b.id,
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<IssueProcessed>({
    items: issues,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: (item) => {
      let isFilterTextFilterCompliant = true;
      if (filterText && filterText.trim().length > 0) {
        isFilterTextFilterCompliant =
          item.name.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;
      }

      let isCategoryFilterCompliant = true;
      const selectedCategories = filters.get("category") || [];
      if (selectedCategories.length > 0) {
        isCategoryFilterCompliant = selectedCategories.some(
          (f) => item.category === f.key
        );
      }

      let isLevelOfEffortCompliant = true;
      const selectedLevelOfEfforts = filters.get("levelOfEffort") || [];
      if (selectedLevelOfEfforts.length > 0) {
        isLevelOfEffortCompliant = selectedLevelOfEfforts.some(
          (f) => item.effort.description === f.key
        );
      }

      let isSourceCompliant = true;
      const selectedSources = filters.get("sourceTechnology") || [];
      if (selectedSources.length > 0) {
        isSourceCompliant = selectedSources.some((f) => {
          const rule = rulesByIssueId.get(item.id);
          if (rule) {
            return technologiesToArray(rule.sourceTechnology || []).includes(
              f.key
            );
          }

          return false;
        });
      }

      let isTargetCompliant = true;
      const selectedTargets = filters.get("targetTechnology") || [];
      if (selectedTargets.length > 0) {
        isTargetCompliant = selectedTargets.some((f) => {
          const rule = rulesByIssueId.get(item.id);
          if (rule) {
            return technologiesToArray(rule.targetTechnology || []).includes(
              f.key
            );
          }

          return false;
        });
      }

      return (
        isFilterTextFilterCompliant &&
        isCategoryFilterCompliant &&
        isLevelOfEffortCompliant &&
        isSourceCompliant &&
        isTargetCompliant
      );
    },
  });

  const itemsToRow = (items: IssueProcessed[]) => {
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
            title: item.category,
          },
          {
            title: <Technologies ruleId={item.ruleId} variant="source" />,
          },
          {
            title: <Technologies ruleId={item.ruleId} variant="target" />,
          },
          {
            title: item.effort.description,
          },
          {
            title: item.totalIncidents,
          },
          {
            title: item.totalStoryPoints,
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
                <div className="pf-u-m-sm">
                  <IssueOverview
                    issue={item}
                    onShowFile={(file) => fileModal.open("showFile", file)}
                  />
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
  const actions: IAction[] = [
    {
      title: "View rule",
      onClick: (
        event: React.MouseEvent,
        rowIndex: number,
        rowData: IRowData,
        extraData: IExtraData
      ) => {
        const row = getRow(rowData);
        issueModal.open("showRule", row);
      },
    },
  ];

  // Reset pagination when application change
  useEffect(() => {
    onPageChange({ page: 1 });
  }, [applicationId, filters, onPageChange]);

  return (
    <>
      <ConditionalRender
        when={allApplications.isLoading || allIssues.isLoading}
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
            isLoading={allIssues.isFetching}
            loadingVariant="skeleton"
            fetchError={allIssues.isError}
            // Toolbar filters
            toolbarClearAllFilters={clearAllFilters}
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
                  <ToolbarFilter
                    chips={filters.get("category")}
                    deleteChip={(
                      category: string | ToolbarChipGroup,
                      chip: ToolbarChip | string
                    ) => removeFilter("category", chip)}
                    deleteChipGroup={() => setFilter("category", [])}
                    categoryName={{ key: "category", name: "Category" }}
                  >
                    <SimpleSelect
                      maxHeight={300}
                      variant={SelectVariant.checkbox}
                      aria-label="category"
                      aria-labelledby="category"
                      placeholderText="Category"
                      value={filters.get("category")?.map(toOption)}
                      options={categories.map(toOption)}
                      onChange={(option) => {
                        const optionValue = option as OptionWithValue<string>;

                        const elementExists = (
                          filters.get("category") || []
                        ).some((f) => f.key === optionValue.value);
                        let newElements: ToolbarChip[];
                        if (elementExists) {
                          newElements = (filters.get("category") || []).filter(
                            (f) => f.key !== optionValue.value
                          );
                        } else {
                          newElements = [
                            ...(filters.get("category") || []),
                            toToolbarChip(optionValue),
                          ];
                        }

                        setFilter("category", newElements);
                      }}
                      hasInlineFilter
                      onClear={() => setFilter("category", [])}
                    />
                  </ToolbarFilter>
                </ToolbarGroup>
                <ToolbarGroup variant="filter-group">
                  <ToolbarFilter
                    chips={filters.get("levelOfEffort")}
                    deleteChip={(
                      category: string | ToolbarChipGroup,
                      chip: ToolbarChip | string
                    ) => removeFilter("levelOfEffort", chip)}
                    deleteChipGroup={() => setFilter("levelOfEffort", [])}
                    categoryName={{
                      key: "levelOfEffort",
                      name: "Level of effort",
                    }}
                  >
                    <SimpleSelect
                      maxHeight={300}
                      variant={SelectVariant.checkbox}
                      aria-label="levelOfEffort"
                      aria-labelledby="levelOfEffort"
                      placeholderText="Level effort"
                      value={filters.get("levelOfEffort")?.map(toOption)}
                      options={levelOfEfforts.map(toOption)}
                      onChange={(option) => {
                        const optionValue = option as OptionWithValue<string>;

                        const elementExists = (
                          filters.get("levelOfEffort") || []
                        ).some((f) => f.key === optionValue.value);
                        let newElements: ToolbarChip[];
                        if (elementExists) {
                          newElements = (
                            filters.get("levelOfEffort") || []
                          ).filter((f) => f.key !== optionValue.value);
                        } else {
                          newElements = [
                            ...(filters.get("levelOfEffort") || []),
                            toToolbarChip(optionValue),
                          ];
                        }

                        setFilter("levelOfEffort", newElements);
                      }}
                      hasInlineFilter
                      onClear={() => setFilter("levelOfEffort", [])}
                    />
                  </ToolbarFilter>
                </ToolbarGroup>
                <ToolbarGroup variant="filter-group">
                  <ToolbarFilter
                    chips={filters.get("sourceTechnology")}
                    deleteChip={(
                      category: string | ToolbarChipGroup,
                      chip: ToolbarChip | string
                    ) => removeFilter("sourceTechnology", chip)}
                    deleteChipGroup={() => setFilter("sourceTechnology", [])}
                    categoryName={{
                      key: "sourceTechnology",
                      name: "Source",
                    }}
                  >
                    <SimpleSelect
                      maxHeight={300}
                      variant={SelectVariant.checkbox}
                      aria-label="sourceTechnology"
                      aria-labelledby="sourceTechnology"
                      placeholderText="Source"
                      value={filters.get("sourceTechnology")?.map(toOption)}
                      options={technologies.source.map(toOption)}
                      onChange={(option) => {
                        const optionValue = option as OptionWithValue<string>;

                        const elementExists = (
                          filters.get("sourceTechnology") || []
                        ).some((f) => f.key === optionValue.value);
                        let newElements: ToolbarChip[];
                        if (elementExists) {
                          newElements = (
                            filters.get("sourceTechnology") || []
                          ).filter((f) => f.key !== optionValue.value);
                        } else {
                          newElements = [
                            ...(filters.get("sourceTechnology") || []),
                            toToolbarChip(optionValue),
                          ];
                        }

                        setFilter("sourceTechnology", newElements);
                      }}
                      hasInlineFilter
                      onClear={() => setFilter("sourceTechnology", [])}
                    />
                  </ToolbarFilter>
                  <ToolbarFilter
                    chips={filters.get("targetTechnology")}
                    deleteChip={(
                      category: string | ToolbarChipGroup,
                      chip: ToolbarChip | string
                    ) => removeFilter("targetTechnology", chip)}
                    deleteChipGroup={() => setFilter("targetTechnology", [])}
                    categoryName={{
                      key: "targetTechnology",
                      name: "Target",
                    }}
                  >
                    <SimpleSelect
                      maxHeight={300}
                      variant={SelectVariant.checkbox}
                      aria-label="targetTechnology"
                      aria-labelledby="targetTechnology"
                      placeholderText="Target"
                      value={filters.get("targetTechnology")?.map(toOption)}
                      options={technologies.target.map(toOption)}
                      onChange={(option) => {
                        const optionValue = option as OptionWithValue<string>;

                        const elementExists = (
                          filters.get("targetTechnology") || []
                        ).some((f) => f.key === optionValue.value);
                        let newElements: ToolbarChip[];
                        if (elementExists) {
                          newElements = (
                            filters.get("targetTechnology") || []
                          ).filter((f) => f.key !== optionValue.value);
                        } else {
                          newElements = [
                            ...(filters.get("targetTechnology") || []),
                            toToolbarChip(optionValue),
                          ];
                        }

                        setFilter("targetTechnology", newElements);
                      }}
                      hasInlineFilter
                      onClear={() => setFilter("targetTechnology", [])}
                    />
                  </ToolbarFilter>
                </ToolbarGroup>
              </>
            }
          />
        )}
      </ConditionalRender>

      <Modal
        title={`Rule: ${issueModalMappedRule?.id}`}
        isOpen={issueModal.isOpen && issueModal.action === "showRule"}
        onClose={issueModal.close}
        variant="large"
      >
        {issueModalMappedRule && (
          <RuleEditor ruleId={issueModalMappedRule.id} />
        )}
      </Modal>
      <Modal
        title={`File ${fileModalMappedFile?.prettyPath}`}
        isOpen={fileModal.isOpen && fileModal.action === "showFile"}
        onClose={fileModal.close}
        variant="default"
        position="top"
        disableFocusTrap
        actions={[
          <Button key="close" variant="primary" onClick={fileModal.close}>
            Close
          </Button>,
        ]}
      >
        {fileModalMappedFile && <FileEditor file={fileModalMappedFile} />}
      </Modal>
    </>
  );
};
