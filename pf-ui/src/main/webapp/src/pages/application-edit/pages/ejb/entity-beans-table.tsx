import React, { useMemo, useState } from "react";

import { Button, ButtonVariant, Modal } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";

import { EntityBeanDto } from "@app/api/ejb";
import { useEJBsQuery } from "@app/queries/ejb";
import { useFilesQuery } from "@app/queries/files";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Bean name",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Class",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Table",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Persistence type",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IEntityBeanTableProps {
  applicationId: string;
}

export const EntityBeanTable: React.FC<IEntityBeanTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFiles = useFilesQuery();
  const allEJBsQuery = useEJBsQuery();

  const beans = useMemo(() => {
    return (
      allEJBsQuery.data?.find((f) => f.applicationId === applicationId)
        ?.entityBeans || []
    );
  }, [allEJBsQuery.data, applicationId]);

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

  const { pageItems, filteredItems } = useTable<EntityBeanDto>({
    items: beans,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: EntityBeanDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: item.beanDescriptorFileId ? (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() =>
                  fileModal.open("showFile", item.beanDescriptorFileId)
                }
              >
                {item.beanName}
              </Button>
            ) : (
              item.beanName
            ),
          },
          {
            title: item.classFileId ? (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() => fileModal.open("showFile", item.classFileId)}
              >
                {item.className}
              </Button>
            ) : (
              item.className
            ),
          },
          {
            title: item?.tableName,
          },
          {
            title: item?.persistenceType,
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
        isLoading={allEJBsQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allEJBsQuery.isError}
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
