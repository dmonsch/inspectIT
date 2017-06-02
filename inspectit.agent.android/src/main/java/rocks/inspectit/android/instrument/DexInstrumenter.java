package rocks.inspectit.android.instrument;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.util.MethodUtil;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;
import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexInstrumenter {

	public DexInstrumenter(InstrumentationConfiguration config) {
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

	private MethodImplementation generateOnCreateMethodImpl() {
		MutableMethodImplementation impl = new MutableMethodImplementation(2);

		int thisRegister = 0;
		int parameterRegister = 1; // in highest register

		impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));

		List<String> parameters = Lists.newArrayList(DexInstrumentationUtil.getType(Bundle.class));
		MethodReference methRef = new ImmutableMethodReference(DexInstrumentationUtil.getType(Activity.class), "onCreate", parameters, "V");

		// first register is this register -> because 1 is parameter register
		BuilderInstruction35c superInvocation = new BuilderInstruction35c(Opcode.INVOKE_SUPER, 2, thisRegister, parameterRegister, 0, 0, 0, methRef);

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));
		impl.addInstruction(0, superInvocation);

		return impl;
	}

	private MethodImplementation instrument(Method method) {
		MutableMethodImplementation impl = new MutableMethodImplementation(method.getImplementation());

		int paramRegisters = MethodUtil.getParameterRegisterCount(method);
		int thisRegister = method.getImplementation().getRegisterCount() - paramRegisters;

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));

		return impl;
	}

	private BuilderInstruction createAgentInitInvocation(int thisRegister) {

		// BuilderInstruction21c loadStringInstruction = new BuilderInstruction21c(Opcode.CONST_STRING, 0, new ImmutableStringReference("Tag"));
		// BuilderInstruction21c loadStringInstruction2 = new
		// BuilderInstruction21c(Opcode.CONST_STRING, 1, new ImmutableStringReference("Message"));

		// ImmutableInstruction22c loadInstruction = new ImmutableInstruction22c(opcode, registerA,
		// registerB, reference);

		List<String> parameterList = new ArrayList<String>();
		parameterList.add(getType(Activity.class));

		ImmutableMethodReference methodReference = new ImmutableMethodReference(getType(AndroidAgent.class), "initAgent", Collections.unmodifiableList(parameterList),
				"V");
		BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, thisRegister, 0, 0, 0, 0, methodReference);

		// l.add(loadStringInstruction2);
		// l.add(invokeInstruction);

		return invokeInstruction;
	}

	// UTILS
	private String getType(Class<?> clz) {
		return "L" + clz.getName().replaceAll("\\.", "/") + ";";
	}

}
