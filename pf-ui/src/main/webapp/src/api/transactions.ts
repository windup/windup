export interface ApplicationTransactionsDto {
  applicationId: string;
  transactions: TransactionDto[];
}

export interface TransactionDto {
  className: string;
  classFileId?: string;
  methodName?: string;
  stackTraces: StackTraceDto[];
}

export interface StackTraceDto {
  sql?: string;
  lineNumber?: number;
}
