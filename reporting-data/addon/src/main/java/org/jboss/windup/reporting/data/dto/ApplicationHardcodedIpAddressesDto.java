package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationHardcodedIpAddressesDto {
    public String applicationId;
    public List<FileDto> files;

    public static class FileDto {
        public String fileId;
        public int lineNumber;
        public int columnNumber;
        public String ipAddress;
    }
}
