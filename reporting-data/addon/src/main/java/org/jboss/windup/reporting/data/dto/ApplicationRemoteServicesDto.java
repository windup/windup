package org.jboss.windup.reporting.data.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationRemoteServicesDto {
    private String applicationId;
    private List<JaxRsServiceDto> jaxRsServices;
    private List<JaxWsServiceDto> jaxWsServices;
    private List<EjbRemoteServiceDto> ejbRemoteServices;
    private List<RmiServiceDto> rmiServices;

    @Data
    public static class JaxRsServiceDto {
        private String path;
        private String interfaceName;
        private String interfaceFileId;
    }

    @Data
    public static class JaxWsServiceDto {
        private String interfaceName;
        private String interfaceFileId;
        private String implementationName;
        private String implementationFileId;
    }

    @Data
    public static class EjbRemoteServiceDto {
        private String interfaceName;
        private String interfaceFileId;
        private String implementationName;
        private String implementationFileId;
    }

    @Data
    public static class RmiServiceDto {
        private String interfaceName;
        private String interfaceFileId;
        private String implementationName;
        private String implementationFileId;
    }
}
