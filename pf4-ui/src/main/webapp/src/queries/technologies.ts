import { useCallback } from "react";

import axios, { AxiosError } from "axios";

import { ApplicationTechnologiesDto } from "@app/api/technologies";
import {
  ApplicationTechnologiesProcessed,
  TechnologyGroupsProcessed,
  TechnologyValueProcessed,
} from "@app/models/api-enriched";

import { useMockableQuery } from "./helpers";
import { MOCK_TECHNOLOGIES } from "./mocks/technologies.mock";

export const useTechnologiesQuery = () => {
  const transformCallback = useCallback(
    (
      data: ApplicationTechnologiesDto[]
    ): ApplicationTechnologiesProcessed[] => {
      const result = data.map((appTech) => {
        const technologyGroupsMapped = Object.entries(appTech.technologyGroups)
          .map(([groupName, groupValue]) => {
            const groupValueMapped = Object.entries(groupValue)
              .map(([technologyName, technologyValue]) => {
                const total = Object.entries(technologyValue).reduce(
                  (prev, [tagName, tagValue]) => {
                    return prev + tagValue;
                  },
                  0
                );

                const technologyValueMapped: TechnologyValueProcessed = {
                  total,
                  tags: { ...technologyValue },
                };

                return {
                  [technologyName]: technologyValueMapped,
                };
              })
              .reduce((prev, current) => {
                return { ...prev, ...current };
              }, {});

            return {
              [groupName]: groupValueMapped,
            };
          })
          .reduce((prev, current) => {
            return { ...prev, ...current };
          }, {});

        return {
          applicationId: appTech.applicationId,
          technologyGroups: technologyGroupsMapped as TechnologyGroupsProcessed,
        };
      });

      return result;
    },
    []
  );

  return useMockableQuery<
    ApplicationTechnologiesDto[],
    AxiosError,
    ApplicationTechnologiesProcessed[]
  >(
    {
      queryKey: ["technologies"],
      queryFn: async () => {
        const url = "/technologies";
        return (await axios.get<ApplicationTechnologiesDto[]>(url)).data;
      },
      select: transformCallback,
    },
    MOCK_TECHNOLOGIES,
    (window as any)["technologies"]
  );
};
