import React, { useMemo, useState, useEffect } from "react";

import { useSelectionState } from "@migtools/lib-ui";
import {
  Button,
  ButtonVariant,
  DescriptionList,
  DescriptionListDescription,
  DescriptionListGroup,
  DescriptionListTerm,
  List,
  ListComponent,
  ListItem,
  Modal,
  OrderType,
  SearchInput,
  ToolbarItem,
} from "@patternfly/react-core";
import {
  IAction,
  ICell,
  IRow,
  IRowData,
  cellWidth,
  sortable,
  truncate,
} from "@patternfly/react-table";


import { JBPMDto } from "@app/api/jbpm";
import { useFilesQuery } from "@app/queries/files";
import { useJBPMsQuery } from "@app/queries/jbpm";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Name",
    transforms: [cellWidth(100), sortable],
    cellTransforms: [truncate],
  },
];

const getRow = (rowData: IRowData): JBPMDto => {
  return rowData[DataKey];
};

export interface IJBPMTableProps {
  applicationId?: string;
}

export const JBPMTable: React.FC<IJBPMTableProps> = ({ applicationId }) => {
  // Filters
  const [filterText, setFilterText] = useState("");

  // Queries
  const allFilesQuery = useFilesQuery();
  const allJBPMs = useJBPMsQuery();

  const jbpms = useMemo(() => {
    return (
      allJBPMs.data?.find((f) => f.applicationId === applicationId)?.jbpms || []
    );
  }, [allJBPMs.data, applicationId]);

  // file editor
  const fileModal = useModal<"showFile", string>();
  const fileModalMappedFile = useMemo(() => {
    return allFilesQuery.data?.find((file) => file.id === fileModal.data);
  }, [allFilesQuery.data, fileModal.data]);

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<JBPMDto>({
    items: jbpms,
    isEqual: (a, b) => {
      return a.fileName === b.fileName && a.processName === b.processName;
    },
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<JBPMDto>({
    items: jbpms,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: JBPMDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const isExpanded = isRowExpanded(item);

      rows.push({
        [DataKey]: item,
        isOpen: isExpanded,
        cells: [
          {
            title: item.fileId ? (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() => fileModal.open("showFile", item.fileId)}
              >
                {item.fileName}
              </Button>
            ) : (
              item.fileName
            ),
          },
          {
            title: item.processName,
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
                      <DescriptionListTerm>Nodes</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.processNoteCount}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Decisions</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.processDecisionCount}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>States</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.processStateCount}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Tasks</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.processTaskCount}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Sub processes</DescriptionListTerm>
                      <DescriptionListDescription>
                        {item.processSubProcessCount}
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>Action handlers</DescriptionListTerm>
                      <DescriptionListDescription>
                        <List
                          component={ListComponent.ol}
                          type={OrderType.number}
                        >
                          {item.actionHandlers.map((elem, index) => (
                            <ListItem key={index}>
                              {elem.fileId ? (
                                <Button
                                  variant={ButtonVariant.link}
                                  isInline
                                  onClick={() =>
                                    fileModal.open("showFile", elem.fileId)
                                  }
                                >
                                  {elem.fileName}
                                </Button>
                              ) : (
                                elem.fileName
                              )}
                            </ListItem>
                          ))}
                        </List>
                      </DescriptionListDescription>
                    </DescriptionListGroup>
                    <DescriptionListGroup>
                      <DescriptionListTerm>
                        Decision handlers
                      </DescriptionListTerm>
                      <DescriptionListDescription>
                        <List
                          component={ListComponent.ol}
                          type={OrderType.number}
                        >
                          {item.decisionHandlers.map((elem, index) => (
                            <ListItem key={index}>
                              {elem.fileId ? (
                                <Button
                                  variant={ButtonVariant.link}
                                  isInline
                                  onClick={() =>
                                    fileModal.open("showFile", elem.fileId)
                                  }
                                >
                                  {elem.fileName}
                                </Button>
                              ) : (
                                elem.fileName
                              )}
                            </ListItem>
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
        isLoading={allJBPMs.isFetching}
        loadingVariant="skeleton"
        fetchError={allJBPMs.isError}
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
