export interface TagDto {
  name: string;
  title: string;
  isRoot: boolean;
  isPseudo: boolean;
  parentsTagNames: string[];
}
