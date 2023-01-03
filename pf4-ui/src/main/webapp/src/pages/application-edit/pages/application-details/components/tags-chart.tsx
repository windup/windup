import React, { useMemo } from "react";

import {
  Chart,
  ChartAxis,
  ChartBar,
  ChartGroup,
  ChartVoronoiContainer,
} from "@patternfly/react-charts";

import { ApplicationFileDto } from "@app/api/application-details";
import { FileDto } from "@app/api/file";
import { TagDto } from "@app/api/tag";
import { useFilesQuery } from "@app/queries/files";
import { useTagsQuery } from "@app/queries/tags";

const getNearestRoots = (tags: TagDto[], tagName: string) => {
  const tag = tags.find((t) => t.name === tagName);
  if (!tag) {
    return null;
  }

  let currentSet = [tag];
  let nextSet: TagDto[] = [];
  const visitedParents = [tagName];
  const roots = tag.isRoot ? [tag] : [];

  // Follow the multiple possible parent paths to their roots.
  while (currentSet.length !== 0) {
    nextSet = [];
    for (let i = 0; i < currentSet.length; i++) {
      const currentTagName = currentSet[i];
      for (let j = 0; j < currentTagName.parentsTagNames.length; j++) {
        const currentParentName = currentTagName.parentsTagNames[j];
        if (visitedParents.indexOf(currentParentName) !== -1) {
          continue;
        }
        visitedParents.push(currentParentName);

        const currentParent = tags.find((t) => t.name === currentParentName);
        if (currentParent) {
          if (currentParent.isRoot) {
            roots.push(currentParent);
          } else {
            nextSet.push(currentParent);
          }
        }
      }
    }
    currentSet = nextSet;
  }
  return roots;
};

export interface ITagsChartProps {
  applicationFile: ApplicationFileDto[];
}

export const TagsChart: React.FC<ITagsChartProps> = ({ applicationFile }) => {
  const tagsQuery = useTagsQuery();
  const allFilesQuery = useFilesQuery();

  const applicationFiles: FileDto[] = useMemo(() => {
    return applicationFile
      .flatMap((f) => f.childrenFileIds)
      .map((childFileId) => {
        const defaultFile: FileDto = {
          id: childFileId,
          fullPath: "",
          prettyPath: "",
          prettyFileName: "",
          hints: [],
          tags: [],
          classificationsAndHintsTags: [],
          sourceType: "",
          storyPoints: -1,
        };
        const file: FileDto | undefined = allFilesQuery.data?.find(
          (file) => file.id === childFileId
        );
        return { ...defaultFile, ...file };
      });
  }, [allFilesQuery.data, applicationFile]);

  const data = useMemo(() => {
    if (applicationFiles && tagsQuery.data) {
      return applicationFiles
        .flatMap((f) => [
          ...f.tags.map((t) => t.name),
          ...f.classificationsAndHintsTags,
        ])
        .reduce((prev, current) => {
          const rootTags = getNearestRoots(tagsQuery.data, current);
          rootTags?.forEach((t) => {
            const key = t.title || t.name;
            prev.set(key, (prev.get(key) ?? 0) + 1);
          });
          return prev;
        }, new Map<string, number>());
    } else {
      return new Map<string, number>();
    }
  }, [applicationFiles, tagsQuery.data]);

  return (
    <>
      <div style={{ height: "250px", width: "600px" }}>
        <Chart
          containerComponent={
            <ChartVoronoiContainer
              labels={({ datum }) => `${datum.y}`}
              constrainToVisibleArea
            />
          }
          height={250}
          width={600}
          padding={{
            bottom: 50,
            left: 200,
            right: 10,
            top: 10,
          }}
          domainPadding={{ x: [15, 15] }}
        >
          <ChartAxis />
          <ChartAxis dependentAxis />
          <ChartGroup offset={11} horizontal>
            <ChartBar
              data={Array.from(data.keys())
                .sort()
                .map((tagName) => ({
                  name: "Tag",
                  x: tagName,
                  y: data.get(tagName),
                }))}
            />
          </ChartGroup>
        </Chart>
      </div>
    </>
  );
};
