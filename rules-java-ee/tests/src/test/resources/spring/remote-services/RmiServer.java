package com.baeldung.server;

import com.baeldung.api.CabBookingService;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiServiceExporter;
import java.util.Calendar;
import com.baeldung.api.Booking;

public class RmiServer {

    @Bean
    RmiServiceExporter exporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setService(implementation);


        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }
}
