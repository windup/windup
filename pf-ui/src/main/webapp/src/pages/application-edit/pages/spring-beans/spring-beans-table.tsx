import React, { useMemo, useState } from "react";

import { Button, ButtonVariant, Modal } from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";

import { SpringBeanDto } from "@app/api/spring-beans";
import { useFilesQuery } from "@app/queries/files";
import { useSpringBeansQuery } from "@app/queries/spring-beans";
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
];

export interface ISpringBeansTableProps {
  applicationId: string;
}

export const SpringBeansTable: React.FC<ISpringBeansTableProps> = ({
  applicationId,
}) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFilesQuery = useFilesQuery();
  const allSpringBeansQuery = useSpringBeansQuery();

  const beans = useMemo(() => {
    return (
      allSpringBeansQuery.data?.find((f) => f.applicationId === applicationId)
        ?.beans || []
    );
  }, [allSpringBeansQuery.data, applicationId]);

  // file editor
  const fileModal = useModal<"showFile", string>();
  const fileModalMappedFile = useMemo(() => {
    return allFilesQuery.data?.find((file) => file.id === fileModal.data);
  }, [allFilesQuery.data, fileModal.data]);

  // Rows
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<SpringBeanDto>({
    items: beans,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: SpringBeanDto[]) => {
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
        isLoading={allSpringBeansQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allSpringBeansQuery.isError}
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
