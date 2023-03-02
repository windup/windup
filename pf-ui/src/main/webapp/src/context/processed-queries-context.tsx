import { createContext, useContext, useMemo } from "react";

import { TechnologyDto } from "@app/api/rule";
import { IssueProcessed, RuleProcessed } from "@app/models/api-enriched";
import { useFilesQuery } from "@app/queries/files";
import { useIssuesQuery } from "@app/queries/issues";
import { useRulesQuery } from "@app/queries/rules";
import { technologiesToArray } from "@app/utils/rule-utils";

interface IProcessedQueriesContext {
  rulesByIssueId: Map<string, RuleProcessed>;
  technologies: {
    source: string[];
    target: string[];
  };
  issuesByFileId: Map<string, IssueProcessed[]>;
}

const ProcessedQueriesContext = createContext<IProcessedQueriesContext>({
  rulesByIssueId: new Map(),
  technologies: { source: [], target: [] },
  issuesByFileId: new Map(),
});

export const ProcessedQueriesContextProvider: React.FC = ({ children }) => {
  const allIssues = useIssuesQuery();
  const allRules = useRulesQuery();
  const allFiles = useFilesQuery();

  const technologies = useMemo(() => {
    const source = technologiesToArray(
      (allRules.data || [])
        .flatMap((e) => e.sourceTechnology)
        .reduce((prev, current) => {
          return current ? [...prev, current] : prev;
        }, [] as TechnologyDto[])
    );
    const target = technologiesToArray(
      (allRules.data || [])
        .flatMap((e) => e.targetTechnology)
        .reduce((prev, current) => {
          return current ? [...prev, current] : prev;
        }, [] as TechnologyDto[])
    );
    return { source, target };
  }, [allRules.data]);

  const rulesByIssueId = useMemo(() => {
    const result = new Map<string, RuleProcessed>();
    if (
      allIssues.isFetched &&
      allRules.isFetched &&
      allIssues.data &&
      allRules.data
    ) {
      allIssues.data
        .flatMap((f) => f.issues)
        .forEach((issue) => {
          const rule = allRules.data.find((rule) => rule.id === issue.ruleId);
          if (rule) {
            result.set(issue.id, rule);
          }
        });
    }
    return result;
  }, [allIssues.isFetched, allRules.isFetched, allIssues.data, allRules.data]);

  const issuesByFileId = useMemo(() => {
    const result = new Map<string, IssueProcessed[]>();
    if (
      allIssues.isFetched &&
      allFiles.isFetched &&
      allIssues.data &&
      allFiles.data
    ) {
      allFiles.data.forEach((file) => {
        const issue = allIssues.data
          .flatMap((f) => f.issues)
          .filter((issue) => {
            return issue.affectedFiles
              .flatMap((f) => f.files)
              .find((affectedFile) => affectedFile.fileId === file.id);
          });
        const current = result.get(file.id) || [];
        result.set(
          file.id,
          [...current, ...issue].sort((a, b) => a.name.localeCompare(b.name))
        );
      });
    }

    return result;
  }, [allIssues.isFetched, allFiles.isFetched, allIssues.data, allFiles.data]);

  return (
    <ProcessedQueriesContext.Provider
      value={{ technologies, rulesByIssueId, issuesByFileId }}
    >
      {children}
    </ProcessedQueriesContext.Provider>
  );
};

export const useProcessedQueriesContext = () =>
  useContext(ProcessedQueriesContext);
