package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationHardcodedIpAddressesDto {
    private String applicationId;
    private List<FileDto> files;

    @Data
    public static class FileDto {
        private String fileId;
        private int lineNumber;
        private int columnNumber;
        private String ipAddress;
    }
}
