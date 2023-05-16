export interface FileDto {
  id: string;
  fullPath: string;
  prettyPath: string;
  prettyFileName: string;
  sourceType: string;
  storyPoints: number;
  hints: HintDto[];
  tags: TagDto[];
  classificationsAndHintsTags: string[];
}

export interface HintDto {
  line: number;
  title: string;
  ruleId: string;
  content: string;
  links: {
    title: string;
    href: string;
  }[];
}

export interface TagDto {
  name: string;
  version: string;
  level: "IMPORTANT" | "INFORMATIONAL";
}
