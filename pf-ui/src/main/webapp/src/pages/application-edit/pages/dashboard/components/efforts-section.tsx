import React, { useMemo } from "react";

import {
  Chart,
  ChartAxis,
  ChartBar,
  ChartGroup,
  ChartThemeColor,
  ChartTooltip,
} from "@patternfly/react-charts";
import {
  Card,
  CardBody,
  CardTitle,
  Grid,
  GridItem,
} from "@patternfly/react-core";
import {
  TableComposable,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
} from "@patternfly/react-table";

import { ApplicationDto } from "@app/api/application";
import {
  ALL_LEVEL_OF_EFFORTS,
  LevelOfEffortType,
} from "@app/api/issues";
import { useIssuesQuery } from "@app/queries/issues";

interface IncidentsData {
  effort: LevelOfEffortType;
  totalIncidents: number;
  totalStoryPoints: number;
}

const DEFAULT_INCIDENTS_DATA: IncidentsData[] = ALL_LEVEL_OF_EFFORTS.map(
  (e) => ({
    effort: e,
    totalIncidents: 0,
    totalStoryPoints: 0,
  })
);

type IncidentsChart = {
  [key in "IncidentsBar" | "StoryPointsBar"]: {
    getY: (data: IncidentsData) => number;
    getTooltip: (data: any) => string;
  };
};
const INCIDENTS_CHART: IncidentsChart = {
  IncidentsBar: {
    getY: (data: IncidentsData) => {
      return data.totalIncidents;
    },
    getTooltip: ({ datum }: any) => `${datum.y} incidents`,
  },
  StoryPointsBar: {
    getY: (data: IncidentsData) => {
      return data.totalStoryPoints;
    },
    getTooltip: ({ datum }: any) => `${datum.y} SP`,
  },
};

const sortIncidentsData = (data: IncidentsData[]) => {
  const getEffortPriority = (effortType: LevelOfEffortType) => {
    switch (effortType) {
      case "Info":
        return 1;
      case "Trivial":
        return 2;
      case "Complex":
        return 3;
      case "Redesign":
        return 4;
      case "Architectural":
        return 5;
      case "Unknown":
        return 6;
      default:
        return 0;
    }
  };

  return data.sort(
    (a, b) => getEffortPriority(a.effort) - getEffortPriority(b.effort)
  );
};

export interface IEffortsSectionProps {
  application: ApplicationDto;
}

export const EffortsSection: React.FC<IEffortsSectionProps> = ({
  application,
}) => {
  const allIssues = useIssuesQuery();

  const applicationIssues = useMemo(() => {
    return allIssues.data?.find((f) => f.applicationId === application.id);
  }, [application, allIssues.data]);

  // Incidents Chart
  const incidents = useMemo(() => {
    return (applicationIssues?.issues || [])
      .filter((e) => e.category === "mandatory")
      .reduce((prev, current) => {
        const prevVal: IncidentsData | undefined = prev.find(
          (e) => e.effort === current.effort.type
        );

        let result: IncidentsData[];
        if (prevVal) {
          result = [
            ...prev.filter((e) => e.effort !== current.effort.type),
            {
              effort: current.effort.type,
              totalIncidents: prevVal.totalIncidents + current.totalIncidents,
              totalStoryPoints:
                prevVal.totalStoryPoints + current.totalStoryPoints,
            },
          ];
        } else {
          result = [
            ...prev,
            {
              effort: current.effort.type,
              totalIncidents: 0,
              totalStoryPoints: 0,
            },
          ];
        }

        return sortIncidentsData(result);
      }, DEFAULT_INCIDENTS_DATA);
  }, [applicationIssues]);

  return (
    <Grid md={6}>
      <GridItem>
        <Card isFullHeight>
          <CardTitle>Mandatory incidents</CardTitle>
          <CardBody>
            <TableComposable variant="compact">
              <Thead>
                <Tr>
                  <Th width={40}>Type</Th>
                  <Th>Incidents</Th>
                  <Th>Total Story Points</Th>
                </Tr>
              </Thead>
              <Tbody>
                {incidents.map((incident) => (
                  <Tr key={incident.effort}>
                    <Td>{incident.effort}</Td>
                    <Td>{incident.totalIncidents}</Td>
                    <Td>{incident.totalStoryPoints}</Td>
                  </Tr>
                ))}
              </Tbody>
            </TableComposable>
          </CardBody>
        </Card>
      </GridItem>
      <GridItem>
        <Card isFullHeight>
          <CardTitle>Mandatory incidents and Story Points</CardTitle>
          <CardBody>
            <Chart
              themeColor={ChartThemeColor.multiOrdered}
              // Define a static domain only if no bar is expected to be rendered
              domain={
                incidents.every((e) => {
                  return e.totalIncidents === 0 && e.totalStoryPoints === 0;
                })
                  ? { y: [0, 9] }
                  : undefined
              }
              domainPadding={{ x: 35 }}
              padding={{
                bottom: 40,
                top: 20,
                left: 60,
                right: 0,
              }}
              height={300}
              width={700}
            >
              <ChartAxis />
              <ChartAxis dependentAxis showGrid={false} />
              <ChartGroup offset={10}>
                {Object.entries(INCIDENTS_CHART).map(([barName, barConfig]) => (
                  <ChartBar
                    key={barName}
                    data={incidents.map((incident) => ({
                      name: barName,
                      x: incident.effort,
                      y: barConfig.getY(incident),
                      label: barConfig.getTooltip,
                    }))}
                    labelComponent={<ChartTooltip constrainToVisibleArea />}
                  />
                ))}
              </ChartGroup>
            </Chart>
          </CardBody>
        </Card>
      </GridItem>
    </Grid>
  );
};
