import { useCallback, useReducer } from 'react';

import { IExtraColumnData, SortByDirection } from '@patternfly/react-table';
import { ActionType, createAction, getType } from 'typesafe-actions';

import { Page, SortBy } from '../../common/types';

// Actions

const setPage = createAction('useTableControls/pagination/change')<Page>();
const setSortBy = createAction('useTableControls/sortBy/change')<SortBy>();

// State
type State = Readonly<{
  changed: boolean;

  currentPage: Page;
  sortBy?: SortBy;
}>;

const defaultState: State = {
  changed: false,

  currentPage: {
    page: 1,
    perPage: 10,
  },
  sortBy: undefined,
};

// Reducer

type Action = ActionType<typeof setSortBy | typeof setPage>;

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case getType(setPage):
      return {
        ...state,
        changed: true,
        currentPage: {
          page: action.payload.page,
          perPage: action.payload.perPage,
        },
      };
    case getType(setSortBy):
      return {
        ...state,
        changed: true,
        sortBy: {
          index: action.payload.index,
          direction: action.payload.direction,
        },
      };
    default:
      return state;
  }
};

// Hook

interface HookArgs {
  page?: Page;
  sortBy?: SortBy;
}

interface HookState {
  page: Page;
  sortBy?: SortBy;
  changePage: (page: { page: number; perPage?: number }) => void;
  changeSortBy: (
    event: React.MouseEvent,
    index: number,
    direction: SortByDirection,
    extraData: IExtraColumnData
  ) => void;
}

export const useTableControls = (args?: HookArgs): HookState => {
  const [state, dispatch] = useReducer(reducer, {
    ...defaultState,
    currentPage: args && args.page ? { ...args.page } : { ...defaultState.currentPage },
    sortBy: args && args.sortBy ? { ...args.sortBy } : defaultState.sortBy,
  });

  const handlePageChange = useCallback((newPage: { page: number; perPage?: number }) => {
    dispatch(
      setPage({
        page: newPage.page >= 1 ? newPage.page : 1,
        perPage: newPage.perPage ?? defaultState.currentPage.perPage,
      })
    );
  }, []);

  const handleSortByChange = useCallback(
    (
      event: React.MouseEvent,
      index: number,
      direction: SortByDirection,
      extraData: IExtraColumnData
    ) => {
      dispatch(
        setSortBy({
          index: index,
          direction: direction,
        })
      );
    },
    []
  );

  return {
    page: state.currentPage,
    sortBy: state.sortBy,
    changePage: handlePageChange,
    changeSortBy: handleSortByChange,
  };
};
