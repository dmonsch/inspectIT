package rocks.inspectit.android.instrument.dex.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.util.MethodUtil;

import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.agent.android.delegation.event.MethodEnterEvent;
import rocks.inspectit.agent.android.delegation.event.MethodExitEvent;
import rocks.inspectit.agent.android.sensor.SensorAnnotation;
import rocks.inspectit.agent.android.sensor.TraceSensor;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.dex.IDexMethodInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexSensorInstrumenter implements IDexMethodInstrumenter {

	private TraceCollectionConfiguration traceConfiguration;
	private int traceSensorId;

	public DexSensorInstrumenter(TraceCollectionConfiguration traceConfig) {
		this.traceConfiguration = traceConfig;
		this.traceSensorId = TraceSensor.class.getAnnotation(SensorAnnotation.class).id();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTargetMethod(Method method) {
		return isTracedMethod(method.getDefiningClass(), method.getName(), method.getParameters());
	}

	@Override
	public Method instrumentMethod(Method meth) {
		return instrument(meth);
	}

	private Method instrument(Method method) {
		if (method.getImplementation() == null) {
			return method;
		}

		int addedRegisters = 6;
		Pair<Integer, MutableMethodImplementation> extendedInstr = DexInstrumentationUtil.extendMethodRegisters(method, addedRegisters);

		MutableMethodImplementation nImpl = extendedInstr.getRight();
		int instrOffset = extendedInstr.getLeft();

		// create additional code
		String signature = DexInstrumentationUtil.getMethodSignature(method);

		int thisRegister = method.getImplementation().getRegisterCount() - MethodUtil.getParameterRegisterCount(method);
		int dest = nImpl.getRegisterCount() - addedRegisters; // free register offset

		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createIntegerConstant(dest + 0, traceSensorId));
		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createLongConstant(dest + 1, signature.hashCode()));
		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createStringConstant(dest + 3, signature));

		if (!MethodUtil.isStatic(method)) {
			// create a move
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.moveRegister(dest + 4, thisRegister, Opcode.MOVE_OBJECT));
		} else {
			// push null
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createIntegerConstant(dest + 4, 0));
		}

		// method id -> 0, method signature -> 2, object -> thisRegister, parameter array -> 3 ff.
		List<BuilderInstruction> delegationEventCreation = DexInstrumentationUtil.generateDelegationEventCreation(MethodEnterEvent.class, dest + 5, new int[] { dest, dest + 1, dest + 3, dest + 4 });
		BuilderInstruction delegationEventProcessing = DexInstrumentationUtil.generateDelegationEventProcessing(dest + 5);

		DexInstrumentationUtil.addInstructions(nImpl, delegationEventCreation, instrOffset += delegationEventCreation.size());
		nImpl.addInstruction(instrOffset++, delegationEventProcessing);

		// add the move back instruction
		if (!MethodUtil.isStatic(method)) {
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.moveRegister(thisRegister, dest + 4, Opcode.MOVE_OBJECT));
		}

		// search for exit points
		List<BuilderInstruction> instrList = nImpl.getInstructions();

		List<Integer> positions_ret = new ArrayList<>();
		List<Integer> positions_throw = new ArrayList<>();
		for (int i = 0; i < instrList.size(); i++) {
			Instruction instr = instrList.get(i);

			if ((instr.getOpcode() == Opcode.RETURN_VOID) || (instr.getOpcode() == Opcode.RETURN) || (instr.getOpcode() == Opcode.RETURN_OBJECT) || (instr.getOpcode() == Opcode.RETURN_WIDE)) {
				positions_ret.add(i);
			}
		}

		// TODO add exit instructions -> throw
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
				continue;
			}

			// create exit code
			List<BuilderInstruction> exitDelegationEventCreation = DexInstrumentationUtil.generateDelegationEventCreation(MethodExitEvent.class, dest + 4,
					new int[] { dest, dest + 1, dest + 3, dest + 4 });
			BuilderInstruction exitDelegationEventProcessing = DexInstrumentationUtil.generateDelegationEventProcessing(dest + 4);

			BuilderInstruction firstExitInstr;
			if (!MethodUtil.isStatic(method)) {
				// create a move
				BuilderInstruction moveThisBack = DexInstrumentationUtil.moveRegister(dest + 4, thisRegister, Opcode.MOVE_OBJECT);
				nImpl.addInstruction(instrOffset++, moveThisBack);

				firstExitInstr = moveThisBack;
			} else {
				firstExitInstr = exitDelegationEventCreation.get(0);
			}

			DexInstrumentationUtil.addInstructions(nImpl, exitDelegationEventCreation, instrOffset += exitDelegationEventCreation.size());
			nImpl.addInstruction(instrOffset++, exitDelegationEventProcessing);

			// move labels
			for (Label movingLabel : movingLabels) {
				retLocation.getLabels().remove(movingLabel);
				firstExitInstr.getLocation().getLabels().add(movingLabel);
			}

			// TODO EXIT THINGS

			offset += 2; // we added two new instructions therefore we need to shift the instr
			// position
		}

		// after adding exit body statements because otherwise the offset is unknown
		for (int i = 0; i < instrList.size(); i++) {
			Instruction instr = instrList.get(i);
			if (instr.getOpcode() == Opcode.THROW) {
				positions_throw.add(i);
			}
		}

		offset = 0;
		for (int throwPos : positions_throw) {
			Instruction throwInstr = nImpl.getInstructions().get(throwPos + offset);
			int exceptionRegister = -1;
			if (throwInstr instanceof BuilderInstruction11x) {
				exceptionRegister = ((BuilderInstruction11x) throwInstr).getRegisterA();
			}

			if (exceptionRegister >= 0) {
				MethodReference onExitErrorReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "exitErrorBody", "V", Throwable.class, int.class);

				if (DexInstrumentationUtil.numBits(dest + 0) == 4) { // => exception register also 4
					// bits
					nImpl.addInstruction(throwPos + offset, new BuilderInstruction31i(Opcode.CONST, dest + 0, signature.hashCode()));
					BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 2, exceptionRegister, dest + 0, 0, 0, 0, onExitErrorReference);
					nImpl.addInstruction(throwPos + offset + 1, invokeInstruction);

					offset += 2;
				} else {
					// TODO instruction for copying throwable register -> 2 moves
					// nImpl.addInstruction(throwPos + offset + 1, new
					// BuilderInstruction31i(Opcode.CONST, dest + 1, signature.hashCode()));
					// BuilderInstruction3rc invokeInstruction = new
					// BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, dest + 0, 2,
					// onEnterReference);
					// nImpl.addInstruction(throwPos + offset + 2, invokeInstruction);
				}
			}
		}

		return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(), nImpl);
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

}
