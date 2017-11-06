package rocks.inspectit.android.instrument.dex;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;

import com.google.common.collect.Lists;

import rocks.inspectit.agent.android.delegation.AndroidAgentDelegator;
import rocks.inspectit.agent.android.delegation.DelegationAnnotation;
import rocks.inspectit.agent.android.delegation.DelegationPoint;
import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.dex.impl.DexActivityInstrumenter;
import rocks.inspectit.android.instrument.dex.impl.DexSensorInstrumenter;
import rocks.inspectit.android.instrument.dex.impl.DexSensorMethodInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexInstrumenter {
	private TraceCollectionConfiguration traceConfiguration;

	private IDexClassInstrumenter[] classInstrumenters;
	private IDexMethodInstrumenter[] methodInstrumenters;
	private IDexMethodImplementationInstrumenter[] implementationInstrumenters;

	private Map<DelegationPoint, MethodReference> delegationPointMapping;

	public DexInstrumenter(InstrumentationConfiguration config) {
		this.traceConfiguration = config.getXmlConfiguration().getTraceCollectionList();

		this.delegationPointMapping = new HashMap<>();
		loadDelegationMap();

		this.classInstrumenters = new IDexClassInstrumenter[] { new DexActivityInstrumenter(delegationPointMapping) };
		this.methodInstrumenters = new IDexMethodInstrumenter[] { new DexSensorInstrumenter(delegationPointMapping, traceConfiguration) };
		this.implementationInstrumenters = new IDexMethodImplementationInstrumenter[] { new DexSensorMethodInstrumenter(traceConfiguration, delegationPointMapping) }; // new
		// DexNetworkInstrumenter()
		// };
	}

	public void instrument(File input, File output) throws IOException {
		DexBackedDexFile dex = DexFileFactory.loadDexFile(input, Opcodes.forApi(19));

		final List<ClassDef> classes = Lists.newArrayList();

		// apply all class instrumenters
		for (ClassDef classDef : dex.getClasses()) {
			for (IDexClassInstrumenter instrumenter : classInstrumenters) {
				if (instrumenter.isTargetClass(classDef)) {
					classDef = instrumenter.instrumentClass(classDef);
				}
			}
			classes.add(classDef);
		}

		// apply all method instrumenters
		int k = 0;
		for (ClassDef classDef : classes) {
			List<Method> methods = Lists.newArrayList();
			for (Method method : classDef.getMethods()) {
				if (method.getImplementation() != null) {
					for (IDexMethodInstrumenter instrumenter : methodInstrumenters) {
						if (instrumenter.isTargetMethod(method)) {
							method = instrumenter.instrumentMethod(classDef, method);
						}
					}
				}
				methods.add(method);
			}

			// apply method implementation instrumenters
			int j = 0;
			for (Method method : methods) {
				if (method.getImplementation() != null) {
					for (IDexMethodImplementationInstrumenter instrumenter : implementationInstrumenters) {
						Pair<Boolean, MutableMethodImplementation> impl = instrumenter.instrument(method);
						if (impl.getKey()) {
							methods.set(j, new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(),
									method.getAnnotations(), impl.getValue()));
						}
					}
				}
				j++;
			}

			classDef = new ImmutableClassDef(classDef.getType(), classDef.getAccessFlags(), classDef.getSuperclass(), classDef.getInterfaces(), classDef.getSourceFile(), classDef.getAnnotations(),
					classDef.getFields(), methods);
			classes.set(k++, classDef);
		}

		DexFileFactory.writeDexFile(output.getAbsolutePath(), new DexFile() {
			@Override
			public Set<? extends ClassDef> getClasses() {
				return new AbstractSet<ClassDef>() {
					@Override
					public Iterator<ClassDef> iterator() {
						return classes.iterator();
					}

					@Override
					public int size() {
						return classes.size();
					}
				};
			}

			@Override
			public Opcodes getOpcodes() {
				return Opcodes.getDefault();
			}
		});
	}

	private void loadDelegationMap() {
		this.delegationPointMapping.clear();

		for (java.lang.reflect.Method delMethod : AndroidAgentDelegator.class.getDeclaredMethods()) {
			if (delMethod.isAnnotationPresent(DelegationAnnotation.class)) {
				MethodReference methRef = DexInstrumentationUtil.getMethodReference(AndroidAgentDelegator.class, delMethod.getName(), "V", delMethod.getParameterTypes());

				this.delegationPointMapping.put(delMethod.getAnnotation(DelegationAnnotation.class).correspondsTo(), methRef);
			}
		}
	}

}
