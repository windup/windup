package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationJBPMsDto {
    private String applicationId;
    private List<JBPMDto> jbpms;

    @Data
    public static class JBPMDto {
        private String fileId;
        private String fileName;
        private String processName;
        private Integer processNoteCount;
        private Integer processDecisionCount;
        private Integer processStateCount;
        private Integer processTaskCount;
        private Integer processSubProcessCount;
        private List<ActionHandlerDto> actionHandlers;
        private List<DecisionHandlerDto> decisionHandlers;
    }

    @Data
    public static class ActionHandlerDto {
        private String fileId;
        private String fileName;
    }

    @Data
    public static class DecisionHandlerDto {
        private String fileId;
        private String fileName;
    }
}
