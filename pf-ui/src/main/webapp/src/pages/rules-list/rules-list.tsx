import React, { useMemo, useState } from "react";

import { useSelectionState } from "@migtools/lib-ui";
import {
  DescriptionList,
  DescriptionListDescription,
  DescriptionListGroup,
  DescriptionListTerm,
  Modal,
  PageSection,
  PageSectionVariants,
  SearchInput,
  SelectVariant,
  Text,
  TextContent,
  ToolbarChip,
  ToolbarChipGroup,
  ToolbarFilter,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import {
  ICell,
  IExtraData,
  IRow,
  IRowData,
  IAction,
} from "@patternfly/react-table";

import { RuleProcessed } from "@app/models/api-enriched";
import { useRulesQuery } from "@app/queries/rules";
import {
  SimpleTableWithToolbar,
  OptionWithValue,
  SimpleSelect,
  RuleEditor,
} from "@app/shared/components";
import {
  useModal,
  useTable,
  useTableControls,
  useToolbar,
} from "@app/shared/hooks";

export interface TableData {
  phase: string;
  properties: { [key: string]: string };
}

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

const STATUS: ToolbarChip[] = [
  {
    key: "true",
    node: "Condition met",
  },
  {
    key: "false",
    node: "Condition not met",
  },
];

const RESULT: ToolbarChip[] = [
  {
    key: "true",
    node: "Success",
  },
  {
    key: "false",
    node: "Failed",
  },
];

//

const DataKey = "DataKey";

const columns: ICell[] = [
  { title: "Phase", transforms: [] },
  { title: "Ruleset", transforms: [] },
  { title: "ID", transforms: [] },
  {
    title: "Status",
    transforms: [],
  },
  {
    title: "Result",
    transforms: [],
    cellTransforms: [],
  },
];

const getRow = (rowData: IRowData): RuleProcessed => {
  return rowData[DataKey];
};

export const RulesList: React.FC = () => {
  // Queries
  const allRulesQuery = useRulesQuery();

  // Filters
  const [filterText, setFilterText] = useState("");
  const { filters, setFilter, removeFilter, clearAllFilters } = useToolbar<
    "phase" | "ruleset" | "status" | "result",
    ToolbarChip
  >();

  const phases = useMemo(() => {
    const phases = new Set<string>();
    allRulesQuery.data?.forEach((f) => phases.add(f.phase));
    return Array.from(phases).sort((a, b) => a.localeCompare(b));
  }, [allRulesQuery.data]);

  const ruleSets = useMemo(() => {
    const ruleSets = new Set<string>();
    allRulesQuery.data?.forEach((f) => ruleSets.add(f.ruleSetId));
    return Array.from(ruleSets).sort((a, b) => a.localeCompare(b));
  }, [allRulesQuery.data]);

  // Modal
  const ruleModal = useModal<"showRule", RuleProcessed>();

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<RuleProcessed>({
    items: allRulesQuery.data || [],
    isEqual: (a, b) => `${a.phase}-${a.id}` === `${b.phase}-${b.id}`,
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<RuleProcessed>({
    items: allRulesQuery.data || [],
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => {
      let isFilterTextFilterCompliant = true;
      if (filterText && filterText.trim().length > 0) {
        isFilterTextFilterCompliant =
          item.id.toLowerCase().indexOf(filterText.toLowerCase()) !== -1;
      }

      let isPhaseCompliant = true;
      const selectedPhases = filters.get("phase") || [];
      if (selectedPhases.length > 0) {
        isPhaseCompliant = selectedPhases.some((f) => item.phase === f.key);
      }

      let isRulesetCompliant = true;
      const selectedRulesets = filters.get("ruleset") || [];
      if (selectedRulesets.length > 0) {
        isRulesetCompliant = selectedRulesets.some(
          (f) => item.ruleSetId === f.key
        );
      }

      let isStatusCompliant = true;
      const selectedStatus = filters.get("status") || [];
      if (selectedStatus.length > 0) {
        isStatusCompliant = selectedStatus.some(
          (f) => item.executed === (f.key === "true")
        );
      }

      let isResultCompliant = true;
      const selectedResult = filters.get("result") || [];
      if (selectedResult.length > 0) {
        isResultCompliant = selectedResult.some(
          (f) => item.failed === (f.key === "false")
        );
      }

      return (
        isFilterTextFilterCompliant &&
        isPhaseCompliant &&
        isRulesetCompliant &&
        isStatusCompliant &&
        isResultCompliant
      );
    },
  });

  const itemsToRow = (items: RuleProcessed[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const isExpanded = isRowExpanded(item);

      rows.push({
        [DataKey]: item,
        isOpen: isExpanded,

        cells: [
          {
            title: item.phase,
          },
          {
            title: item.ruleSetId,
          },
          {
            title: item.id,
          },
          {
            title: item.executed ? "Condition met" : "Condition not met",
          },
          {
            title: item.failed ? "Failed" : "Success",
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
                  <DescriptionList isCompact isHorizontal>
                    <DescriptionListGroup>
                      <DescriptionListTerm>
                        Vertices created
                      </DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.verticesAdded}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>
                        Vertices removed
                      </DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.verticesRemoved}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Edges created</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.edgesAdded}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Edges removed</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.edgesRemoved}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Failure cause</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.failureMessage}
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
        ruleModal.open("showRule", row);
      },
    },
  ];

  return (
    <>
      <PageSection variant={PageSectionVariants.light}>
        <TextContent>
          <Text component="h1">Rule providers execution overview</Text>
          <Text component="small">
            This report lists 'rule providers', or sets of Windup rules. They
            may originate from a '.windup.xml', a '.rhamt.xml', or a '.mta.xml'
            file or a Java class implementing 'RuleProvider'.
          </Text>
        </TextContent>
      </PageSection>

      <PageSection variant={PageSectionVariants.default}>
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
          isLoading={allRulesQuery.isFetching}
          loadingVariant="skeleton"
          fetchError={allRulesQuery.isError}
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
                  chips={filters.get("phase")}
                  deleteChip={(
                    category: string | ToolbarChipGroup,
                    chip: ToolbarChip | string
                  ) => removeFilter("phase", chip)}
                  deleteChipGroup={() => setFilter("phase", [])}
                  categoryName={{ key: "phase", name: "Phase" }}
                >
                  <SimpleSelect
                    maxHeight={300}
                    variant={SelectVariant.checkbox}
                    aria-label="phase"
                    aria-labelledby="phase"
                    placeholderText="Phase"
                    value={filters.get("phase")?.map(toOption)}
                    options={phases.map(toOption)}
                    onChange={(option) => {
                      const optionValue = option as OptionWithValue<string>;

                      const elementExists = (filters.get("phase") || []).some(
                        (f) => f.key === optionValue.value
                      );
                      let newElements: ToolbarChip[];
                      if (elementExists) {
                        newElements = (filters.get("phase") || []).filter(
                          (f) => f.key !== optionValue.value
                        );
                      } else {
                        newElements = [
                          ...(filters.get("phase") || []),
                          toToolbarChip(optionValue),
                        ];
                      }

                      setFilter("phase", newElements);
                    }}
                    hasInlineFilter
                    onClear={() => setFilter("phase", [])}
                  />
                </ToolbarFilter>
                <ToolbarFilter
                  chips={filters.get("ruleset")}
                  deleteChip={(
                    category: string | ToolbarChipGroup,
                    chip: ToolbarChip | string
                  ) => removeFilter("ruleset", chip)}
                  deleteChipGroup={() => setFilter("ruleset", [])}
                  categoryName={{ key: "ruleset", name: "Ruleset" }}
                >
                  <SimpleSelect
                    maxHeight={300}
                    variant={SelectVariant.checkbox}
                    aria-label="ruleset"
                    aria-labelledby="ruleset"
                    placeholderText="Ruleset"
                    value={filters.get("ruleset")?.map(toOption)}
                    options={ruleSets.map(toOption)}
                    onChange={(option) => {
                      const optionValue = option as OptionWithValue<string>;

                      const elementExists = (filters.get("ruleset") || []).some(
                        (f) => f.key === optionValue.value
                      );
                      let newElements: ToolbarChip[];
                      if (elementExists) {
                        newElements = (filters.get("ruleset") || []).filter(
                          (f) => f.key !== optionValue.value
                        );
                      } else {
                        newElements = [
                          ...(filters.get("ruleset") || []),
                          toToolbarChip(optionValue),
                        ];
                      }

                      setFilter("ruleset", newElements);
                    }}
                    hasInlineFilter
                    onClear={() => setFilter("ruleset", [])}
                  />
                </ToolbarFilter>
              </ToolbarGroup>
              <ToolbarGroup variant="filter-group">
                <ToolbarFilter
                  chips={filters.get("status")}
                  deleteChip={(
                    category: string | ToolbarChipGroup,
                    chip: ToolbarChip | string
                  ) => removeFilter("status", chip)}
                  deleteChipGroup={() => setFilter("status", [])}
                  categoryName={{ key: "status", name: "Status" }}
                >
                  <SimpleSelect
                    maxHeight={300}
                    variant={SelectVariant.checkbox}
                    aria-label="status"
                    aria-labelledby="status"
                    placeholderText="Status"
                    value={filters.get("status")?.map(toOption)}
                    options={STATUS.map(toOption)}
                    onChange={(option) => {
                      const optionValue = option as OptionWithValue<string>;

                      const elementExists = (filters.get("status") || []).some(
                        (f) => f.key === optionValue.value
                      );
                      let newElements: ToolbarChip[];
                      if (elementExists) {
                        newElements = (filters.get("status") || []).filter(
                          (f) => f.key !== optionValue.value
                        );
                      } else {
                        newElements = [
                          ...(filters.get("status") || []),
                          toToolbarChip(optionValue),
                        ];
                      }

                      setFilter("status", newElements);
                    }}
                    hasInlineFilter
                    onClear={() => setFilter("status", [])}
                  />
                </ToolbarFilter>
                <ToolbarFilter
                  chips={filters.get("result")}
                  deleteChip={(
                    category: string | ToolbarChipGroup,
                    chip: ToolbarChip | string
                  ) => removeFilter("result", chip)}
                  deleteChipGroup={() => setFilter("result", [])}
                  categoryName={{ key: "result", name: "Result" }}
                >
                  <SimpleSelect
                    maxHeight={300}
                    variant={SelectVariant.checkbox}
                    aria-label="result"
                    aria-labelledby="result"
                    placeholderText="Result"
                    value={filters.get("result")?.map(toOption)}
                    options={RESULT.map(toOption)}
                    onChange={(option) => {
                      const optionValue = option as OptionWithValue<string>;

                      const elementExists = (filters.get("result") || []).some(
                        (f) => f.key === optionValue.value
                      );
                      let newElements: ToolbarChip[];
                      if (elementExists) {
                        newElements = (filters.get("result") || []).filter(
                          (f) => f.key !== optionValue.value
                        );
                      } else {
                        newElements = [
                          ...(filters.get("result") || []),
                          toToolbarChip(optionValue),
                        ];
                      }

                      setFilter("result", newElements);
                    }}
                    hasInlineFilter
                    onClear={() => setFilter("result", [])}
                  />
                </ToolbarFilter>
              </ToolbarGroup>
            </>
          }
        />
      </PageSection>

      <Modal
        title={`Rule: ${ruleModal.data?.id}`}
        isOpen={ruleModal.isOpen && ruleModal.action === "showRule"}
        onClose={ruleModal.close}
        variant="large"
      >
        {ruleModal.data && <RuleEditor ruleId={ruleModal.data.id} />}
      </Modal>
    </>
  );
};
