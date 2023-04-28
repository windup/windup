import React, { useCallback, useMemo, useState } from "react";

import {
  Badge,
  Button,
  Flex,
  FlexItem,
  Label,
  List,
  ListComponent,
  ListItem,
  Modal,
  OrderType,
  Truncate,
} from "@patternfly/react-core";
import { IAction, ICell, IRow, cellWidth } from "@patternfly/react-table";

import { ApplicationFileDto } from "@app/api/application-details";
import { FileDto } from "@app/api/file";
import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { useFilesQuery } from "@app/queries/files";
import { useIssuesQuery } from "@app/queries/issues";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Name",
    transforms: [cellWidth(40)],
    cellTransforms: [],
  },
  {
    title: "Tags",
    transforms: [cellWidth(20)],
    cellTransforms: [],
  },
  {
    title: "Issues",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Story points",
    transforms: [],
    cellTransforms: [],
  },
];

export interface IApplicationFilesChildrenTableProps {
  applicationFile: ApplicationFileDto;
}

export const ApplicationFilesChildrenTable: React.FC<
  IApplicationFilesChildrenTableProps
> = ({ applicationFile }) => {
  const fileModal = useModal<"showFile", FileDto>();

  // Filters
  const [filterText] = useState("");

  //
  const allIssuesQuery = useIssuesQuery();
  const allFilesQuery = useFilesQuery();
  const applicationFiles: FileDto[] = useMemo(() => {
    return applicationFile.childrenFileIds.map((childFileId) => {
      const defaultFile: FileDto = {
        id: childFileId,
        fullPath: "",
        prettyPath: "",
        prettyFileName: "",
        hints: [],
        tags: [],
        classificationsAndHintsTags: [],
        sourceType: "",
        storyPoints: -1,
      };
      const file: FileDto | undefined = allFilesQuery.data?.find(
        (file) => file.id === childFileId
      );
      return { ...defaultFile, ...file };
    });
  }, [allFilesQuery.data, applicationFile]);

  const findIssuesByFileId = useCallback(
    (fileId: string) => {
      return (allIssuesQuery.data || [])
        .filter((f) => f.applicationId !== ALL_APPLICATIONS_ID)
        .flatMap((f) => f.issues)
        .filter((issue) => {
          return issue.affectedFiles
            .flatMap((f) => f.files)
            .find((affectedFile) => affectedFile.fileId === fileId);
        });
    },
    [allIssuesQuery.data]
  );

  //
  const {
    page: currentPage,
    sortBy: currentSortBy,
    changePage: onPageChange,
    changeSortBy: onChangeSortBy,
  } = useTableControls();

  const { pageItems, filteredItems } = useTable<FileDto>({
    items: applicationFiles,
    currentPage: currentPage,
    currentSortBy: currentSortBy,
    compareToByColumn: () => 0,
    filterItem: (item) => true,
  });

  const itemsToRow = (items: FileDto[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const issues = findIssuesByFileId(item.id) || [];

      rows.push({
        [DataKey]: item,
        cells: [
          {
            title: (
              <Button
                variant="link"
                onClick={() => fileModal.open("showFile", item)}
              >
                <Truncate content={item.prettyFileName} />
              </Button>
            ),
          },
          {
            title: (
              <Flex>
                {item.tags.map((t) => (
                  <FlexItem key={t.name} spacer={{ default: "spacerXs" }}>
                    <Label
                      color={t.level === "IMPORTANT" ? "red" : "grey"}
                      isCompact
                    >
                      {t.name}
                    </Label>
                  </FlexItem>
                ))}
                {item.classificationsAndHintsTags.map((t) => (
                  <FlexItem key={t} spacer={{ default: "spacerXs" }}>
                    <Label color="grey" isCompact>
                      {t}
                    </Label>
                  </FlexItem>
                ))}
              </Flex>
            ),
          },
          {
            title: (
              <List component={ListComponent.ol} type={OrderType.number}>
                {issues.map((f, index) => {
                  const totalOcurrences = f.affectedFiles
                    .flatMap((f) => f.files)
                    .filter((f) => f.fileId === item.id)
                    .reduce((prev, current) => prev + current.occurrences, 0);

                  return (
                    <ListItem key={f.name}>
                      {f.name} <Badge isRead>{totalOcurrences}</Badge>
                    </ListItem>
                  );
                })}
              </List>
            ),
          },
          {
            title: item.storyPoints,
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
        variant="compact"
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
        isLoading={allFilesQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allFilesQuery.isError}
        // Toolbar filters
        filtersApplied={filterText.trim().length > 0}
      />

      <Modal
        title={`File ${fileModal?.data?.prettyFileName}`}
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
        {fileModal.data && <FileEditor file={fileModal.data} />}
      </Modal>
    </>
  );
};
