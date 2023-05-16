import React, { useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";

import { useSelectionState } from "@migtools/lib-ui";
import {
  Button,
  ButtonVariant,
  ExpandableSection,
  Modal,
  PageSection,
} from "@patternfly/react-core";
import {
  IRow,
  ICell,
  IRowData,
  IAction,
  TableComposable,
  Tbody,
  Tr,
  Td,
} from "@patternfly/react-table";

import { ApplicationDto } from "@app/api/application";
import { TransactionDto } from "@app/api/transactions";
import { useFilesQuery } from "@app/queries/files";
import { useTransactionsQuery } from "@app/queries/transactions";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

interface SelectedFile {
  fileId: string;
  lineToFocus: number;
}

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Entry class",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Entry method",
    transforms: [],
    cellTransforms: [],
  },
];

const getRow = (rowData: IRowData): TransactionDto => {
  return rowData[DataKey];
};

export const Transactions: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  // Filters
  const [filterText] = useState("");

  // Queries
  const allFilesQuery = useFilesQuery();
  const allTransactionsQuery = useTransactionsQuery();

  const tableData = useMemo(() => {
    return (
      allTransactionsQuery.data?.find(
        (f) => f.applicationId === application?.id
      )?.transactions || []
    );
  }, [allTransactionsQuery.data, application]);

  // file editor
  const fileModal = useModal<"showFile", SelectedFile>();
  const fileModalMappedFile = useMemo(() => {
    return allFilesQuery.data?.find(
      (file) => file.id === fileModal.data?.fileId
    );
  }, [allFilesQuery.data, fileModal.data]);

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<TransactionDto>({
    items: tableData,
    isEqual: (a, b) =>
      `${a.className}.${a.methodName}` === `${b.className}.${b.methodName}`,
  });

  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<TransactionDto>({
    items: tableData,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: () => true,
  });

  const itemsToRow = (items: TransactionDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const isExpanded = isRowExpanded(item);

      rows.push({
        [DataKey]: item,
        isOpen: isExpanded,
        cells: [
          {
            title: item.className,
          },
          {
            title: item.methodName,
          },
        ],
      });

      // Expanded area
      if (isExpanded) {
        rows.push({
          parent: rows.length - 1,
          noPadding: true,
          cells: [
            {
              title: (
                <TableComposable>
                  <Tbody>
                    {item.stackTraces.map((stackTrace, index) => {
                      const shortClassName = item.className
                        .split(".")
                        .slice(-1);
                      const text = `${item.className}.${item.methodName}(${shortClassName}, line ${stackTrace.lineNumber})`;
                      return (
                        <Tr key={index} style={{ border: "transparent" }}>
                          <Td>
                            <ExpandableSection
                              isIndented
                              toggleText={stackTrace.sql}
                            >
                              {item.classFileId ? (
                                <Button
                                  variant={ButtonVariant.link}
                                  isInline
                                  onClick={() =>
                                    fileModal.open("showFile", {
                                      fileId: item.classFileId!,
                                      lineToFocus: stackTrace.lineNumber || 0,
                                    })
                                  }
                                >
                                  {text}
                                </Button>
                              ) : (
                                text
                              )}
                            </ExpandableSection>
                          </Td>
                        </Tr>
                      );
                    })}
                  </Tbody>
                </TableComposable>
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
    <PageSection>
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
        isLoading={allTransactionsQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allTransactionsQuery.isError}
        // Toolbar filters
        filtersApplied={filterText.trim().length > 0}
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
        {fileModalMappedFile && (
          <FileEditor
            file={fileModalMappedFile}
            lineToFocus={fileModal.data?.lineToFocus}
          />
        )}
      </Modal>
    </PageSection>
  );
};
