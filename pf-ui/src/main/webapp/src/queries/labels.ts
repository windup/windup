import { useCallback } from "react";

import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { LabelDto } from "@app/api/label";

import { useMockableQuery } from "./helpers";
import { MOCK_LABELS } from "./mocks/labels.mock";

export const useLabelsQuery = (): UseQueryResult<LabelDto[], AxiosError> => {
  const sortListCallback = useCallback((data: LabelDto[]): LabelDto[] => {
    return data.sort((a, b) => b.name.localeCompare(a.name));
  }, []);

  return useMockableQuery<LabelDto[], AxiosError>(
    {
      queryKey: ["labels"],
      queryFn: async () => (await axios.get<LabelDto[]>("/labels")).data,
      select: sortListCallback,
    },
    MOCK_LABELS,
    (window as any)["labels"]
  );
};
