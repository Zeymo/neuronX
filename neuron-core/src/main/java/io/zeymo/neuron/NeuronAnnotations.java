package io.zeymo.neuron;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NeuronAnnotations {
	@Retention(RetentionPolicy.CLASS)
	public static @interface GCFree {
		String value() default "";
	}

	@Retention(RetentionPolicy.CLASS)
	public static @interface NotGCFree {
		String value() default "";

	}

	@Retention(RetentionPolicy.CLASS)
	public static @interface Singleton {
		String value() default "";
	}

}
