package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationTransactionsDto {
    private String applicationId;
    private List<TransactionDto> transactions;

    @Data
    public static class TransactionDto {
        private String className;
        private String classFileId;
        private String methodName;
        private List<StackTraceDto> stackTraces;
    }

    @Data
    public static class StackTraceDto {
        private String sql;
        private int lineNumber;
    }

}
