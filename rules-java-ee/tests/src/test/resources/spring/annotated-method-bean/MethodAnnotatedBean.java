package me.whatever.windup;

import java.util.Calendar;
import org.springframework.context.annotation.Bean;
import me.whatever.windup.MyInterface;
import me.whatever.windup.MyOtherInterface;
import me.whatever.windup.MyImplementation;
import me.whatever.windup.MyOtherImplementation;

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
