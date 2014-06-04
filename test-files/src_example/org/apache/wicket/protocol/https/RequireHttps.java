package org.apache.wicket.protocol.https;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface RequireHttps{
}
