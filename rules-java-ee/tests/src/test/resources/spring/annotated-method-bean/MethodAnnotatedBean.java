package com.whatever.windup;

import java.util.Calendar;
import org.springframework.context.annotation.Bean;
import com.whatever.windup.MyInterface;
import com.whatever.windup.MyOtherInterface;
import com.whatever.windup.MyImplementation;
import com.whatever.windup.MyOtherImplementation;

import javax.annotation.ManagedBean;


public class MethodAnnotatedBean {
    public Calendar mycalendar;

    @Bean
    public MyInterface anotherExporter(int number) {
        return new MyImplementation();
    }

    @Bean
    public MyOtherInterface secondExporter(int value) {
        return new MyOtherImplementation();
    }
}
