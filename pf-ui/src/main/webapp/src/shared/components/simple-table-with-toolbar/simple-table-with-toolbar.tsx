import React from "react";

import {
  Toolbar,
  ToolbarContent,
  ToolbarItem,
  ToolbarItemVariant,
  ToolbarToggleGroup,
} from "@patternfly/react-core";
import FilterIcon from "@patternfly/react-icons/dist/esm/icons/filter-icon";

import { SimplePagination } from "./simple-pagination";
import { SimpleTable, ISimpleTableProps } from "./simple-table";

export interface ISimpleTableWithToolbarProps extends ISimpleTableProps {
  totalCount: number;

  currentPage: {
    page?: number;
    perPage?: number;
  };
  onPageChange: ({ page, perPage }: { page: number; perPage: number }) => void;

  hasTopPagination?: boolean;
  hasBottomPagination?: boolean;

  toolbarBulkSelector?: any;
  toolbarToggle?: any;
  toolbarActions?: any;
  toolbarClearAllFilters?: () => void;
}

export const SimpleTableWithToolbar: React.FC<ISimpleTableWithToolbarProps> = ({
  totalCount,
  currentPage,
  onPageChange,

  hasTopPagination = false,
  hasBottomPagination = false,

  toolbarBulkSelector,
  toolbarToggle,
  toolbarActions,
  toolbarClearAllFilters,

  ...rest
}) => {
  return (
    <div style={{ backgroundColor: "var(--pf-global--BackgroundColor--100)" }}>
      <Toolbar
        className="pf-m-toggle-group-container"
        collapseListedFiltersBreakpoint="xl"
        clearAllFilters={toolbarClearAllFilters}
      >
        <ToolbarContent>
          {toolbarBulkSelector}
          {toolbarToggle && (
            <ToolbarToggleGroup toggleIcon={<FilterIcon />} breakpoint="xl">
              {toolbarToggle}
            </ToolbarToggleGroup>
          )}
          {toolbarActions}
          {hasTopPagination && (
            <ToolbarItem
              variant={ToolbarItemVariant.pagination}
              alignment={{ default: "alignRight" }}
            >
              <SimplePagination
                count={totalCount}
                params={currentPage}
                onChange={onPageChange}
                isTop={true}
              />
            </ToolbarItem>
          )}
        </ToolbarContent>
      </Toolbar>
      <SimpleTable {...rest} />
      {hasBottomPagination && (
        <SimplePagination
          count={totalCount}
          params={currentPage}
          onChange={onPageChange}
        />
      )}
    </div>
  );
};
