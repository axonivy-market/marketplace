package com.axonivy.market.core.aop.annotation;

import com.axonivy.market.core.enums.MatomoTrackerSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TrackApiCallFromNeo {

  MatomoTrackerSource value();
}
