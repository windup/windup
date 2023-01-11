import React, { useMemo, useState } from "react";

import {
  Bullseye,
  Button,
  ButtonVariant,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Modal,
  Spinner,
  Title,
} from "@patternfly/react-core";
import { ArrowUpIcon } from "@patternfly/react-icons";
import { IAction, ICell, IRow } from "@patternfly/react-table";
import {
  ConditionalRender,
  SimpleTableWithToolbar,
  useModal,
  useTable,
  useTableControls,
} from "@project-openubl/lib-ui";

import { FileEditor } from "@app/shared/components";

import { useEJBsQuery } from "@app/queries/ejb";
import { BeanDto } from "@app/api/application-ejb";
import { useFilesQuery } from "@app/queries/files";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "MDB name",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Class",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "JMS destination",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IMessageDrivenBeansTableProps {
  applicationId?: string;
}

export const MessageDrivenBeansTable: React.FC<
  IMessageDrivenBeansTableProps
> = ({ applicationId }) => {
  // Filters
  const [filterText] = useState("");

  // Queries
  const allFiles = useFilesQuery();
  const allEJBsQuery = useEJBsQuery();

  const beans = useMemo(() => {
    return (
      allEJBsQuery.data
        ?.find((f) => f.applicationId === applicationId)
        ?.beans.filter((f) => f.type === "MESSAGE_DRIVEN_BEAN") || []
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

  const { pageItems, filteredItems } = useTable<BeanDto>({
    items: beans,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: BeanDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      console.log(item);
      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: (
              <Button
                variant={ButtonVariant.link}
                onClick={() => fileModal.open("showFile", item.beanDescriptorFileId)}
              >
                {item.beanName}
              </Button>
            ),
          },
          {
            title: (
              <Button
                variant={ButtonVariant.link}
                onClick={() => fileModal.open("showFile", item.classFileId)}
              >
                {item.className}
              </Button>
            ),
          },
          {
            title: item.jmsDestination,
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
      <ConditionalRender
        when={allEJBsQuery.isLoading}
        then={
          <Bullseye>
            <Spinner />
          </Bullseye>
        }
      >
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
            hasTopPagination
            hasBottomPagination
            totalCount={filteredItems.length}
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
            isLoading={allEJBsQuery.isFetching}
            loadingVariant="skeleton"
            fetchError={allEJBsQuery.isError}
            // Toolbar filters
            filtersApplied={filterText.trim().length > 0}
          />
        )}
      </ConditionalRender>
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
