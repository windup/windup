import React from 'react';

import { Pagination, PaginationVariant, ToggleTemplate } from '@patternfly/react-core';

export interface ISimplePaginationProps {
  count: number;
  params: {
    perPage?: number;
    page?: number;
  };

  isTop?: boolean;
  isCompact?: boolean;
  perPageOptions?: number[];
  onChange: ({ page, perPage }: { page: number; perPage: number }) => void;
}

export const SimplePagination: React.FC<ISimplePaginationProps> = ({
  count,
  params,
  isTop,
  isCompact,
  perPageOptions,
  onChange,
}) => {
  const mapPerPageOptions = (options: number[]) => {
    return options.map((option) => ({
      title: String(option),
      value: option,
    }));
  };

  const getPerPage = () => {
    return params.perPage || 10;
  };

  return (
    <Pagination
      itemCount={count}
      page={params.page || 1}
      perPage={getPerPage()}
      onPageInput={(_, page) => {
        onChange({ page, perPage: getPerPage() });
      }}
      onSetPage={(_, page) => {
        onChange({ page, perPage: getPerPage() });
      }}
      onPerPageSelect={(_, perPage) => {
        onChange({ page: 1, perPage });
      }}
      isCompact={isTop || isCompact}
      widgetId="pagination-options-menu"
      variant={isTop ? PaginationVariant.top : PaginationVariant.bottom}
      perPageOptions={mapPerPageOptions(perPageOptions || [10, 20, 50, 100])}
      toggleTemplate={(props) => <ToggleTemplate {...props} />}
    />
  );
};
