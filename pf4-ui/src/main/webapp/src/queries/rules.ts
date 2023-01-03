import { useCallback } from "react";

import axios, { AxiosError } from "axios";

import { RuleGroupDto } from "@app/api/rule";
import { RuleContentDto } from "@app/api/rule-content";
import { RuleProcessed } from "@app/models/api-enriched";

import { useMockableQuery } from "./helpers";
import { MOCK_RULES, MOCK_RULES_CONTENT } from "./mocks/rules.mock";

export const useRulesQuery = () => {
  const transformCallback = useCallback((data: RuleGroupDto) => {
    let result: RuleProcessed[] = [];

    for (const [key, value] of Object.entries(data)) {
      const rulesFromPhase = value.map((e) => {
        const processedRule: RuleProcessed = { ...e, phase: key };
        return processedRule;
      });

      result = result.concat(rulesFromPhase);
    }

    return result;
  }, []);

  return useMockableQuery<RuleGroupDto, AxiosError, RuleProcessed[]>(
    {
      queryKey: ["rules"],
      queryFn: async () => (await axios.get<RuleGroupDto>("/rules")).data,
      select: transformCallback,
    },
    MOCK_RULES,
    (window as any)["rules"]
  );
};

export const useRuleQuery = (ruleId: string) => {
  return useMockableQuery<RuleContentDto, AxiosError, RuleContentDto>(
    {
      queryKey: ["rules", ruleId],
      queryFn: async () =>
        (await axios.get<RuleContentDto>(`/rules/${ruleId}`)).data,
    },
    MOCK_RULES_CONTENT[ruleId],
    (window as any)["rules_by_id"]
      ? (window as any)["rules_by_id"][ruleId]
      : undefined
  );
};
