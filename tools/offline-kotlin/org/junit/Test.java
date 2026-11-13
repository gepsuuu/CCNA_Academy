package org.junit;
import java.lang.annotation.*;
/** API-compatible stand-in for org.junit.Test (JUnit 4.13.2 unavailable offline). */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface Test {}
