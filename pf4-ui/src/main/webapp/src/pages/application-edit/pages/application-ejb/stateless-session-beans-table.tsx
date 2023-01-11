import React, { useMemo, useState } from "react";

import {
  Bullseye,
  Button,
  ButtonVariant,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  Flex,
  FlexItem,
  List,
  ListItem,
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
  applicationId?: string;
  sessionBeanType: "STATELESS_SESSION_BEAN" | "STATEFUL_SESSION_BEAN";
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
        ?.beans.filter((f) => f.type === sessionBeanType) || []
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
            title: item.beanDescriptorFileId ? (
              <Button
                variant={ButtonVariant.link}
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
                {item.homeEJBFileId && (
                  <FlexItem>
                    <Button
                      variant={ButtonVariant.tertiary}
                      isSmall
                      onClick={() =>
                        fileModal.open("showFile", item.homeEJBFileId)
                      }
                    >
                      Home
                    </Button>
                  </FlexItem>
                )}
                {item.localEJBFileId && (
                  <FlexItem>
                    <Button
                      variant={ButtonVariant.tertiary}
                      isSmall
                      onClick={() =>
                        fileModal.open("showFile", item.localEJBFileId)
                      }
                    >
                      Local
                    </Button>
                  </FlexItem>
                )}
                {item.remoteEJBFileId && (
                  <FlexItem>
                    <Button
                      variant={ButtonVariant.secondary}
                      isSmall
                      isDanger
                      onClick={() =>
                        fileModal.open("showFile", item.remoteEJBFileId)
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
            title: (
              <List isPlain>
                {item.jndiLocations?.map((f, index) => (
                  <ListItem key={index}>{f}</ListItem>
                ))}
              </List>
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
