package com.cydeo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultExceptionMessage { // If any certain things happen in the method, I am gonna put this annotation on top of the method, and this method is gonna throw that exception

    String defaultMessage() default "";

}