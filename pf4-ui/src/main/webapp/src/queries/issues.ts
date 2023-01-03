import { useCallback } from "react";

import axios, { AxiosError } from "axios";

import { ApplicationIssuesDto } from "@app/api/application-issues";
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
        const mandatory: IssueProcessed[] =
          e.issues.mandatory?.map((e) => ({
            ...e,
            category: "mandatory",
          })) || [];
        const optional: IssueProcessed[] =
          e.issues.optional?.map((e) => ({
            ...e,
            category: "optional",
          })) || [];
        const potential: IssueProcessed[] =
          e.issues.potential?.map((e) => ({
            ...e,
            category: "potential",
          })) || [];
        const information: IssueProcessed[] =
          e.issues.information?.map((e) => ({
            ...e,
            category: "information",
          })) || [];

        const result: ApplicationIssuesProcessed = {
          applicationId: e.applicationId,
          issues: mandatory
            .concat(optional, potential, information)
            .sort((a, b) => a.name.localeCompare(b.name)),
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
