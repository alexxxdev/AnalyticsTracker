package com.github.alexxxdev.analyticsTracker;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by aderendyaev on 12.12.17.
 */

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD})
public @interface AnalyticsEnumAttr {
    String value() default "";
}