import React from "react";import ReactMarkdown from "react-markdown";

import { ReactMarkdownOptions } from "react-markdown/lib/react-markdown";
import remarkGfm from "remark-gfm";
import "github-markdown-css/github-markdown-light.css";

interface ISimpleMarkdownProps extends ReactMarkdownOptions {}

export const SimpleMarkdown: React.FC<ISimpleMarkdownProps> = ({ ...rest }) => {
  return (
    <ReactMarkdown
      className="markdown-body"
      remarkPlugins={[remarkGfm]}
      linkTarget="_blank"
      {...rest}
    />
  );
};
