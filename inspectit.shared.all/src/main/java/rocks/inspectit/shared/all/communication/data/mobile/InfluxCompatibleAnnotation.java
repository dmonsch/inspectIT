package rocks.inspectit.shared.all.communication.data.mobile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author David Monschein
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface InfluxCompatibleAnnotation {
	String measurement() default "";

	boolean tag() default false;

	String key() default "";
}
