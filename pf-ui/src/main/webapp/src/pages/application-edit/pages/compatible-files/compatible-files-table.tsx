import React, { useMemo, useState } from "react";

import { Button, ButtonVariant, Modal } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";

import { useCompatibleFilesQuery } from "@app/queries/compatible-files";
import { useFilesQuery } from "@app/queries/files";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

export interface TableData {
  artifactName: string;
  fileName: string;
  fileId?: string;
}

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Artifact",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Name",
    transforms: [],
    cellTransforms: [],
  },
];

export interface ICompatibleFilesTableProps {
  applicationId: string;
}

export const CompatibleFilesTable: React.FC<ICompatibleFilesTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFilesQuery = useFilesQuery();
  const allCompatibleFilesQuery = useCompatibleFilesQuery();

  const tableData = useMemo(() => {
    return (
      allCompatibleFilesQuery.data
        ?.find((f) => f.applicationId === applicationId)
        ?.artifacts.flatMap((artifact) => {
          return artifact.files.map((f) => {
            const data: TableData = {
              artifactName: artifact.name,
              fileName: f.fileName,
              fileId: f.fileId,
            };
            return data;
          });
        }) || []
    );
  }, [allCompatibleFilesQuery.data, applicationId]);

  // file editor
  const fileModal = useModal<"showFile", TableData>();
  const fileModalMappedFile = useMemo(() => {
    return allFilesQuery.data?.find(
      (file) => file.id === fileModal.data?.fileId
    );
  }, [allFilesQuery.data, fileModal.data]);

  // Rows
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
    filterItem: (item) => true,
  });

  const itemsToRow = (items: TableData[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const sourceFile = allFilesQuery.data?.find((f) => f.id === item.fileId);
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.artifactName,
          },
          {
            title: sourceFile ? (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() => fileModal.open("showFile", item)}
              >
                {item.fileName}
              </Button>
            ) : (
              item.fileName
            ),
          },
        ],
      });
    });

    return rows;
  };

  const rows: IRow[] = itemsToRow(pageItems);
  const actions: IAction[] = [];

  return (
    <>
      <SimpleTableWithToolbar
        hasTopPagination
        hasBottomPagination
        totalCount={filteredItems.length}
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
        isLoading={allCompatibleFilesQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allCompatibleFilesQuery.isError}
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
        {fileModalMappedFile && <FileEditor file={fileModalMappedFile} />}
      </Modal>
    </>
  );
};
