import React, { useMemo, useState } from "react";

import { Button, ButtonVariant, Modal } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";

import { JaxRsServiceDto } from "@app/api/remote-services";
import { useFilesQuery } from "@app/queries/files";
import { useRemoteServicesQuery } from "@app/queries/remote-services";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Service path",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Implementation",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IJaxRsTableProps {
  applicationId: string;
}

export const JaxRsTable: React.FC<IJaxRsTableProps> = ({ applicationId }) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFiles = useFilesQuery();
  const allRemoteServicesQuery = useRemoteServicesQuery();

  const beans = useMemo(() => {
    return (
      allRemoteServicesQuery.data?.find(
        (f) => f.applicationId === applicationId
      )?.jaxRsServices || []
    );
  }, [allRemoteServicesQuery.data, applicationId]);

  // file editor
  const fileModal = useModal<"showFile", string>();
  const fileModalMappedFile = useMemo(() => {
    return allFiles.data?.find((file) => file.id === fileModal.data);
  }, [allFiles.data, fileModal.data]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<JaxRsServiceDto>({
    items: beans,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: JaxRsServiceDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.path,
          },
          {
            title: item.interfaceFileId ? (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() => fileModal.open("showFile", item.interfaceFileId)}
              >
                {item.interfaceName}
              </Button>
            ) : (
              item.interfaceName
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
        isLoading={allRemoteServicesQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allRemoteServicesQuery.isError}
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
