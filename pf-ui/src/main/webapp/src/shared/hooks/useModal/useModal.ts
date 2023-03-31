import { useCallback, useReducer } from 'react';

import { ActionType, createAction, getType } from 'typesafe-actions';

interface IOpenAction {
  action: any;
  data: any;
}

const openModal = createAction('useModal/action/openModalWithData')<IOpenAction>();
const closeModal = createAction('useModal/action/startClose')();

// State
type State = Readonly<{
  action: any;
  data: any;
  isOpen: boolean;
}>;

const defaultState: State = {
  action: undefined,
  data: undefined,
  isOpen: false,
};

// Reducer

type Action = ActionType<typeof openModal | typeof closeModal>;

const reducer = (state: State, action: Action): State => {
  switch (action.type) {
    case getType(openModal):
      return {
        ...state,
        action: action.payload.action,
        data: action.payload.data,
        isOpen: true,
      };
    case getType(closeModal):
      return {
        ...state,
        action: undefined,
        data: undefined,
        isOpen: false,
      };
    default:
      return state;
  }
};

// Hook

interface HookState<A, T> {
  action?: A;
  data?: T;
  isOpen: boolean;
  open: (action: A, data?: T) => void;
  close: () => void;
  isAction: (action: A) => boolean;
}

export const useModal = <A, T = any>(): HookState<A, T> => {
  const [state, dispatch] = useReducer(reducer, {
    ...defaultState,
  });

  const openHandler = useCallback((action: A, entity?: T) => {
    dispatch(openModal({ action: action, data: entity }));
  }, []);

  const closeHandler = useCallback(() => {
    dispatch(closeModal());
  }, []);

  return {
    action: state.action,
    data: state.data,
    isOpen: state.isOpen,
    open: openHandler,
    close: closeHandler,
    isAction: (action: A) => state.action === action,
  };
};

export default useModal;
