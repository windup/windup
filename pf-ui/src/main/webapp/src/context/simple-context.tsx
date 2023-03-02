import React, {
  createContext,
  useContext,
  useEffect,
  useReducer,
  useState,
} from "react";

import {
  ContextSelector,
  ContextSelectorItem,
  ContextSelectorProps,
} from "@patternfly/react-core";

import "./simple-context.css";

export interface Context {
  key: string;
  label: string;
}

interface ISimpleContext {
  allContexts: Context[];
  currentContext?: Context;
  selectContext: (key: string) => void;
}

const SimpleContext = createContext<ISimpleContext>({
  allContexts: [],
  currentContext: undefined,
  selectContext: () => undefined,
});

interface ISimpleContextProviderProps {
  allContexts: Context[];
  children: React.ReactNode;
}

export const SimpleContextProvider: React.FunctionComponent<
  ISimpleContextProviderProps
> = ({ allContexts, children }: ISimpleContextProviderProps) => {
  const [selectedContextKey, setSelectedContextKey] = useState<string>();

  return (
    <SimpleContext.Provider
      value={{
        allContexts,
        currentContext: allContexts.find((f) => f.key === selectedContextKey),
        selectContext: (key: string) => setSelectedContextKey(key),
      }}
    >
      {children}
    </SimpleContext.Provider>
  );
};

export const useSimpleContext = (): ISimpleContext => useContext(SimpleContext);

// Helpers components

export interface ISimpleContextSelectorProps {
  contextKeyFromURL?: string;
  props?: Omit<
    ContextSelectorProps,
    | "isOpen"
    | "toggleText"
    | "onToggle"
    | "searchInputValue"
    | "onSearchInputChange"
  >;
  onChange: (context: Context) => void;
}

export const SimpleContextSelector: React.FC<ISimpleContextSelectorProps> = ({
  contextKeyFromURL,
  props,
  onChange,
}) => {
  const { allContexts, currentContext, selectContext } = useSimpleContext();

  useEffect(() => {
    const currentContextKey = contextKeyFromURL ?? currentContext?.key;

    if (typeof currentContextKey === "string") {
      selectContext(currentContextKey);
    }
  }, [contextKeyFromURL, currentContext, selectContext]);

  const [filterText, setFilterText] = useState("");
  const [isSelectorOpen, toggleSelector] = useReducer(
    (isVisible) => !isVisible,
    false
  );

  const onSelect = (value: Context) => {
    toggleSelector();
    selectContext(value.key);
    onChange(value);
  };

  return (
    <ContextSelector
      isOpen={isSelectorOpen}
      toggleText={currentContext?.label}
      onToggle={toggleSelector}
      searchInputValue={filterText}
      onSearchInputChange={setFilterText}
      className="firstChildBordered"
      {...props}
    >
      {allContexts
        .filter(
          (f) => f.label.toLowerCase().indexOf(filterText.toLowerCase()) !== -1
        )
        .map((item, index) => {
          return (
            <ContextSelectorItem key={index} onClick={() => onSelect(item)}>
              {item.label}
            </ContextSelectorItem>
          );
        })}
    </ContextSelector>
  );
};
