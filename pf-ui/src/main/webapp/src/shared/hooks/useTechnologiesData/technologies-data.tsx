import { useCallback, useMemo } from "react";

import { ToolbarChip } from "@patternfly/react-core";


import { ApplicationDto } from "@app/api/application";
import { ALL_TECHNOLOGY_GROUPS } from "@app/api/technologies";
import { ALL_APPLICATIONS_ID } from "@app/Constants";
import { TechnologyGroupsProcessed } from "@app/models/api-enriched";
import { useApplicationsQuery } from "@app/queries/applications";
import { useTechnologiesQuery } from "@app/queries/technologies";
import { OptionWithValue } from "@app/shared/components";

interface RowData {
  application: ApplicationDto;
  technologyGroups: TechnologyGroupsProcessed;
}

export interface ITechnologiesProps {
  applicationId?: string;
  hideEmptyCategoryOptions?: boolean;
}

export const useTechnologiesData = ({
  applicationId,
  hideEmptyCategoryOptions,
}: ITechnologiesProps) => {
  // Queries
  const allApplications = useApplicationsQuery();
  const allTechnologies = useTechnologiesQuery();

  const applications = useMemo(() => {
    const toRowData = (appsToMap: ApplicationDto[]) => {
      return appsToMap.reduce((prev, current) => {
        const applicationTechnologies = allTechnologies.data?.find(
          (appTech) => appTech.applicationId === current.id
        );

        if (applicationTechnologies) {
          const rowData: RowData = {
            application: current,
            technologyGroups: applicationTechnologies.technologyGroups,
          };
          return [...prev, rowData];
        } else {
          return prev;
        }
      }, [] as RowData[]);
    };

    if (applicationId === ALL_APPLICATIONS_ID) {
      const applications = (allApplications.data || []).filter(
        (e) => !e.isVirtual
      );
      return toRowData(applications);
    } else {
      const selectedApplication = allApplications.data?.find(
        (f) => f.id === applicationId
      );

      return toRowData(selectedApplication ? [selectedApplication] : []);
    }
  }, [allApplications.data, allTechnologies.data, applicationId]);

  // Category Select filter
  const toOption = useCallback((option: ToolbarChip): OptionWithValue => {
    const toStringFn = () => option.node as string;
    return {
      value: option.key,
      toString: toStringFn,
      compareTo: (other: string | OptionWithValue) => {
        return typeof other === "string"
          ? toStringFn().toLowerCase().includes(other.toLowerCase())
          : option.key === other.value;
      },
    };
  }, []);

  const allCategoryOptions = useMemo(() => {
    if (applications.length > 0) {
      return ALL_TECHNOLOGY_GROUPS.reduce((prev, currentGroup) => {
        const technologies = applications[0].technologyGroups[currentGroup];
        const numberOfTechnologies = Object.keys(technologies).length;

        const sumOfTechnologiesTotal = Object.entries(technologies)
          .map(([technologyName, { total }]) => total)
          .reduce((prev, current) => prev + current, 0);

        return sumOfTechnologiesTotal > 0 || !hideEmptyCategoryOptions
          ? [
              ...prev,
              toOption({
                key: currentGroup,
                node: !hideEmptyCategoryOptions
                  ? `${currentGroup} (${numberOfTechnologies})`
                  : currentGroup,
              }),
            ]
          : prev;
      }, [] as OptionWithValue[]);
    } else {
      return ALL_TECHNOLOGY_GROUPS.map((elem) =>
        toOption({ key: elem, node: elem })
      );
    }
  }, [hideEmptyCategoryOptions, applications, toOption]);

  return {
    applications: applications,
    categoryOptions: allCategoryOptions,
    allApplications,
    allTechnologies,
  };
};
