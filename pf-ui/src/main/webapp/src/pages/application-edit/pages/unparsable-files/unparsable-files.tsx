import React, { useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";

import { useSelectionState } from "@migtools/lib-ui";
import {
  Button,
  ButtonVariant,
  Modal,
  PageSection,
} from "@patternfly/react-core";
import { IAction, ICell, IRow, IRowData } from "@patternfly/react-table";

import { ApplicationDto } from "@app/api/application";
import { useFilesQuery } from "@app/queries/files";
import { useUnparsableFilesQuery } from "@app/queries/unparsable-files";
import { SimpleTableWithToolbar, FileEditor } from "@app/shared/components";
import { useModal, useTable, useTableControls } from "@app/shared/hooks";

const DataKey = "DataKey";

const columns: ICell[] = [
  {
    title: "Artifact",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "File",
    transforms: [],
    cellTransforms: [],
  },
  {
    title: "Path",
    transforms: [],
    cellTransforms: [],
  },
];

const getRow = (rowData: IRowData): TableData => {
  return rowData[DataKey];
};

export interface TableData {
  artifactName: string;
  fileName: string;
  filePath: string;
  fileId?: string;
  description?: string;
}

export const UnparsableFiles: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  // Filters
  const [filterText] = useState("");

  // Queries
  const allFilesQuery = useFilesQuery();
  const allUnparsableFilesQuery = useUnparsableFilesQuery();

  const tableData = useMemo(() => {
    return (
      allUnparsableFilesQuery.data
        ?.find((f) => f.applicationId === application?.id)
        ?.subProjects.flatMap((subProject) => {
          return subProject.unparsableFiles.map((unparsableFile) => {
            const data: TableData = {
              artifactName: subProject.path,
              filePath: unparsableFile.filePath,
              fileName: unparsableFile.fileName,
              fileId: unparsableFile.fileId,
              description: unparsableFile.parseError,
            };
            return data;
          });
        }) || []
    );
  }, [allUnparsableFilesQuery.data, application]);

  // file editor
  const fileModal = useModal<"showFile", string>();
  const fileModalMappedFile = useMemo(() => {
    return allFilesQuery.data?.find((file) => file.id === fileModal.data);
  }, [allFilesQuery.data, fileModal.data]);

  // Rows
  const {
    isItemSelected: isRowExpanded,
    toggleItemSelected: toggleRowExpanded,
  } = useSelectionState<TableData>({
    items: tableData,
    isEqual: (a, b) => a.filePath === b.filePath,
  });

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
    filterItem: () => true,
  });

  const itemsToRow = (items: TableData[]) => {
    const rows: IRow[] = [];
    items.forEach((item) => {
      const isExpanded = isRowExpanded(item);

      rows.push({
        [DataKey]: item,
        isOpen: isExpanded,
        cells: [
          {
            title: item.artifactName,
          },
          {
            title: item.fileId ? (
              <Button
                variant={ButtonVariant.link}
                isInline
                onClick={() => fileModal.open("showFile", item.fileId)}
              >
                {item.fileName}
              </Button>
            ) : (
              item.fileName
            ),
          },
          {
            title: item.filePath,
          },
        ],
      });

      // Expanded area
      if (isExpanded) {
        rows.push({
          parent: rows.length - 1,
          fullWidth: true,
          cells: [
            {
              title: <div className="pf-u-m-md">{item.description}</div>,
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
        isLoading={allUnparsableFilesQuery.isFetching}
        loadingVariant="skeleton"
        fetchError={allUnparsableFilesQuery.isError}
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
    </PageSection>
  );
};
