package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationCompatibleFilesDto {
    private String applicationId;
    private List<ArtifactDto> artifacts;

    @Data
    public static class ArtifactDto {
        private String name;
        private List<FileDto> files;
    }

    @Data
    public static class FileDto {
        private String fileId;
        private String fileName;
    }

}
