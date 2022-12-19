package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationIgnoredFilesDto {
    public String applicationId;
    public List<IgnoredFileDto> ignoredFiles;

    public static class IgnoredFileDto {
        public String fileName;
        public String filePath;
        public String reason;
    }
}
