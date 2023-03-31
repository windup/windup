import { LinkDto } from "@app/api/issues";
import { TechnologyDto } from "@app/api/rule";

export const getMarkdown = (body: string, links: LinkDto[]): string => {
  const formattedLinks = links
    .map((link, index) => `${index + 1}. [${link.title}](${link.href})`)
    .join("\n");
  return [body, formattedLinks].join("\n");
};

export const technologiesToArray = (technologies: TechnologyDto[]) => {
  const technologyVersionMap: Map<string, Set<string>> = new Map();

  technologies.forEach((technology) => {
    let versions: string[] = [];

    if (technology.versionRange && technology.versionRange.length > 0) {
      const versionsRange = technology.versionRange.split(",");

      const start =
        versionsRange[0] && !versionsRange[0].startsWith("(")
          ? //eslint-disable-next-line
            versionsRange[0].replace(/[(\[\])]/g, "")
          : "";
      const end =
        versionsRange[1] && !versionsRange[1].endsWith(")")
          ? //eslint-disable-next-line
            versionsRange[0].replace(/[(\[\])]/g, "")
          : "";

      versions = [start, end].filter((version) => version !== "");
    }

    const newVersions = new Set(technologyVersionMap.get(technology.id));
    versions.forEach((f) => newVersions.add(f));

    technologyVersionMap.set(technology.id, newVersions);
  });

  //
  const result: string[] = [];
  technologyVersionMap.forEach((versions, technology) => {
    result.push(technology);
    versions.forEach((version) => {
      result.push(`${technology} ${version}`);
    });
  });
  return result;
};
