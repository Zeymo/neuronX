package io.zeymo.commons.annotation;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface LockProtected {
	String desc() default "";
}