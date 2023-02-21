package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationHardcodedIpAddressesDto {
    public String applicationId;
    public List<FileDto> files;

    public static class FileDto {
        public String fileId;
        public String lineNumber;
        public String columnNumber;
        public String ipAddress;
    }

}
