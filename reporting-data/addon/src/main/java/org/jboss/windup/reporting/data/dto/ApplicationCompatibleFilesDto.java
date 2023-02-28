package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationCompatibleFilesDto {
    public String applicationId;
    public List<ArtifactDto> artifacts;

    public static class ArtifactDto {
        public String name;
        public List<FileDto> files;
    }

    public static class FileDto {
        public String fileId;
        public String fileName;
    }

}
