package rocks.inspectit.android.instrument.dex.impl;

import java.util.HashSet;
import java.util.List;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.util.MethodUtil;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;
import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.android.instrument.dex.IDexClassInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexActivityInstrumenter implements IDexClassInstrumenter {

	private static final String METHOD_ONCREATE = "onCreate";
	private static final String METHOD_ONDESTROY = "onDestroy";

	@Override
	public boolean isTargetClass(ClassDef clazz) {
		String superClass = clazz.getSuperclass();
		return (superClass != null) && (superClass.equals(DexInstrumentationUtil.getType(Activity.class)));
	}

	@Override
	public ClassDef instrumentClass(ClassDef clazz) {
		if (!isTargetClass(clazz)) {
			return clazz;
		}

		List<Method> methods = Lists.newArrayList();

		boolean foundOnCreate = false;
		boolean foundOnDestroy = false;

		for (Method meth : clazz.getMethods()) {
			if (meth.getImplementation() != null) {
				if (METHOD_ONCREATE.equals(meth.getName())) {
					foundOnCreate = true;
				} else if (METHOD_ONDESTROY.equals(meth.getName())) {
					foundOnDestroy = true;
				}
				methods.add(instrumentMethod(meth));
			}
		}

		if (!foundOnCreate) {
			MethodImplementation nImpl = generateOnCreateMethodImpl();
			List<ImmutableMethodParameter> parameters = Lists.newArrayList(new ImmutableMethodParameter(DexInstrumentationUtil.getType(Bundle.class), new HashSet<Annotation>(), "bundle"));
			methods.add(new ImmutableMethod(clazz.getType(), "onCreate", parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl));
		}

		if (!foundOnDestroy) {
			MethodImplementation nImpl = generateOnDestroyMethodImpl();
			List<ImmutableMethodParameter> parameters = Lists.newArrayList();
			methods.add(new ImmutableMethod(clazz.getType(), "onDestroy", parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl));
		}

		return clazz;
	}

	@Override
	public Method instrumentMethod(Method method) {
		String name = method.getName();
		MethodImplementation implementation = method.getImplementation();
		if ((implementation != null) && METHOD_ONCREATE.equals(name)) {
			MethodImplementation newImplementation = null;
			// if(!method.getName().equals("<init>"))
			newImplementation = instrument(method);

			if (newImplementation != null) {
				return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(),
						newImplementation);
			} else {
				return method;
			}
		} else if ((implementation != null) && METHOD_ONDESTROY.equals(name)) {
			MethodImplementation newImplementation = null;
			newImplementation = instrumentDestroy(method);

			if (newImplementation != null) {
				return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(),
						newImplementation);
			} else {
				return method;
			}
		} else {
			return method;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetMethod(Method method) {
		return true;
	}

	private MethodImplementation instrumentDestroy(Method method) {
		MutableMethodImplementation impl = new MutableMethodImplementation(method.getImplementation());
		impl.addInstruction(0, createAgentDestroyInvocation());
		return impl;
	}

	private MethodImplementation instrument(Method method) {
		MutableMethodImplementation impl = new MutableMethodImplementation(method.getImplementation());

		int paramRegisters = MethodUtil.getParameterRegisterCount(method);
		int thisRegister = DexInstrumentationUtil.getThisRegister(impl.getRegisterCount(), paramRegisters);

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));

		return impl;
	}

	private MethodImplementation generateOnCreateMethodImpl() {
		MutableMethodImplementation impl = new MutableMethodImplementation(2);

		// 2 because there is 1 register parameter 1 this reference -> parameter count is this
		// inclusive
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

	private MethodImplementation generateOnDestroyMethodImpl() {
		MutableMethodImplementation impl = new MutableMethodImplementation(1);

		impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));

		MethodReference methRef = DexInstrumentationUtil.getMethodReference(Activity.class, "onDestroy", "V");

		BuilderInstruction35c superInvocation = new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1, 0, 0, 0, 0, 0, methRef);

		impl.addInstruction(0, createAgentDestroyInvocation());
		impl.addInstruction(0, superInvocation);

		return impl;
	}

	private BuilderInstruction createAgentInitInvocation(int thisRegister) {
		MethodReference methodReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "initAgent", "V", Activity.class);
		BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, thisRegister, 0, 0, 0, 0, methodReference);

		return invokeInstruction;
	}

	private BuilderInstruction createAgentDestroyInvocation() {
		MethodReference methodReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "destroyAgent", "V");
		BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0, methodReference);

		return invokeInstruction;
	}

}
