package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationUnparsableFilesDto {
    private String applicationId;
    private List<SubProjectDto> subProjects;

    @Data
    public static class SubProjectDto {
        private String path;
        private List<UnparsableFileDto> unparsableFiles;
    }

    @Data
    public static class UnparsableFileDto {
        private String fileId;
        private String fileName;
        private String filePath;
        private String parseError;
    }

}
