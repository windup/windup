package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationTransactionsDto {
    public String applicationId;
    public List<TransactionDto> transactions;

    public static class TransactionDto {
        public String className;
        public String classFileId;
        public String methodName;
        public List<StackTraceDto> stackTraces;
    }

    public static class StackTraceDto {
        public String sql;
        public int lineNumber;
    }

}
