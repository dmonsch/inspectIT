package rocks.inspectit.agent.android.delegation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which is used by the {@link AndroidAgentDelegator} to map methods to a
 * {@link DelegationPoint}.
 *
 * @author David Monschein
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DelegationAnnotation {
	/**
	 * Point to which the annotated method corresponds.
	 */
	DelegationPoint correspondsTo();
}
