export interface LabelDto {
  id: string;
  name: string;
  description?: string;
  supported: string[];
  unsuitable: string[];
  neutral: string[];
}
