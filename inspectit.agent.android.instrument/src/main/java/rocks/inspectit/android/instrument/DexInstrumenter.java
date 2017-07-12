package rocks.inspectit.android.instrument;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.util.MethodUtil;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;
import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexInstrumenter {

	private DexSensorInstrumenter dexSensorInstrumenter;
	private DexNetworkInstrumenter dexNetworkInstrumenter;

	private TraceCollectionConfiguration traceConfiguration;

	public DexInstrumenter(InstrumentationConfiguration config) {
		this.dexSensorInstrumenter = new DexSensorInstrumenter();
		this.dexNetworkInstrumenter = new DexNetworkInstrumenter();

		this.traceConfiguration = config.getXmlConfiguration().getTraceCollectionList();
	}

	public void instrument(File input, File output) throws IOException {
		DexBackedDexFile dex = DexFileFactory.loadDexFile(input, Opcodes.forApi(19));

		final List<ClassDef> classes = Lists.newArrayList();

		// generate agent invocation statements
		for (ClassDef classDef : dex.getClasses()) {
			boolean modifiedMethod = false;
			List<Method> methods = Lists.newArrayList();
			String superClass = classDef.getSuperclass();
			if ((superClass != null) && (superClass.equals(DexInstrumentationUtil.getType(Activity.class)))) {
				boolean foundOnCreate = false;
				for (Method method : classDef.getMethods()) {
					String name = method.getName();
					MethodImplementation implementation = method.getImplementation();
					if ((implementation != null) && name.equals("onCreate")) {
						foundOnCreate = true;
						MethodImplementation newImplementation = null;
						// if(!method.getName().equals("<init>"))
						newImplementation = instrument(method);

						if (newImplementation != null) {
							modifiedMethod = true;
							methods.add(new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(),
									method.getAnnotations(), newImplementation));
						} else {
							methods.add(method);
						}
					} else {
						methods.add(method);
					}
				}

				if (!foundOnCreate) {
					modifiedMethod = true;
					MethodImplementation nImpl = generateOnCreateMethodImpl();
					List<ImmutableMethodParameter> parameters = Lists.newArrayList(new ImmutableMethodParameter(DexInstrumentationUtil.getType(Bundle.class), new HashSet<Annotation>(), "bundle"));
					Method onCreateMethod = new ImmutableMethod(classDef.getType(), "onCreate", parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl);
					methods.add(onCreateMethod);
				}
			} else {
				for (Method method : classDef.getMethods()) {
					boolean methodWithSensor = isTracedMethod(method.getDefiningClass(), method.getName(), method.getParameterTypes());

					if ((method.getImplementation() != null) && methodWithSensor) {
						modifiedMethod = true;
						methods.add(dexSensorInstrumenter.instrumentMethod(method));
					} else {
						methods.add(method);
					}
				}
			}

			for (int i = 0; i < methods.size(); i++) {
				Pair<Boolean, ? extends Method> result = dexNetworkInstrumenter.instrumentMethod(methods.get(i));
				if (result.getLeft()) {
					// instrumented
					modifiedMethod = true;
					methods.set(i, result.getRight());
				}
			}

			if (!modifiedMethod) {
				classes.add(classDef);
			} else {
				classes.add(new ImmutableClassDef(classDef.getType(), classDef.getAccessFlags(), classDef.getSuperclass(), classDef.getInterfaces(), classDef.getSourceFile(),
						classDef.getAnnotations(), classDef.getFields(), methods));
			}
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

	private boolean isTracedMethod(String clazz, String method, List<? extends CharSequence> parameters) {
		List<String> patterns = traceConfiguration.getPackages();

		for (String pattern : patterns) {
			String[] patternSplit = pattern.split("\\.");
			String[] matchSplit = (clazz.replaceAll("/", ".").substring(1, clazz.length() - 1) + "." + method).split("\\.");

			int k = 0;
			for (String part : patternSplit) {

				if (k >= matchSplit.length) {
					break;
				}

				if (!part.equals("*")) {
					if (part.equals("**")) {
						return true;
					} else {
						if (!part.equals(matchSplit[k])) {
							break;
						}
					}
				}

				++k;
			}
		}

		return false;
	}

	private MethodImplementation generateOnCreateMethodImpl() {
		MutableMethodImplementation impl = new MutableMethodImplementation(2);

		// 2 because there is 1 register parameter 1 this reference -> parameter count is this inclusive
		int thisRegister = DexInstrumentationUtil.getThisRegister(impl.getRegisterCount(), 2);
		int parameterRegister = 1; // in highest register

		impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));

		MethodReference methRef = DexInstrumentationUtil.getMethodReference(Activity.class, "onCreate", "V", Bundle.class);

		// first register is this register -> because 1 is parameter register
		BuilderInstruction35c superInvocation = new BuilderInstruction35c(Opcode.INVOKE_SUPER, 2, thisRegister, parameterRegister, 0, 0, 0, methRef);

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));
		impl.addInstruction(0, superInvocation);

		return impl;
	}

	private MethodImplementation instrument(Method method) {
		MutableMethodImplementation impl = new MutableMethodImplementation(method.getImplementation());

		int paramRegisters = MethodUtil.getParameterRegisterCount(method);
		int thisRegister = DexInstrumentationUtil.getThisRegister(impl.getRegisterCount(), paramRegisters);

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));

		return impl;
	}

	private BuilderInstruction createAgentInitInvocation(int thisRegister) {
		MethodReference methodReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "initAgent", "V", Activity.class);
		BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, thisRegister, 0, 0, 0, 0, methodReference);

		return invokeInstruction;
	}

}
