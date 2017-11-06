package rocks.inspectit.android.instrument.dex.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.util.MethodUtil;

import rocks.inspectit.agent.android.delegation.DelegationPoint;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.dex.IDexMethodInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;
import rocks.inspectit.android.instrument.util.MethodSignatureFormatter;

/**
 * @author David Monschein
 *
 */
public class DexSensorInstrumenter implements IDexMethodInstrumenter {

	private static final String CONSTRUCTOR_METHOD_NAME = "<init>";

	private TraceCollectionConfiguration traceConfiguration;
	private MethodSignatureFormatter methodSignatureFormatter;
	private SensorCache sensorCache;

	private Map<DelegationPoint, MethodReference> delegationMapping;

	public DexSensorInstrumenter(Map<DelegationPoint, MethodReference> delegationMap, TraceCollectionConfiguration traceConfig) {
		this.traceConfiguration = traceConfig;
		this.methodSignatureFormatter = new MethodSignatureFormatter();
		this.sensorCache = SensorCache.getCurrentInstance(traceConfig);
		this.delegationMapping = delegationMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetMethod(Method method) {
		return traceConfiguration.isTracedMethod(method.getDefiningClass(), method.getName(), method.getParameters()).size() > 0;
	}

	@Override
	public Method instrumentMethod(ClassDef parent, Method meth) {
		return instrument(parent, meth);
	}

	private Method instrument(ClassDef parent, Method method) {
		if (method.getImplementation() == null) {
			return method;
		}

		int addedRegisters = 6;
		int[] sensorIds = sensorCache.resolveAllSensorIds(method);

		Pair<Integer, MutableMethodImplementation> extendedInstr = DexInstrumentationUtil.extendMethodRegisters(method, addedRegisters);

		MutableMethodImplementation nImpl = extendedInstr.getRight();

		int instrOffset = extendedInstr.getLeft();

		// generate invocation stuff
		String signature = methodSignatureFormatter.formatSignature(DexInstrumentationUtil.getMethodSignature(method));

		int thisRegister = method.getImplementation().getRegisterCount() - MethodUtil.getParameterRegisterCount(method);
		final int dest = nImpl.getRegisterCount() - addedRegisters; // free register offset

		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createLongConstant(dest + 1, signature.hashCode()));
		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createStringConstant(dest + 3, signature));

		if (!MethodUtil.isStatic(method) && !method.getName().equals(CONSTRUCTOR_METHOD_NAME)) {
			// create a move
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.moveRegister(dest + 4, thisRegister, Opcode.MOVE_OBJECT));
		} else {
			// push null
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createIntegerConstant(dest + 4, 0));
		}

		// the invocation #1
		for (int traceSensorId : sensorIds) {
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createIntegerConstant(dest, traceSensorId));
			// invoc
			nImpl.addInstruction(instrOffset++,
					DexInstrumentationUtil.generateDelegationInvocation(new int[] { dest, dest + 1, dest + 2, dest + 3, dest + 4 }, delegationMapping.get(DelegationPoint.ON_METHOD_ENTER)));
		}

		// move back #1
		if (!MethodUtil.isStatic(method) && !method.getName().equals(CONSTRUCTOR_METHOD_NAME)) {
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.moveRegister(thisRegister, dest + 4, Opcode.MOVE_OBJECT));
		}

		// search for exit points
		List<BuilderInstruction> instrList = nImpl.getInstructions();

		List<Integer> positions_ret = new ArrayList<>();
		for (int i = 0; i < instrList.size(); i++) {
			Instruction instr = instrList.get(i);

			if ((instr.getOpcode() == Opcode.THROW) || (instr.getOpcode() == Opcode.RETURN_VOID) || (instr.getOpcode() == Opcode.RETURN) || (instr.getOpcode() == Opcode.RETURN_OBJECT)
					|| (instr.getOpcode() == Opcode.RETURN_WIDE)) {
				positions_ret.add(i);
			}
		}

		int offset = 0;
		for (int retPos : positions_ret) {
			// get moving labels
			Instruction retInstr = nImpl.getInstructions().get(retPos + offset);
			Set<Label> movingLabels;
			MethodLocation retLocation;

			if (retInstr instanceof BuilderInstruction10x) {
				BuilderInstruction10x ri = (BuilderInstruction10x) retInstr;
				movingLabels = new HashSet<>(ri.getLocation().getLabels());
				retLocation = ri.getLocation();
			} else if (retInstr instanceof BuilderInstruction11x) {
				BuilderInstruction11x ri = (BuilderInstruction11x) retInstr;
				movingLabels = new HashSet<>(ri.getLocation().getLabels());
				retLocation = ri.getLocation();
			} else {
				System.out.println(retInstr.getClass().getName());
				continue;
			}

			int instrPos = retPos + offset;

			// move object #2
			BuilderInstruction firstExitInstruction;
			if (!MethodUtil.isStatic(method)) {
				firstExitInstruction = DexInstrumentationUtil.moveRegister(dest + 4, thisRegister, Opcode.MOVE_OBJECT);
			} else {
				firstExitInstruction = DexInstrumentationUtil.createIntegerConstant(dest + 4, 0);
			}
			nImpl.addInstruction(instrPos++, firstExitInstruction);
			offset++;

			// reload parameters
			nImpl.addInstruction(instrPos++, DexInstrumentationUtil.createLongConstant(dest + 1, signature.hashCode()));
			nImpl.addInstruction(instrPos++, DexInstrumentationUtil.createStringConstant(dest + 3, signature));
			offset += 2;

			// the invocation #2
			for (int traceSensorId : sensorIds) {
				nImpl.addInstruction(instrPos++, DexInstrumentationUtil.createIntegerConstant(dest, traceSensorId));
				// invoc
				nImpl.addInstruction(instrPos++,
						DexInstrumentationUtil.generateDelegationInvocation(new int[] { dest, dest + 1, dest + 2, dest + 3, dest + 4 }, delegationMapping.get(DelegationPoint.ON_METHOD_EXIT)));

				offset += 2;
			}

			// move back #2
			if (!MethodUtil.isStatic(method)) {
				nImpl.addInstruction(instrPos++, DexInstrumentationUtil.moveRegister(thisRegister, dest + 4, Opcode.MOVE_OBJECT));
			} else {
				// push null
				nImpl.addInstruction(instrPos++, DexInstrumentationUtil.createIntegerConstant(dest + 4, 0));
			}
			offset++;

			// move labels
			for (Label movingLabel : movingLabels) {
				retLocation.getLabels().remove(movingLabel);
				firstExitInstruction.getLocation().getLabels().add(movingLabel);
			}
		}
		traceConfiguration.markMethodAsInstrumented(method);

		return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(), nImpl);
	}

}
