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
  ALL_SUPPORTED_ISSUE_CATEGORY,
  compareByCategoryFn,
  IssueCategoryType,
} from "@app/api/issues";
import { useIssuesQuery } from "@app/queries/issues";

interface IncidentsData {
  category: IssueCategoryType;
  totalIncidents: number;
  totalStoryPoints: number;
}

const DEFAULT_INCIDENTS_DATA: IncidentsData[] =
  ALL_SUPPORTED_ISSUE_CATEGORY.map((e) => ({
    category: e,
    totalIncidents: 0,
    totalStoryPoints: 0,
  }));

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

export interface IIncidentsSectionProps {
  application: ApplicationDto;
}

export const IncidentsSection: React.FC<IIncidentsSectionProps> = ({
  application,
}) => {
  const allIssues = useIssuesQuery();

  const applicationIssues = useMemo(() => {
    return allIssues.data?.find((f) => f.applicationId === application.id);
  }, [application, allIssues.data]);

  // Incidents Chart
  const incidents = useMemo(() => {
    return (applicationIssues?.issues || []).reduce((prev, current) => {
      const prevVal: IncidentsData | undefined = prev.find(
        (e) => e.category === current.category
      );

      let result: IncidentsData[];
      if (prevVal) {
        result = [
          ...prev.filter((e) => e.category !== current.category),
          {
            category: current.category,
            totalIncidents: prevVal.totalIncidents + current.totalIncidents,
            totalStoryPoints:
              prevVal.totalStoryPoints + current.totalStoryPoints,
          },
        ];
      } else {
        result = [
          ...prev,
          {
            category: current.category,
            totalIncidents: 0,
            totalStoryPoints: 0,
          },
        ];
      }

      return result.sort(compareByCategoryFn((elem) => elem.category));
    }, DEFAULT_INCIDENTS_DATA);
  }, [applicationIssues]);

  return (
    <Grid md={6}>
      <GridItem>
        <Card isFullHeight>
          <CardTitle>Incidents</CardTitle>
          <CardBody>
            <TableComposable variant="compact">
              <Thead>
                <Tr>
                  <Th width={40}>Category</Th>
                  <Th>Incidents</Th>
                  <Th>Total Story Points</Th>
                </Tr>
              </Thead>
              <Tbody>
                {incidents.map((incident) => (
                  <Tr key={incident.category}>
                    <Td>{incident.category}</Td>
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
          <CardTitle>Incidents and Story Points</CardTitle>
          <CardBody>
            <Chart
              themeColor={ChartThemeColor.multiOrdered}
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
                      x: incident.category,
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
