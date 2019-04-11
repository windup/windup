package org.whatever.windup;

import java.util.Calendar;
import org.springframework.context.annotation.Bean;
import org.whatever.windup.MyInterface;
import org.whatever.windup.MyOtherInterface;
import org.whatever.windup.MyImplementation;
import org.whatever.windup.MyOtherImplementation;

public class MethodAnnotatedBean {

    @Bean
    public MyInterface anotherExporter(int number) {
        return new MyImplementation();
    }

    @Bean
    public MyOtherInterface secondExporter(int value) {
        return new MyOtherImplementation();
    }
}
