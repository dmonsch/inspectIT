package rocks.inspectit.android.instrument.dex.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodLocation;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.util.MethodUtil;

import rocks.inspectit.agent.android.core.AndroidAgent;
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

	private static final String CONSTRUCTOR_METHOD_NAME = "<init>";

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
		return false; // traceConfiguration.isTracedMethod(method.getDefiningClass(),
						// method.getName(), method.getParameters());
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

		Pair<Integer, MutableMethodImplementation> extendedInstr = DexInstrumentationUtil.extendMethodRegisters(method, addedRegisters);

		MutableMethodImplementation nImpl = extendedInstr.getRight();

		int instrOffset = extendedInstr.getLeft();

		// create additional code
		String signature = DexInstrumentationUtil.getMethodSignature(method);

		int thisRegister = method.getImplementation().getRegisterCount() - MethodUtil.getParameterRegisterCount(method);
		final int dest = nImpl.getRegisterCount() - addedRegisters; // free register offset

		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createIntegerConstant(dest + 1, traceSensorId));
		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createLongConstant(dest + 2, signature.hashCode()));
		nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createStringConstant(dest + 4, signature));

		if (!MethodUtil.isStatic(method) && !method.getName().equals(CONSTRUCTOR_METHOD_NAME)) {
			// create a move
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.moveRegister(dest + 5, thisRegister, Opcode.MOVE_OBJECT));
		} else {
			// push null
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.createIntegerConstant(dest + 5, 0));
		}

		// constructor is called with null as object instance
		// method id -> 0, method signature -> 2, object -> thisRegister, parameter array -> 3 ff.
		//List<BuilderInstruction> delegationEventCreation = DexInstrumentationUtil.generateDelegationEventCreation(MethodEnterEvent.class, dest + 0,
		//		new int[] { dest + 1, dest + 2, dest + 3, dest + 4, dest + 5 });
		//BuilderInstruction delegationEventProcessing = DexInstrumentationUtil.generateDelegationEventProcessing(dest);

		/*
		 * if (method.getName().equals(CONSTRUCTOR_METHOD_NAME)) { // special handling for
		 * (Instruction instr : method.getImplementation().getInstructions()) { // search for super
		 * init invocation if ((instr.getOpcode() == Opcode.INVOKE_DIRECT) || (instr.getOpcode() ==
		 * Opcode.INVOKE_DIRECT_RANGE)) { // constructor invocation
		 * System.out.println(instr.getClass()); } } }
		 */

		//instrOffset += DexInstrumentationUtil.addInstructions(nImpl, delegationEventCreation, instrOffset);
		// nImpl.addInstruction(instrOffset++, delegationEventProcessing);

		// add the move back instruction
		if (!MethodUtil.isStatic(method) && !method.getName().equals(CONSTRUCTOR_METHOD_NAME)) {
			nImpl.addInstruction(instrOffset++, DexInstrumentationUtil.moveRegister(thisRegister, dest + 5, Opcode.MOVE_OBJECT));
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
		int movingLabelsSize = 0;
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

			int instrPos = retPos + offset;

			// create exit code
			//List<BuilderInstruction> exitDelegationEventCreation = DexInstrumentationUtil.generateDelegationEventCreation(MethodExitEvent.class, dest,
			//		new int[] { dest + 1, dest + 2, dest + 3, dest + 4, dest + 5 });
			//BuilderInstruction exitDelegationEventProcessing = DexInstrumentationUtil.generateDelegationEventProcessing(dest);

			BuilderInstruction firstExitInstr;
			if (!MethodUtil.isStatic(method)) {
				// create a move
				BuilderInstruction moveThisBack = DexInstrumentationUtil.moveRegister(dest + 5, thisRegister, Opcode.MOVE_OBJECT);
				nImpl.addInstruction(instrPos++, moveThisBack);
				offset++;

				firstExitInstr = moveThisBack;
			} else {
				//firstExitInstr = exitDelegationEventCreation.get(0);
			}

			//instrPos += DexInstrumentationUtil.addInstructions(nImpl, exitDelegationEventCreation, instrPos);
			//nImpl.addInstruction(instrPos++, exitDelegationEventProcessing);

			offset += 3; // three new instructions we're added

			if (!MethodUtil.isStatic(method)) {
				BuilderInstruction moveThisBack = DexInstrumentationUtil.moveRegister(thisRegister, dest + 5, Opcode.MOVE_OBJECT);
				nImpl.addInstruction(instrPos++, moveThisBack);
				offset++;
			}

			// move labels
			for (Label movingLabel : movingLabels) {
				retLocation.getLabels().remove(movingLabel);
				// firstExitInstr.getLocation().getLabels().add(movingLabel);
			}
			movingLabelsSize += movingLabels.size();
		}

		if (method.getName().equals("a") && method.getDefiningClass().contains("Lnet/hockeyapp/android/c/d;") && (method.getParameters().size() == 6)) {
			System.out.println("------" + method.getDefiningClass() + "#" + method.getName() + " (" + method.getParameters().size() + ") " + "------");
			System.out.println("Found ret positions: " + positions_ret.size());
			System.out.println("Moving labels: " + movingLabelsSize);
			System.out.println("isSynchronized: " + (AccessFlags.SYNCHRONIZED.isSet(method.getAccessFlags())));
			System.out.println("isStatic: " + AccessFlags.STATIC.isSet(method.getAccessFlags()));
			System.out.println("Destination: " + dest);
			System.out.println("Added Instructions: " + offset);
			for (Instruction instr : nImpl.getInstructions()) {
				if (instr.getOpcode() == Opcode.MONITOR_EXIT) {
					BuilderInstruction11x x11 = (BuilderInstruction11x) instr;
					System.out.println(instr.getOpcode() + " " + x11.getRegisterA() + " (Labels: " + x11.getLocation().getLabels().size() + ")");
					for (Label label : x11.getLocation().getLabels()) {
						System.out.println(label.getLocation().getIndex());
					}
				} else if (instr instanceof BuilderInstruction35c) {
					BuilderInstruction35c c35 = (BuilderInstruction35c) instr;
					System.out.println(instr.getOpcode() + " " + c35.getRegisterC() + "," + c35.getRegisterD() + "," + c35.getRegisterE() + "," + c35.getRegisterF() + "," + c35.getRegisterG());
				} else if (instr instanceof BuilderInstruction11x) {
					System.out.println(instr.getOpcode() + " " + ((BuilderInstruction11x) instr).getRegisterA());
				} else if (instr instanceof BuilderInstruction10t) {
					System.out.println(instr.getOpcode() + " " + ((BuilderInstruction10t) instr).getTarget().getLocation().getIndex());
				} else if (instr instanceof BuilderInstruction12x) {
					BuilderInstruction12x x12 = (BuilderInstruction12x) instr;
					System.out.println(instr.getOpcode() + " " + x12.getRegisterA() + "<-" + x12.getRegisterB());
				} else if (instr instanceof BuilderInstruction21c) {
					BuilderInstruction21c c21 = (BuilderInstruction21c) instr;
					System.out.println(instr.getOpcode() + " " + c21.getRegisterA() + " (Labels: " + c21.getLocation().getLabels().size() + ")");
				} else {
					System.out.println(instr.getOpcode() + " (" + instr.getClass() + ")");
				}
			}
			System.out.println("--------------------------------------------------------------------------");
		}

		if (addedRegisters == 6) {
			return new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(), nImpl);
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
					// nImpl.addInstruction(throwPos + offset, new
					// BuilderInstruction31i(Opcode.CONST, dest + 0, signature.hashCode()));
					BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 2, exceptionRegister, dest + 0, 0, 0, 0, onExitErrorReference);
					// nImpl.addInstruction(throwPos + offset + 1, invokeInstruction);

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

}
