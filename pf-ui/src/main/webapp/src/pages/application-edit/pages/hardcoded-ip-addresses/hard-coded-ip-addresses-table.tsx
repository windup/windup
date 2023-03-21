import React, { useMemo, useState } from "react";

import { Button, ButtonVariant, Modal } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";


import { FileDto } from "@app/api/hardcoded-ip-addresses";
import { useFilesQuery } from "@app/queries/files";
import { useHardcodedIpAddressesQuery } from "@app/queries/hardcoded-ip-addresses";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "File",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Location",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "IP address",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IHardcodedIpAddressesTableProps {
  applicationId: string;
}

export const HardcodedIpAddressesTable: React.FC<
  IHardcodedIpAddressesTableProps
> = ({ applicationId }) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFilesQuery = useFilesQuery();
  const allHardcodedIpAddressesQuery = useHardcodedIpAddressesQuery();

  const harcodedFiles = useMemo(() => {
    return (
      allHardcodedIpAddressesQuery.data?.find(
        (f) => f.applicationId === applicationId
      )?.files || []
    );
  }, [allHardcodedIpAddressesQuery.data, applicationId]);

  // file editor
  const fileModal = useModal<"showFile", FileDto>();
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

  const { pageItems, filteredItems } = useTable<FileDto>({
    items: harcodedFiles,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: FileDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const sourceFile = allFilesQuery.data?.find((f) => f.id === item.fileId);
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() => fileModal.open("showFile", item)}
              >
                {sourceFile?.prettyFileName}
              </Button>
            ),
          },
          {
            title: `Line: ${item.lineNumber}, Position: ${item.columnNumber}`,
          },
          { title: item.ipAddress },
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
        isLoading={allHardcodedIpAddressesQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allHardcodedIpAddressesQuery.isError}
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
            lineToFocus={fileModal.data?.lineNumber}
          />
        )}
      </Modal>
    </>
  );
};
