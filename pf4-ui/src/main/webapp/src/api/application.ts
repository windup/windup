export interface ApplicationDto {
  id: string;
  name: string;
  isVirtual: boolean;
  tags: string[];
  storyPoints: number;
  incidents: {
    [key: string]: number;
  };
}
