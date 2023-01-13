package org.jboss.windup.reporting.data.dto;

import java.util.List;

public class ApplicationRemoteServicesDto {
    public String applicationId;
    public List<JaxRsServiceDto> jaxRsServices;
    public List<JaxWsServiceDto> jaxWsServices;
    public List<EjbRemoteServiceDto> ejbRemoteServices;
    public List<RmiServiceDto> rmiServices;

    public static class JaxRsServiceDto {
        public String path;
        public String interfaceName;
        public String interfaceFileId;
    }

    public static class JaxWsServiceDto {
        public String interfaceName;
        public String interfaceFileId;
        public String implementationName;
        public String implementationFileId;
    }

    public static class EjbRemoteServiceDto {
        public String interfaceName;
        public String interfaceFileId;
        public String implementationName;
        public String implementationFileId;
    }

    public static class RmiServiceDto {
        public String interfaceName;
        public String interfaceFileId;
        public String implementationName;
        public String implementationFileId;
    }
}
