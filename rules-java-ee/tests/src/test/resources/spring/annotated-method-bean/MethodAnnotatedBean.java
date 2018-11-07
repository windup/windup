package com.whatever.windup;

import java.util.Calendar;
import org.springframework.context.annotation.Bean;
import com.whatever.windup.MyInterface;
import com.whatever.windup.MyImplementation;


public class MethodAnnotatedBean {
    public Calendar mycalendar;

    @Bean
    public MyInterface anotherExporter(int number) {
        return new MyImplementation();
    }
}
