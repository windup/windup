import React from 'react';

export interface ConditionalRenderProps {
  when: boolean;
  then: any;
  children: any;
}

export const ConditionalRender: React.FC<ConditionalRenderProps> = ({ when, then, children }) => {
  return when ? then : children || <></>;
};
