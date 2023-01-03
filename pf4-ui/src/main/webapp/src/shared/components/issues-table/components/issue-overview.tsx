import React, { useMemo } from "react";

import {
  Badge,
  Button,
  Card,
  CardBody,
  Grid,
  GridItem,
  Stack,
  StackItem,
  Truncate,
} from "@patternfly/react-core";
import {
  TableComposable,
  Tbody,
  Td,
  Th,
  Thead,
  Tr,
} from "@patternfly/react-table";

import { IssueDto } from "@app/api/application-issues";
import { useFilesQuery } from "@app/queries/files";
import { SimpleMarkdown } from "@app/shared/components";
import { getMarkdown } from "@app/utils/rule-utils";

interface IIssueOverviewProps {
  issue: IssueDto;
  onShowFile: (fileId: string) => void;
}

export const IssueOverview: React.FC<IIssueOverviewProps> = ({
  issue,
  onShowFile,
}) => {
  return (
    <Stack hasGutter>
      {issue.affectedFiles.map((affectedFile, index) => (
        <StackItem key={index}>
          <Grid hasGutter>
            <GridItem md={5}>
              <Card isCompact isFullHeight>
                <CardBody>
                  <TableComposable aria-label="Files table" variant="compact">
                    <Thead>
                      <Tr>
                        <Th>File</Th>
                        <Th>Incidents found</Th>
                      </Tr>
                    </Thead>
                    <Tbody>
                      {affectedFile.files.map((file) => (
                        <Tr key={file.fileId}>
                          <Td dataLabel="File">
                            <FileLink
                              fileId={file.fileId}
                              defaultText={file.fileName}
                              onClick={() => onShowFile(file.fileId)}
                            />
                          </Td>
                          <Td dataLabel="Incidents found" width={10}>
                            <Badge isRead>{file.occurrences}</Badge>
                          </Td>
                        </Tr>
                      ))}
                    </Tbody>
                  </TableComposable>
                </CardBody>
              </Card>
            </GridItem>
            <GridItem md={7}>
              <Card isCompact isFullHeight>
                <CardBody>
                  <SimpleMarkdown
                    children={getMarkdown(
                      affectedFile.description,
                      issue.links
                    )}
                  />
                </CardBody>
              </Card>
            </GridItem>
          </Grid>
        </StackItem>
      ))}
    </Stack>
  );
};

interface IFileLinkProps {
  fileId: string;
  defaultText: string;
  onClick: () => void;
}

export const FileLink: React.FC<IFileLinkProps> = ({
  fileId,
  defaultText,
  onClick,
}) => {
  const allFiles = useFilesQuery();
  const file = useMemo(() => {
    const result = allFiles.data?.find((e) => e.id === fileId);
    return result;
  }, [allFiles.data, fileId]);

  return (
    <>
      {file ? (
        <Button variant="link" onClick={onClick}>
          <Truncate content={defaultText || file.prettyPath} />
        </Button>
      ) : (
        defaultText
      )}
    </>
  );
};
