package rocks.inspectit.agent.android.module.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining methods of a module which should be executed
 * periodically.
 * 
 * @author David Monschein
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExecutionProperty {
	/**
	 * Amount of time between the executions
	 * 
	 * @return the interval length between executions of the method
	 */
	public long interval() default 60000L;
}
