package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationUnparsableFilesDto {
    public String applicationId;
    public List<SubProjectDto> subProjects;

    public static class SubProjectDto {
        public String path;
        public List<UnparsableFileDto> unparsableFiles;
    }

    public static class UnparsableFileDto {
        public String fileId;
        public String fileName;
        public String filePath;
        public String parseError;
    }

}
