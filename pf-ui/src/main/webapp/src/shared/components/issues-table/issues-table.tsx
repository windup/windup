import React, { useCallback, useEffect, useMemo, useState } from "react";

import { useSelectionState } from "@migtools/lib-ui";
import {
  Bullseye,
  Button,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Label,
  Modal,
  SearchInput,
  SelectVariant,
  Split,
  SplitItem,
  Title,
  ToolbarChip,
  ToolbarChipGroup,
  ToolbarFilter,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import ArrowUpIcon from "@patternfly/react-icons/dist/esm/icons/arrow-up-icon";
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
import { useDebounce } from "usehooks-ts";

import { HintDto } from "@app/api/file";
import { compareByCategoryFn, compareByEffortFn } from "@app/api/issues";
import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { IssueProcessed } from "@app/models/api-enriched";
import { useAnalysisConfigurationQuery } from "@app/queries/analysis-configuration";
import { useApplicationsQuery } from "@app/queries/applications";
import { useFilesQuery } from "@app/queries/files";
import { useIssuesQuery } from "@app/queries/issues";
import { useRulesQuery } from "@app/queries/rules";
import {
  SimpleTableWithToolbar,
  SimpleSelect,
  OptionWithValue,
  FileEditor,
  RuleEditor,
} from "@app/shared/components";
import {
  useModal,
  useTable,
  useTableControls,
  useToolbar,
} from "@app/shared/hooks";

import { IssueOverview } from "./components/issue-overview";

const areRowsEquals = (a: TableData, b: TableData) => {
  return a.id === b.id;
};

//

interface SelectedFile {
  fileId: string;
  ruleId: string;
  issueDescription?: string;
}

//

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

//

export interface TableData extends IssueProcessed {}

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
    title: "Source",
    transforms: [cellWidth(10)],
  },
  {
    title: "Target",
    transforms: [cellWidth(10)],
  },
  {
    title: "Level of effort",
    transforms: [cellWidth(15)],
    cellTransforms: [truncate],
  },
  {
    title: "Total incidents",
    transforms: [cellWidth(10), sortable],
  },
  {
    title: "Total storypoints",
    transforms: [cellWidth(10), sortable],
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
    case 6: // Total incidents
      return a.totalIncidents - b.totalIncidents;
    case 7: // Total storypoints
      return a.totalStoryPoints - b.totalStoryPoints;
    default:
      return 0;
  }
};

const getRow = (rowData: IRowData): TableData => {
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

  const debouncedFilterText = useDebounce<string>(filterText, 250);
  const debouncedFilters = useDebounce<
    Map<
      "category" | "levelOfEffort" | "sourceTechnology" | "targetTechnology",
      ToolbarChip[]
    >
  >(filters, 100);

  // Queries
  const analysisConfigurationQuery = useAnalysisConfigurationQuery();
  const allRulesQuery = useRulesQuery();
  const allApplicationsQuery = useApplicationsQuery();
  const allIssuesQuery = useIssuesQuery();
  const allFilesQuery = useFilesQuery();

  const findRuleByIssueId = useCallback(
    (issueId: string) => {
      return allRulesQuery.data?.find((elem) => elem.id === issueId);
    },
    [allRulesQuery.data]
  );

  // Modal
  const issueModal = useModal<"showRule", TableData>();
  const issueModalMappedRule = useMemo(() => {
    return issueModal.data != null
      ? findRuleByIssueId(issueModal.data.ruleId)
      : undefined;
  }, [findRuleByIssueId, issueModal.data]);

  const {
    data: fileModalData,
    isOpen: isFileModalOpen,
    action: fileModalAction,
    open: openFileModal,
    close: closeFileModal,
  } = useModal<"showFile", SelectedFile>();
  const fileModalMappedFile = useMemo(() => {
    if (allFilesQuery.data !== undefined && fileModalData !== undefined) {
      return allFilesQuery.data.find(
        (file) => file.id === fileModalData?.fileId
      );
    } else {
      return undefined;
    }
  }, [allFilesQuery.data, fileModalData]);

  const application = useMemo(() => {
    return allApplicationsQuery.data?.find((f) => f.id === applicationId);
  }, [allApplicationsQuery.data, applicationId]);

  const issues: TableData[] = useMemo(() => {
    if (
      !allApplicationsQuery.data ||
      !allIssuesQuery.data ||
      applicationId === undefined
    ) {
      return [];
    }

    return (
      allIssuesQuery.data?.find((f) => f.applicationId === applicationId)
        ?.issues || []
    );
  }, [allApplicationsQuery.data, allIssuesQuery.data, applicationId]);

  // Technologies
  const technologies = useMemo(() => {
    const sources = new Set<string>();
    const targets = new Set<string>();

    issues.forEach((elem) => {
      elem.sourceTechnologies?.forEach((e) => sources.add(e));
      elem.targetTechnologies?.forEach((e) => targets.add(e));
    });

    return { source: Array.from(sources), target: Array.from(targets) };
  }, [issues]);

  //
  const categories = useMemo(() => {
    const allCategories = (allIssuesQuery.data || [])
      .flatMap((f) => f.issues)
      .map((e) => e.category);
    return Array.from(new Set(allCategories)).sort(
      compareByCategoryFn((e) => e)
    );
  }, [allIssuesQuery.data]);

  const levelOfEfforts = useMemo(() => {
    const allLevelOfEfforts = (allIssuesQuery.data || [])
      .flatMap((f) => f.issues)
      .map((e) => e.effort.type);
    return Array.from(new Set(allLevelOfEfforts)).sort(
      compareByEffortFn((e) => e)
    );
  }, [allIssuesQuery.data]);

  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<TableData>({
    items: issues,
    isEqual: areRowsEquals,
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const filterItem = useCallback(
    (item: TableData) => {
      let isFilterTextFilterCompliant = true;
      if (debouncedFilterText && debouncedFilterText.trim().length > 0) {
        isFilterTextFilterCompliant =
          item.name.toLowerCase().indexOf(debouncedFilterText.toLowerCase()) !==
          -1;
      }

      let isCategoryFilterCompliant = true;
      const selectedCategories = debouncedFilters.get("category") || [];
      if (selectedCategories.length > 0) {
        isCategoryFilterCompliant = selectedCategories.some(
          (f) => item.category === f.key
        );
      }

      let isLevelOfEffortCompliant = true;
      const selectedLevelOfEfforts =
        debouncedFilters.get("levelOfEffort") || [];
      if (selectedLevelOfEfforts.length > 0) {
        isLevelOfEffortCompliant = selectedLevelOfEfforts.some(
          (f) => item.effort.type === f.key
        );
      }

      let isSourceCompliant = true;
      const selectedSources = debouncedFilters.get("sourceTechnology") || [];
      if (selectedSources.length > 0) {
        isSourceCompliant = selectedSources.some((f) => {
          return item.sourceTechnologies?.includes(f.key);
        });
      }

      let isTargetCompliant = true;
      const selectedTargets = debouncedFilters.get("targetTechnology") || [];
      if (selectedTargets.length > 0) {
        isTargetCompliant = selectedTargets.some((f) => {
          return item.targetTechnologies?.includes(f.key);
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
    [debouncedFilterText, debouncedFilters]
  );

  const { pageItems, filteredItems } = useTable<TableData>({
    items: issues,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: compareByColumnIndex,
    filterItem: filterItem,
  });

  const rows: IRow[] = useMemo(() => {
    const rows: IRow[] = [];
    pageItems.forEach((item) => {
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
            title: (
              <>
                <Split hasGutter>
                  {item.sourceTechnologies?.map((technology) => (
                    <SplitItem key={technology}>
                      <Label isCompact color="blue">
                        {technology}
                      </Label>
                    </SplitItem>
                  ))}
                </Split>
              </>
            ),
          },
          {
            title: (
              <>
                <Split hasGutter>
                  {item.targetTechnologies?.map((technology) => (
                    <SplitItem key={technology}>
                      <Label isCompact color="blue">
                        {technology}
                      </Label>
                    </SplitItem>
                  ))}
                </Split>
              </>
            ),
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
          [DataKey]: item,
          parent: rows.length - 1,
          fullWidth: true,
          cells: [
            {
              title: (
                <div className="pf-u-m-sm">
                  <IssueOverview
                    issue={item}
                    onShowFile={(file, issueDescription) =>
                      openFileModal("showFile", {
                        fileId: file,
                        ruleId: item.ruleId,
                        issueDescription: issueDescription,
                      })
                    }
                  />
                </div>
              ),
            },
          ],
        });
      }
    });

    return rows;
  }, [pageItems, isRowExpanded, openFileModal]);

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
    onPageChange({ page: 1, perPage: currentPage.perPage });
  }, [
    debouncedFilterText,
    debouncedFilters,
    onPageChange,
    currentPage.perPage,
  ]);

  return (
    <>
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
            rowWrapper={(props) => {
              const row = getRow(props.row as IRowData);
              const isNotExpandedRow = !props.row?.isExpanded;
              return (
                <tr key={`${row.name}${isNotExpandedRow ? "" : "-expanded"}`}>
                  {props.children}
                </tr>
              );
            }}
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
            isLoading={
              allIssuesQuery.isLoading || allApplicationsQuery.isLoading
            }
            loadingVariant="skeleton"
            fetchError={allIssuesQuery.isError}
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
                  {technologies.source.length > 0 && (
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
                  )}
                  {technologies.target.length > 0 && (
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
                  )}
                </ToolbarGroup>
                {analysisConfigurationQuery.data?.exportCSV && (
                  <>
                    <ToolbarItem variant="separator" />
                    <ToolbarItem>
                      <a
                        className="pf-c-button pf-m-primary "
                        href={
                          ALL_APPLICATIONS_ID === applicationId
                            ? "./AllIssues.csv"
                            : `${application?.name
                                .replaceAll(".", "_")
                                .replaceAll("-", "_")}.csv`
                        }
                        rel="noopener noreferrer"
                        target="_blank"
                      >
                        Export CSV
                      </a>
                    </ToolbarItem>
                  </>
                )}
              </>
            }
          />
        )}
      </>

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
        isOpen={isFileModalOpen && fileModalAction === "showFile"}
        onClose={closeFileModal}
        variant="default"
        position="top"
        disableFocusTrap
        actions={[
          <Button key="close" variant="primary" onClick={closeFileModal}>
            Close
          </Button>,
        ]}
      >
        {fileModalMappedFile && (
          <FileEditor
            file={fileModalMappedFile}
            hintToFocus={fileModalMappedFile.hints
              .filter((hint) => {
                return (
                  hint.ruleId === fileModalData?.ruleId &&
                  hint.content === fileModalData.issueDescription
                );
              })
              .reduce((prev, current) => {
                if (!prev) {
                  return current;
                }
                return current.line < prev.line ? current : prev;
              }, undefined as HintDto | undefined)}
          />
        )}
      </Modal>
    </>
  );
};
