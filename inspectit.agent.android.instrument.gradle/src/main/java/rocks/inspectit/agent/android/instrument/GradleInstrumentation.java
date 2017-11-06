package rocks.inspectit.agent.android.instrument;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * @author David Monschein
 *
 */
public class GradleInstrumentation implements Plugin<Project> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void apply(Project arg0) {
		arg0.getExtensions().create("inspectIT", InstrumentationExtension.class);
		InstrumentationTask task = arg0.getTasks().create("instrumentAndroid", InstrumentationTask.class);
		task.dependsOn(arg0.getTasks().findByName("assemble"));
		task.mustRunAfter(arg0.getTasks().findByName("assemble"));
	}

}
