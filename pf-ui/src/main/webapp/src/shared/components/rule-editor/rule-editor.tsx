import React, { useEffect, useRef } from "react";

import {
  CodeEditor,
  CodeEditorProps,
  Language,
} from "@patternfly/react-code-editor";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";

import { useRuleQuery } from "@app/queries/rules";
import { ConditionalRender } from "@app/shared/components";

interface IRuleEditorProps {
  ruleId: string;
  props?: Partial<Omit<CodeEditorProps, "ref" | "code">>;
}

export const RuleEditor: React.FC<IRuleEditorProps> = ({ ruleId, props }) => {
  const ruleQuery = useRuleQuery(ruleId);

  const editorRef = useRef<monacoEditor.editor.IStandaloneCodeEditor>();
  const monacoRef = useRef<typeof monacoEditor>();
  useEffect(() => {
    return () => {
      monacoRef.current?.editor.getModels().forEach((model) => model.dispose());
    };
  }, [monacoRef]);

  return (
    <ConditionalRender
      when={ruleQuery.isLoading}
      then={<span>Loading...</span>}
    >
      <CodeEditor
        key={ruleId}
        isDarkTheme
        isLineNumbersVisible
        isReadOnly
        isMinimapVisible
        isLanguageLabelVisible
        isDownloadEnabled
        code={ruleQuery.data?.content}
        language={
          ruleQuery.data?.content.startsWith("<rule")
            ? Language.xml
            : Language.java
        }
        onEditorDidMount={(editor, monaco) => {
          editor.layout();
          editor.focus();
          monaco.editor.getModels()[0].updateOptions({ tabSize: 5 });

          editorRef.current = editor;
          monacoRef.current = monaco;
        }}
        height="600px"
        {...props}
      />
    </ConditionalRender>
  );
};
