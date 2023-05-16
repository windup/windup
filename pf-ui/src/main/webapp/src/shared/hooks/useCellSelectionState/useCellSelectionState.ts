import { useState } from "react";

interface Cell {
  isSelected: boolean;
}

const defaultCellState: Cell = { isSelected: false };

export interface IArgs<R, C> {
  rows: R[];
  columns: C[];
}

export interface IState<R, C> {
  isCellSelected: (row: R, column: C) => boolean;
  isSomeCellSelected: (row: R, columns: C[]) => boolean;
  toggleCellSelected: (row: R, column: C, isSelecting?: boolean) => void;
}

export const useCellSelectionState = <R, C>({
  rows,
  columns,
}: IArgs<R, C>): IState<R, C> => {
  const [spreadsheet, setSpreadsheet] = useState<Map<R, Map<C, Cell>>>(
    new Map()
  );

  const isCellSelected = (row: R, column: C) => {
    const cell = spreadsheet.get(row)?.get(column);
    return cell ? cell.isSelected : false;
  };

  const isSomeCellSelected = (row: R, columns: C[]) => {
    return columns.some((elem) => spreadsheet.get(row)?.get(elem));
  };

  const toggleCellSelected = (
    row: R,
    column: C,
    isSelecting: boolean = !isCellSelected(row, column)
  ) => {
    const columns: Map<C, Cell> = spreadsheet.get(row) || new Map();
    const cell: Cell = columns.get(column) || { ...defaultCellState };

    const newColumns = new Map().set(column, {
      ...cell,
      isSelected: isSelecting,
    });
    const newSpreadsheet = new Map(spreadsheet).set(row, newColumns);

    setSpreadsheet(newSpreadsheet);
  };

  return {
    isCellSelected,
    isSomeCellSelected,
    toggleCellSelected,
  };
};

export default useCellSelectionState;
