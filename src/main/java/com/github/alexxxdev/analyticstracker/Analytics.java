package com.github.alexxxdev.analyticstracker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by aderendyaev on 12.12.17.
 */

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface Analytics {
}