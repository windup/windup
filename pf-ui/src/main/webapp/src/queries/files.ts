import { UseQueryResult } from "@tanstack/react-query";
import axios, { AxiosError } from "axios";

import { FileDto } from "@app/api/file";
import { FileContentDto } from "@app/api/file-content";

import { useMockableQuery } from "./helpers";
import { MOCK_APP_FILES, MOCK_APP_FILES_CONTENT } from "./mocks/files.mock";

export const useFilesQuery = (): UseQueryResult<FileDto[], AxiosError> => {
  return useMockableQuery<FileDto[], AxiosError>(
    {
      queryKey: ["files"],
      queryFn: async () => (await axios.get<FileDto[]>("/files")).data,
    },
    MOCK_APP_FILES,
    (window as any)["files"]
  );
};

export const useFileQuery = (fileId: string) => {
  return useMockableQuery<FileContentDto, AxiosError, FileContentDto>(
    {
      queryKey: ["files", fileId],
      queryFn: async () =>
        (await axios.get<FileContentDto>(`/files/${fileId}`)).data,
    },
    MOCK_APP_FILES_CONTENT[fileId],
    (window as any)["files_by_id"]
      ? (window as any)["files_by_id"][fileId]
      : undefined
  );
};
