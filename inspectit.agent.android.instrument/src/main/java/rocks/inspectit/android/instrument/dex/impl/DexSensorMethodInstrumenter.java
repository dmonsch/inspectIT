package rocks.inspectit.android.instrument.dex.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;

import rocks.inspectit.agent.android.delegation.DelegationPoint;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.dex.IDexMethodImplementationInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;
import rocks.inspectit.android.instrument.util.MethodSignatureFormatter;

/**
 * @author David Monschein
 *
 */
public class DexSensorMethodInstrumenter implements IDexMethodImplementationInstrumenter {
	private static final int REGISTER_ADD_COUNT = 7;
	private static final String CONSTRUCTOR_METHOD_NAME = "<init>";

	private TraceCollectionConfiguration config;

	private Map<DelegationPoint, MethodReference> delegationMapping;

	private MethodSignatureFormatter methodSignatureFormatter;

	private SensorCache sensorCache;

	public DexSensorMethodInstrumenter(TraceCollectionConfiguration traceConfig, Map<DelegationPoint, MethodReference> delegationMapping) {
		this.config = traceConfig;
		this.delegationMapping = delegationMapping;
		this.methodSignatureFormatter = new MethodSignatureFormatter();
		this.sensorCache = SensorCache.getCurrentInstance(config);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Boolean, MutableMethodImplementation> instrument(Method reference) {
		int offset = 0;
		int k = 0;

		List<Pair<Integer, MethodReference>> tracedInstructions = new ArrayList<>();

		// create mutable implementation
		Pair<Integer, MutableMethodImplementation> mutedImplementation = DexInstrumentationUtil.extendMethodRegisters(reference, REGISTER_ADD_COUNT);
		int destRegisterStart = mutedImplementation.getRight().getRegisterCount() - REGISTER_ADD_COUNT;

		for (Instruction instr : mutedImplementation.getRight().getInstructions()) {
			Opcode o = instr.getOpcode();
			if (DexInstrumentationUtil.isInvocationOpcode(o)) {
				if (instr instanceof Instruction35c) {
					Instruction35c invoc = (Instruction35c) instr;
					MethodReference meth = (MethodReference) invoc.getReference();
					if ((config.isTracedMethod(meth.getDefiningClass(), meth.getName(), meth.getParameterTypes()).size() > 0) && !config.isAlreadyInstrumented(meth)) {
						tracedInstructions.add(Pair.of(k, meth));
					}
				} else if (instr instanceof Instruction3rc) {
					Instruction3rc invoc = (Instruction3rc) instr;
					MethodReference meth = (MethodReference) invoc.getReference();
					if ((config.isTracedMethod(meth.getDefiningClass(), meth.getName(), meth.getParameterTypes()).size() > 0) && !config.isAlreadyInstrumented(meth)) {
						tracedInstructions.add(Pair.of(k, meth));
					}
				} // else shouldn't happen => would be invalid bytecode
				break;
			}

			// increment
			k++;
		}

		boolean instrumented = false;
		for (Pair<Integer, MethodReference> tracedMethodCall : tracedInstructions) {
			int[] sensors = sensorCache.resolveAllSensorIds(tracedMethodCall.getRight());
			int added = instrument(mutedImplementation.getRight(), tracedMethodCall.getLeft() + offset, destRegisterStart, sensors, tracedMethodCall.getRight());
			if (added > 0) {
				instrumented = true;
				offset += added;
			}
		}

		if (!instrumented) {
			return Pair.of(false, null);
		} else {
			return Pair.of(true, mutedImplementation.getRight());
		}
	}

	private int instrument(MutableMethodImplementation impl, int position, int dest, int[] sensors, MethodReference toTrace) {

		Instruction instr = impl.getInstructions().get(position);

		boolean isStatic = (instr.getOpcode() == Opcode.INVOKE_STATIC) || (instr.getOpcode() == Opcode.INVOKE_STATIC_RANGE);
		int firstRegister = -1; // holds the object reference
		int instrDest = position;

		char returnTypeFirstChar = toTrace.getReturnType().charAt(0);
		Opcode returnTypeMoveOpcode = ((returnTypeFirstChar == 'J') || (returnTypeFirstChar == 'D')) ? Opcode.MOVE_WIDE
				: (((returnTypeFirstChar == 'L') || (returnTypeFirstChar == '[')) ? Opcode.MOVE_OBJECT : Opcode.MOVE);
		boolean returnTransformed = false;

		if (!isStatic) {
			if (instr instanceof BuilderInstruction35c) {
				BuilderInstruction35c instrTemp = (BuilderInstruction35c) instr;
				firstRegister = instrTemp.getRegisterC();
			} else if (instr instanceof BuilderInstruction3rc) {
				BuilderInstruction3rc instrTemp = (BuilderInstruction3rc) instr;
				firstRegister = instrTemp.getStartRegister();
			}
		}

		// generate invocation stuff
		String signature = methodSignatureFormatter.formatSignature(DexInstrumentationUtil.getMethodSignature(toTrace));

		impl.addInstruction(instrDest++, DexInstrumentationUtil.createLongConstant(dest + 1, signature.hashCode()));
		impl.addInstruction(instrDest++, DexInstrumentationUtil.createStringConstant(dest + 3, signature));

		// move object #1
		if (isStatic || toTrace.getName().equals(CONSTRUCTOR_METHOD_NAME)) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.createIntegerConstant(dest + 4, 0));
		} else {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.moveRegister(dest + 4, firstRegister, Opcode.MOVE_OBJECT));
		}

		// the invocation #1
		for (int traceSensorId : sensors) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.createIntegerConstant(dest, traceSensorId));
			// invoc
			impl.addInstruction(instrDest++,
					DexInstrumentationUtil.generateDelegationInvocation(new int[] { dest, dest + 1, dest + 2, dest + 3, dest + 4 }, delegationMapping.get(DelegationPoint.ON_METHOD_ENTER)));
		}

		// move back #1
		if (!isStatic && !toTrace.getName().equals(CONSTRUCTOR_METHOD_NAME)) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.moveRegister(firstRegister, dest + 4, Opcode.MOVE_OBJECT));
		}

		// move behind the original instruction
		int jumpedInstructions = 1;
		instrDest++;
		// look for move result statement
		Instruction afterInvoc = impl.getInstructions().get(instrDest);
		Opcode afterInvocOpcode = afterInvoc.getOpcode();
		if ((afterInvocOpcode == Opcode.MOVE_RESULT) || (afterInvocOpcode == Opcode.MOVE_RESULT_OBJECT) || (afterInvocOpcode == Opcode.MOVE_RESULT_WIDE)) {
			// inspect the move
			BuilderInstruction11x resultMoveInstr = (BuilderInstruction11x) afterInvoc;
			if (resultMoveInstr.getRegisterA() == firstRegister) {
				// replace the move
				BuilderInstruction11x transformedMove = new BuilderInstruction11x(resultMoveInstr.getOpcode(), dest + 5);
				impl.replaceInstruction(instrDest, transformedMove);
				returnTransformed = true;
			}

			// jump over move
			instrDest++;
			jumpedInstructions++;
		}

		// move object #2
		if (!isStatic) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.moveRegister(dest + 4, firstRegister, Opcode.MOVE_OBJECT));
		} else {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.createIntegerConstant(dest + 4, 0));
		}

		// reload parameters
		impl.addInstruction(instrDest++, DexInstrumentationUtil.createLongConstant(dest + 1, signature.hashCode()));
		impl.addInstruction(instrDest++, DexInstrumentationUtil.createStringConstant(dest + 3, signature));

		// the invocation #2
		for (int traceSensorId : sensors) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.createIntegerConstant(dest, traceSensorId));
			// invoc
			impl.addInstruction(instrDest++,
					DexInstrumentationUtil.generateDelegationInvocation(new int[] { dest, dest + 1, dest + 2, dest + 3, dest + 4 }, delegationMapping.get(DelegationPoint.ON_METHOD_EXIT)));
		}

		// move back #2
		if (!isStatic && !returnTransformed) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.moveRegister(firstRegister, dest + 4, Opcode.MOVE_OBJECT));
		} else if (returnTransformed) {
			impl.addInstruction(instrDest++, DexInstrumentationUtil.moveRegister(firstRegister, dest + 5, returnTypeMoveOpcode));
		}

		return instrDest - position - jumpedInstructions;
	}

}
