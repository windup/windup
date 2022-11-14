package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class IgnoredFilesDto {
    public String applicationId;
    public List<FileDto> ignoredFiles;

    public static class FileDto {
        public String fileName;
        public String filePath;
        public String reason;
    }
}
