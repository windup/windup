import React, { useState } from 'react';

import {
  Select,
  SelectOption,
  SelectOptionObject,
  SelectOptionProps,
  SelectProps,
} from '@patternfly/react-core';

export interface OptionWithValue<T = string> extends SelectOptionObject {
  value: T;
  props?: Partial<SelectOptionProps>; // Extra props for <SelectOption>, e.g. children, className
}

type OptionLike = string | SelectOptionObject | OptionWithValue;

export interface ISimpleSelectProps
  extends Omit<
    SelectProps,
    'onChange' | 'isOpen' | 'onToggle' | 'onSelect' | 'selections' | 'value'
  > {
  'aria-label': string;
  onChange: (selection: OptionLike) => void;
  options: OptionLike[];
  value?: OptionLike | OptionLike[];
}

export const SimpleSelect: React.FC<ISimpleSelectProps> = ({
  onChange,
  options,
  value,
  placeholderText = 'Select...',

  ...props
}) => {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <Select
      placeholderText={placeholderText}
      isOpen={isOpen}
      onToggle={setIsOpen}
      onSelect={(_, selection: OptionLike) => {
        onChange(selection);
        if (props.variant !== 'checkbox') {
          setIsOpen(false);
        }
      }}
      selections={value}
      {...props}
    >
      {options.map((option, index) => (
        <SelectOption
          key={`${index}-${option.toString()}`}
          value={option}
          {...(typeof option === 'object' && (option as OptionWithValue).props)}
        />
      ))}
    </Select>
  );
};
