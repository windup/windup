package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationIgnoredFilesDto {
    private String applicationId;
    private List<IgnoredFileDto> ignoredFiles;

    @Data
    public static class IgnoredFileDto {
        private String fileName;
        private String filePath;
        private String reason;
    }
}
