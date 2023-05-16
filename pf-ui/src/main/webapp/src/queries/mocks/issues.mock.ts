import { ApplicationIssuesDto } from "@app/api/issues";

export let MOCK_ISSUES: ApplicationIssuesDto[];

if (
  process.env.NODE_ENV === "test" ||
  process.env.REACT_APP_DATA_SOURCE === "mock"
) {
  const issuesApp1: ApplicationIssuesDto = {
    applicationId: "app-1",
    issues: {
      optional: [],
      potential: [],
      information: [],
      mandatory: [
        {
          id: "app1-issue-1",
          name: "Issue name",
          effort: {
            type: "Architectural",
            points: 10,
            description: "Level description",
          },
          totalIncidents: 1,
          totalStoryPoints: 1,
          ruleId: "rule-1",
          links: [
            { title: "Link1", href: "http://windup1.com" },
            { title: "Link2", href: "http://windup2.com" },
          ],
          affectedFiles: [
            {
              description: "Hint text",
              files: [
                {
                  fileId: "file-1",
                  fileName: "file1",
                  occurrences: 1,
                },
              ],
            },
          ],
        },
      ],
      "cloud-mandatory": [],
      "cloud-optional": [],
    },
  };

  const issuesApp2: ApplicationIssuesDto = {
    applicationId: "app-2",
    issues: {
      optional: [
        {
          id: "app2-issue-1",
          name: "Issue name",
          effort: {
            type: "Complex",
            points: 10,
            description: "Level description",
          },
          totalIncidents: 1,
          totalStoryPoints: 2,
          ruleId: "rule-2",
          links: [
            { title: "Link1", href: "http://windup1.com" },
            { title: "Link2", href: "http://windup2.com" },
          ],
          affectedFiles: [
            {
              description: "Hint text",
              files: [
                {
                  fileId: "file-2",
                  fileName: "file2",
                  occurrences: 1,
                },
              ],
            },
          ],
        },
      ],
      potential: [],
      information: [],
      mandatory: [],
      "cloud-mandatory": [],
      "cloud-optional": [],
    },
  };

  MOCK_ISSUES = [issuesApp1, issuesApp2];
}
