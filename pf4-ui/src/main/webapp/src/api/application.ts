export interface ApplicationDto {
  id: string;
  name: string;
  tags: string[];
  storyPoints: number;
  incidents: {
    [key: string]: number;
  };
}
