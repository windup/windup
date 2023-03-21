import React, { useEffect, useRef, useState } from "react";

import {
  CodeEditor,
  CodeEditorProps,
  Language,
} from "@patternfly/react-code-editor";
import {
  Card,
  CardActions,
  CardBody,
  CardHeader,
  CardTitle,
  Drawer,
  DrawerActions,
  DrawerCloseButton,
  DrawerContent,
  DrawerContentBody,
  DrawerHead,
  DrawerPanelContent,
  Text,
  TextContent,
} from "@patternfly/react-core";
import * as monacoEditor from "monaco-editor/esm/vs/editor/editor.api";

import { FileDto, HintDto } from "@app/api/file";
import { useFileQuery } from "@app/queries/files";
import { useRulesQuery } from "@app/queries/rules";
import { ConditionalRender, SimpleMarkdown } from "@app/shared/components";
import { getMarkdown } from "@app/utils/rule-utils";

interface IFileEditorProps {
  file: FileDto;
  lineToFocus?: number;
  hintToFocus?: HintDto;
  props?: Partial<
    Omit<CodeEditorProps, "ref" | "code" | "options" | "onEditorDidMount">
  >;
}

export const FileEditor: React.FC<IFileEditorProps> = ({
  file,
  props,
  lineToFocus,
  hintToFocus,
}) => {
  const allRules = useRulesQuery();
  const fileContent = useFileQuery(file.id);

  // Editor
  const editorRef = useRef<monacoEditor.editor.IStandaloneCodeEditor>();
  const monacoRef = useRef<typeof monacoEditor>();
  useEffect(() => {
    return () => {
      monacoRef.current?.editor.getModels().forEach((model) => model.dispose());
      editorRef.current?.dispose();
    };
  }, [editorRef, monacoRef]);

  // Disposables
  const [disposables, setDisposables] = useState<monacoEditor.IDisposable[]>(
    []
  );
  useEffect(() => {
    return () => {
      disposables.forEach((disposable) => disposable && disposable.dispose());
    };
  }, [disposables]);

  const drawerRef = React.useRef<any>();
  const [isDrawerExpanded, setIsDrawerExpanded] = useState(
    hintToFocus ? true : false
  );
  const [drawerHint, setDrawerHint] = useState(hintToFocus);
  const onDrawerExpand = () => {
    drawerRef.current && drawerRef.current.focus();
  };

  /**
   * Adds the left Windup icon to the editor
   */
  const addDeltaDecorations = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor,
    hints: HintDto[]
  ) => {
    const decorations = hints.map((hint) => {
      const decoration = {
        range: new monaco.Range(hint.line, 1, hint.line, 1),
        options: {
          isWholeLine: true,
          glyphMarginClassName: "windupGlyphMargin",
        },
      };
      return decoration;
    });

    editor.deltaDecorations([], decorations);
  };

  /**
   * Adds actions on top of the line with a Hint
   */
  const addCodeLens = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor,
    hints: HintDto[]
  ) => {
    const lenses = hints.map((hint) => {
      const lense: monacoEditor.languages.CodeLens = {
        range: new monaco.Range(hint.line!, 1, hint.line!, 1),
        id: "view-hint",
        command: {
          title: "View Hint",
          id: editor.addCommand(
            0,
            () => {
              setDrawerHint(hint);
              setIsDrawerExpanded(true);
            },
            ""
          )!,
        },
      };
      return lense;
    });

    const codeLens = monaco.languages.registerCodeLensProvider("*", {
      provideCodeLenses: (model, token) => {
        return {
          lenses: lenses,
          dispose: () => {
            // codeLens.dispose();
          },
        };
      },
      resolveCodeLens: (model, codeLens, token) => {
        return codeLens;
      },
    });

    return codeLens;
  };

  /**
   * Adds a hover text to the hint line
   */
  const addHover = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor,
    hints: HintDto[]
  ) => {
    return hints.map((hint) => {
      return monaco.languages.registerHoverProvider("*", {
        provideHover: (model, position) => {
          if (position.lineNumber !== hint.line) {
            return undefined;
          }

          return {
            range: new monaco.Range(hint.line!, 1, hint.line!, 1),
            contents: [
              {
                value: getMarkdown(hint.content, hint.links),
              },
            ],
          };
        },
      });
    });
  };

  /**
   * Underlines the hint line
   */
  const addMarkers = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor,
    hints: HintDto[]
  ) => {
    const markers = hints.map((hint) => {
      const rule = allRules.data?.find((f) => f.id === hint.ruleId);
      const marker: monacoEditor.editor.IMarkerData = {
        startLineNumber: hint.line,
        startColumn: 0,
        endLineNumber: hint.line,
        endColumn: 1000,
        message: hint.content,
        source: rule?.id,
        severity: monaco.MarkerSeverity.Warning,
      };
      return marker;
    });

    const model = monaco.editor.getModels()[0];
    monaco.editor.setModelMarkers(model, "*", markers);
  };

  const onEditorDidMount = (
    editor: monacoEditor.editor.IStandaloneCodeEditor,
    monaco: typeof monacoEditor
  ) => {
    editor.layout();
    editor.focus();
    monaco.editor.getModels()[0].updateOptions({ tabSize: 5 });

    const hints = file?.hints || [];
    let newDisposables: monacoEditor.IDisposable[] = [];

    // Add markers
    addMarkers(editor, monaco, file?.hints || []);

    // Add code lenses
    const codeLens = addCodeLens(editor, monaco, hints);
    newDisposables.push(codeLens);

    // Add delta decorations
    addDeltaDecorations(editor, monaco, hints);

    // Add hovers
    const hovers = addHover(editor, monaco, hints);
    newDisposables = newDisposables.concat(hovers);

    setDisposables(newDisposables);

    const offset = 5;
    if (hintToFocus && hintToFocus.line) {
      editor.revealLineNearTop(hintToFocus.line + offset);
    }
    if (lineToFocus) {
      editor.revealLineNearTop(lineToFocus + offset);
    }

    // Open warning programatically
    // editor.trigger("anystring", `editor.action.marker.next`, "s");

    editorRef.current = editor;
    monacoRef.current = monaco;
  };

  return (
    <Drawer isExpanded={isDrawerExpanded} onExpand={onDrawerExpand} isInline>
      <DrawerContent
        panelContent={
          <DrawerPanelContent
            isResizable
            defaultSize={"800px"}
            minSize={"350px"}
          >
            <DrawerHead>
              <Card isLarge>
                <CardHeader>
                  <CardActions hasNoOffset>
                    <DrawerActions>
                      <DrawerCloseButton
                        onClick={() => setIsDrawerExpanded(false)}
                      />
                    </DrawerActions>
                  </CardActions>
                  <CardTitle>
                    <TextContent>
                      <Text component="h1">{drawerHint?.title}</Text>
                      <Text component="small">Line: {drawerHint?.line}</Text>
                    </TextContent>
                  </CardTitle>
                </CardHeader>
                <CardBody>
                  {drawerHint && (
                    <SimpleMarkdown
                      children={getMarkdown(
                        drawerHint.content,
                        drawerHint.links
                      )}
                    />
                  )}
                </CardBody>
              </Card>
            </DrawerHead>
          </DrawerPanelContent>
        }
      >
        <DrawerContentBody>
          <ConditionalRender
            when={fileContent.isLoading}
            then={<span>Loading...</span>}
          >
            <CodeEditor
              isDarkTheme
              isLineNumbersVisible
              isReadOnly
              isMinimapVisible
              isLanguageLabelVisible
              isDownloadEnabled
              code={fileContent.data?.content}
              language={Object.values(Language).find(
                (l) => l === file.sourceType.toLowerCase()
              )}
              options={{
                glyphMargin: true,
                "semanticHighlighting.enabled": true,
                renderValidationDecorations: "on",
              }}
              onEditorDidMount={(
                editor: monacoEditor.editor.IStandaloneCodeEditor,
                monaco: typeof monacoEditor
              ) => {
                onEditorDidMount(editor, monaco);
              }}
              height={`${window.innerHeight - 300}px`}
              {...props}
            />
          </ConditionalRender>
        </DrawerContentBody>
      </DrawerContent>
    </Drawer>
  );
};
