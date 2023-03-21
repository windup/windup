import React, { useMemo, useState } from "react";

import {
  Button,
  ButtonVariant,
  Flex,
  FlexItem,
  Modal,
} from "@patternfly/react-core";
import { IAction, ICell, IRow } from "@patternfly/react-table";


import { SessionBeanDto } from "@app/api/ejb";
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
    title: "Interface",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Implementation",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "JNDI location",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IStatelessSessionBeansTableProps {
  applicationId: string;
  sessionBeanType: "STATELESS" | "STATEFUL";
}

export const StatelessSessionBeansTable: React.FC<
  IStatelessSessionBeansTableProps
> = ({ applicationId, sessionBeanType }) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFiles = useFilesQuery();
  const allEJBsQuery = useEJBsQuery();

  const beans = useMemo(() => {
    return (
      allEJBsQuery.data
        ?.find((f) => f.applicationId === applicationId)
        ?.sessionBeans.filter((f) => f.type === sessionBeanType) || []
    );
  }, [allEJBsQuery.data, applicationId, sessionBeanType]);

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

  const { pageItems, filteredItems } = useTable<SessionBeanDto>({
    items: beans,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: SessionBeanDto[]) => {
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
            title: (
              <Flex>
                {item?.homeEJBFileId && (
                  <FlexItem>
                    <Button
                      variant={ButtonVariant.tertiary}
                      isSmall
                      onClick={() =>
                        fileModal.open("showFile", item?.homeEJBFileId)
                      }
                    >
                      Home
                    </Button>
                  </FlexItem>
                )}
                {item?.localEJBFileId && (
                  <FlexItem>
                    <Button
                      variant={ButtonVariant.tertiary}
                      isSmall
                      onClick={() =>
                        fileModal.open("showFile", item?.localEJBFileId)
                      }
                    >
                      Local
                    </Button>
                  </FlexItem>
                )}
                {item?.remoteEJBFileId && (
                  <FlexItem>
                    <Button
                      variant={ButtonVariant.secondary}
                      isSmall
                      isDanger
                      onClick={() =>
                        fileModal.open("showFile", item?.remoteEJBFileId)
                      }
                    >
                      Remote
                    </Button>
                  </FlexItem>
                )}
              </Flex>
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
            title: item?.jndiLocation,
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
