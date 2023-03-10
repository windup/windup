import { useCallback } from "react";

import axios, { AxiosError } from "axios";

import {
  ApplicationIssuesDto,
  IssueCategoryType,
  compareByCategoryAndNameFn,
} from "@app/api/issues";
import {
  ApplicationIssuesProcessed,
  IssueProcessed,
} from "@app/models/api-enriched";

import { useMockableQuery } from "./helpers";
import { MOCK_ISSUES } from "./mocks/issues.mock";

export const useIssuesQuery = () => {
  const transformCallback = useCallback(
    (data: ApplicationIssuesDto[]) =>
      data.map((e) => {
        const issuesProccesed: IssueProcessed[] = Object.keys(e.issues).flatMap(
          (category) => {
            return e.issues[category as IssueCategoryType].flatMap((issue) => ({
              ...issue,
              category: category as IssueCategoryType,
            }));
          }
        );

        const result: ApplicationIssuesProcessed = {
          applicationId: e.applicationId,
          issues: issuesProccesed.sort(
            compareByCategoryAndNameFn(
              (elem) => elem.category,
              (elem) => elem.name
            )
          ),
        };
        return result;
      }),
    []
  );

  return useMockableQuery<
    ApplicationIssuesDto[],
    AxiosError,
    ApplicationIssuesProcessed[]
  >(
    {
      queryKey: ["issues"],
      queryFn: async () => {
        return (await axios.get<ApplicationIssuesDto[]>("/issues")).data;
      },
      select: transformCallback,
    },
    MOCK_ISSUES,
    (window as any)["issues"]
  );
};
