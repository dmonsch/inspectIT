package rocks.inspectit.android.instrument.dex.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.util.MethodUtil;

import com.google.common.collect.Lists;

import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.agent.android.delegation.DelegationPoint;
import rocks.inspectit.android.instrument.dex.IDexClassInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexActivityInstrumenter implements IDexClassInstrumenter {

	private static final String METHOD_ONCREATE = "onCreate";
	private static final String METHOD_ONDESTROY = "onDestroy";
	private static final String METHOD_ONSTOP = "onStop";
	private static final String METHOD_ONSTART = "onStart";

	private static final String ACTIVITY_TYPE = "Landroid/app/Activity;";
	private static final String BUNDLE_TYPE = "Landroid/os/Bundle;";

	private Map<DelegationPoint, MethodReference> delegationPointMapping;

	public DexActivityInstrumenter(Map<DelegationPoint, MethodReference> delegationPointMapping) {
		this.delegationPointMapping = delegationPointMapping;
	}

	@Override
	public boolean isTargetClass(ClassDef clazz) {
		String superClass = clazz.getSuperclass();
		return (superClass != null) && (superClass.equals(ACTIVITY_TYPE));
	}

	@Override
	public ClassDef instrumentClass(ClassDef clazz) {
		if (!isTargetClass(clazz)) {
			return clazz;
		}

		List<Method> methods = Lists.newArrayList();

		boolean foundOnCreate = false;
		boolean foundOnDestroy = false;
		boolean foundOnStop = false;
		boolean foundOnStart = false;

		for (Method meth : clazz.getMethods()) {
			if (meth.getImplementation() != null) {
				if (METHOD_ONCREATE.equals(meth.getName())) {
					foundOnCreate = true;
				} else if (METHOD_ONDESTROY.equals(meth.getName())) {
					foundOnDestroy = true;
				} else if (METHOD_ONSTOP.equals(meth.getName())) {
					foundOnStop = true;
				} else if (METHOD_ONSTART.equals(meth.getName())) {
					foundOnStart = true;
				}
				methods.add(instrumentMethod(clazz, meth));
			}
		}

		if (!foundOnCreate) {
			MethodImplementation nImpl = generateOnCreateMethodImpl();
			List<ImmutableMethodParameter> parameters = Lists.newArrayList(new ImmutableMethodParameter(BUNDLE_TYPE, new HashSet<Annotation>(), "bundle"));
			methods.add(new ImmutableMethod(clazz.getType(), METHOD_ONCREATE, parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl));
		}

		if (!foundOnDestroy) {
			MethodImplementation nImpl = generateOnDestroyMethodImpl();
			List<ImmutableMethodParameter> parameters = Lists.newArrayList();
			methods.add(new ImmutableMethod(clazz.getType(), METHOD_ONDESTROY, parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl));
		}

		if (!foundOnStart) {
			MethodImplementation nImpl = generateOnStartStopMethodImpl(METHOD_ONSTART, createDelegation(DelegationPoint.ON_START));
			List<ImmutableMethodParameter> parameters = Lists.newArrayList();
			methods.add(new ImmutableMethod(clazz.getType(), METHOD_ONSTART, parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl));
		}

		if (!foundOnStop) {
			MethodImplementation nImpl = generateOnStartStopMethodImpl(METHOD_ONSTOP, createDelegation(DelegationPoint.ON_STOP));
			List<ImmutableMethodParameter> parameters = Lists.newArrayList();
			methods.add(new ImmutableMethod(clazz.getType(), METHOD_ONSTOP, parameters, "V", AccessFlags.PUBLIC.getValue(), new HashSet<Annotation>(), nImpl));
		}

		return new ImmutableClassDef(clazz.getType(), clazz.getAccessFlags(), clazz.getSuperclass(), clazz.getInterfaces(), clazz.getSourceFile(), clazz.getAnnotations(), clazz.getFields(), methods);
	}

	@Override
	public Method instrumentMethod(ClassDef parent, Method method) {
		String name = method.getName();
		MethodImplementation implementation = method.getImplementation();
		if ((implementation != null)) {
			if (METHOD_ONCREATE.equals(name)) {
				MethodImplementation newImplementation = instrumentCreate(method);
				if (newImplementation != null) {
					return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(),
							newImplementation);
				}
			} else if (METHOD_ONDESTROY.equals(name)) {
				MethodImplementation newImplementation = instrumentDestroy(method);
				if (newImplementation != null) {
					return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(),
							newImplementation);
				}
			} else if (METHOD_ONSTART.equals(name)) {
				MethodImplementation newImplementation = instrumentStartStop(method, DelegationPoint.ON_START);
				if (newImplementation != null) {
					return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(),
							newImplementation);
				}
			} else if (METHOD_ONSTOP.equals(name)) {
				MethodImplementation newImplementation = instrumentStartStop(method, DelegationPoint.ON_STOP);
				if (newImplementation != null) {
					return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(),
							newImplementation);
				}
			}
		}
		return method;
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

	private MethodImplementation instrumentCreate(Method method) {
		MutableMethodImplementation impl = new MutableMethodImplementation(method.getImplementation());

		int paramRegisters = MethodUtil.getParameterRegisterCount(method);
		int thisRegister = DexInstrumentationUtil.getThisRegister(impl.getRegisterCount(), paramRegisters);

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));

		return impl;
	}

	private MethodImplementation instrumentStartStop(Method method, DelegationPoint point) {
		Pair<Integer, MutableMethodImplementation> impl = DexInstrumentationUtil.extendMethodRegisters(method, 1);

		impl.getRight().addInstruction(impl.getLeft(), createDelegation(point));

		return impl.getRight();
	}

	private BuilderInstruction createDelegation(DelegationPoint point) {
		return DexInstrumentationUtil.generateDelegationInvocation(new int[] {}, delegationPointMapping.get(point));
	}

	private MethodImplementation generateOnStartStopMethodImpl(String superName, BuilderInstruction delegation) {
		MutableMethodImplementation impl = new MutableMethodImplementation(2);

		impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));

		MethodReference methRef = DexInstrumentationUtil.getMethodReference(ACTIVITY_TYPE, superName, "V");
		BuilderInstruction35c superInvocation = new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1, 1, 0, 0, 0, 0, methRef);

		impl.addInstruction(0, delegation);
		impl.addInstruction(0, superInvocation);

		return impl;
	}

	private MethodImplementation generateOnCreateMethodImpl() {
		MutableMethodImplementation impl = new MutableMethodImplementation(2);

		// 2 because there is 1 register parameter 1 this reference -> parameter count is this
		// inclusive
		int thisRegister = DexInstrumentationUtil.getThisRegister(impl.getRegisterCount(), 2);
		int parameterRegister = 1; // in highest register

		impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));

		MethodReference methRef = DexInstrumentationUtil.getMethodReference(ACTIVITY_TYPE, "onCreate", "V", BUNDLE_TYPE);

		// first register is this register -> because 1 is parameter register
		BuilderInstruction35c superInvocation = new BuilderInstruction35c(Opcode.INVOKE_SUPER, 2, thisRegister, parameterRegister, 0, 0, 0, methRef);

		impl.addInstruction(0, createAgentInitInvocation(thisRegister));
		impl.addInstruction(0, superInvocation);

		return impl;
	}

	private MethodImplementation generateOnDestroyMethodImpl() {
		MutableMethodImplementation impl = new MutableMethodImplementation(1);

		impl.addInstruction(0, new BuilderInstruction10x(Opcode.RETURN_VOID));

		MethodReference methRef = DexInstrumentationUtil.getMethodReference(ACTIVITY_TYPE, "onDestroy", "V");

		BuilderInstruction35c superInvocation = new BuilderInstruction35c(Opcode.INVOKE_SUPER, 1, 0, 0, 0, 0, 0, methRef);

		impl.addInstruction(0, createAgentDestroyInvocation());
		impl.addInstruction(0, superInvocation);

		return impl;
	}

	private BuilderInstruction createAgentInitInvocation(int thisRegister) {
		MethodReference methodReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "initAgent", "V", ACTIVITY_TYPE);

		BuilderInstruction invokeInstruction;
		if (DexInstrumentationUtil.numBits(thisRegister) == 4) {
			invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, thisRegister, 0, 0, 0, 0, methodReference);
		} else {
			invokeInstruction = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, thisRegister, 1, methodReference);
		}
		return invokeInstruction;
	}

	private BuilderInstruction createAgentDestroyInvocation() {
		MethodReference methodReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "destroyAgent", "V");
		BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 0, 0, 0, 0, 0, 0, methodReference);

		return invokeInstruction;
	}

}
