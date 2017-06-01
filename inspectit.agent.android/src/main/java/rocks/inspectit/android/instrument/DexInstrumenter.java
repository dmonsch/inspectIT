package rocks.inspectit.android.instrument;

import java.io.File;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.util.MethodUtil;

import com.google.common.collect.Lists;

import android.app.Activity;
import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;

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

		for (ClassDef classDef : dex.getClasses()) {
			boolean modifiedMethod = false;
			List<Method> methods = Lists.newArrayList();
			if (classDef.getType().contains("MainActivity")) {
				for (Method method : classDef.getMethods()) {
					String name = method.getName();

					MethodImplementation implementation = method.getImplementation();
					if ((implementation != null) && name.equals("onCreate")) {
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

	private MethodImplementation instrument(Method method) {
		MutableMethodImplementation impl = new MutableMethodImplementation(method.getImplementation());

		int paramRegisters = MethodUtil.getParameterRegisterCount(method);
		int thisRegister = method.getImplementation().getRegisterCount() - paramRegisters;

		impl.addInstruction(0, createHelloWorldPrint(thisRegister));

		return impl;
	}

	private BuilderInstruction createHelloWorldPrint(int thisRegister) {

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
