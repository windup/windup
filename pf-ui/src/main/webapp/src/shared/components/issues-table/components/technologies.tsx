import React, { useMemo } from "react";

import { Label, Split, SplitItem } from "@patternfly/react-core";

import { useRulesQuery } from "@app/queries/rules";

interface ITechnologiesProps {
  variant: "source" | "target";
  ruleId: string;
}

export const Technologies: React.FC<ITechnologiesProps> = ({
  variant,
  ruleId,
}) => {
  const allRules = useRulesQuery();
  const rule = useMemo(() => {
    return allRules.data?.find((e) => e.id === ruleId);
  }, [allRules.data, ruleId]);

  return (
    <Split hasGutter>
      {(variant === "source"
        ? rule?.sourceTechnology
        : rule?.targetTechnology
      )?.map((technology) => (
        <SplitItem key={technology.id}>
          <Label isCompact color="blue">
            {[technology.id, technology.versionRange]
              .filter((e) => e)
              .join(": ")}
          </Label>
        </SplitItem>
      ))}
    </Split>
  );
};
