package com.whatever.windup;

import org.springframework.stereotype.Component;
import com.whatever.windup.MyInterface;

@Component
public class ClassAnnotatedBean implements MyInterface {

    public String dummyMethod(int number) {
        return "void";
    }
}
