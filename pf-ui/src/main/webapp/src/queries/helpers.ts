import { UseQueryOptions, useQuery } from "@tanstack/react-query";

const defaultTimeout =
  process.env.REACT_APP_DATA_SOURCE_TIMEOUT !== undefined
    ? Number(process.env.REACT_APP_DATA_SOURCE_TIMEOUT)
    : 1000;

const mockPromise = <TQueryFnData>(
  data: TQueryFnData,
  timeout = defaultTimeout,
  success = true
) => {
  return new Promise<TQueryFnData>((resolve, reject) => {
    setTimeout(() => {
      if (success) {
        resolve(data);
      } else {
        reject({ message: "Error" });
      }
    }, timeout);
  });
};

export const useMockableQuery = <
  TQueryFnData = unknown,
  TError = unknown,
  TData = TQueryFnData
>(
  params: UseQueryOptions<TQueryFnData, TError, TData>,
  mockData: TQueryFnData,
  offlineData: TQueryFnData
) => {
  return useQuery<TQueryFnData, TError, TData>({
    ...params,
    queryFn:
      (process.env.REACT_APP_DATA_SOURCE !== "mock" &&
        process.env.REACT_APP_DATA_SOURCE !== "offline") ||
      (window as any)["WINDUP_SETTINGS"].forceOnline
        ? params.queryFn
        : () => {
            return mockPromise(
              process.env.REACT_APP_DATA_SOURCE === "offline"
                ? offlineData
                : mockData
            );
          },
  });
};
