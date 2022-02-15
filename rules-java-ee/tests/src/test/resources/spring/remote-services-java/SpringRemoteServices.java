package com.whatever.server;

import com.whatever.api.CabBookingService;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiServiceExporter;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.springframework.remoting.jaxws.SimpleJaxWsServiceExporter;
import org.springframework.jms.remoting.JmsInvokerServiceExporter;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import java.util.Calendar;
import com.whatever.api.Booking;

public class SpringRemoteServices {

    @Bean
    RmiServiceExporter rmiExporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setService(implementation);
        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }

    @Bean
    HessianServiceExporter hessianExporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        HessianServiceExporter exporter = new HessianServiceExporter();
        exporter.setService(implementation);
        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }

    @Bean
    HttpInvokerServiceExporter httpInvokerExporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService(implementation);
        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }

    @Bean
    SimpleJaxWsServiceExporter jaxwsExporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        SimpleJaxWsServiceExporter exporter = new SimpleJaxWsServiceExporter();
        exporter.setService(implementation);
        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }

    @Bean
    JmsInvokerServiceExporter jmsExporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        JmsInvokerServiceExporter exporter = new JmsInvokerServiceExporter();
        exporter.setService(implementation);
        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }

    @Bean
    AmqpInvokerServiceExporter amqpExporter(CabBookingService implementation) {
        Class<CabBookingService> serviceInterface = CabBookingService.class;
        AmqpInvokerServiceExporter exporter = new AmqpInvokerServiceExporter();
        exporter.setService(implementation);
        exporter.setServiceInterface(serviceInterface);
        exporter.setServiceName(serviceInterface.getSimpleName());
        exporter.setRegistryPort(1099);
        return exporter;
    }
}
