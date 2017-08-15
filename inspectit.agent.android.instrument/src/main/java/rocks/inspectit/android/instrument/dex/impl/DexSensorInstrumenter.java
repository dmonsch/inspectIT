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
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.dex.IDexMethodInstrumenter;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 *
 */
public class DexSensorInstrumenter implements IDexMethodInstrumenter {

	private TraceCollectionConfiguration traceConfiguration;

	public DexSensorInstrumenter(TraceCollectionConfiguration traceConfig) {
		this.traceConfiguration = traceConfig;
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
		Pair<Integer, MutableMethodImplementation> extendedInstr = DexInstrumentationUtil.extendMethodRegisters(method, 2);

		MutableMethodImplementation nImpl = extendedInstr.getRight();
		int instrOffset = extendedInstr.getLeft();

		// create additional code
		String signature = DexInstrumentationUtil.getMethodSignature(method);

		int dest = nImpl.getRegisterCount() - 2; // free register offset

		// loads method signature
		nImpl.addInstruction(instrOffset, new BuilderInstruction31i(Opcode.CONST, dest + 0, signature.hashCode()));
		nImpl.addInstruction(instrOffset + 1, new BuilderInstruction21c(Opcode.CONST_STRING, dest + 1, new ImmutableStringReference(signature)));

		// invoke agent
		MethodReference onEnterReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "enterBody", "V", int.class, String.class);
		if (DexInstrumentationUtil.numBits(dest + 1) == 4) {
			BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 2, dest + 0, dest + 1, 0, 0, 0, onEnterReference);
			nImpl.addInstruction(instrOffset + 2, invokeInstruction);
		} else {
			BuilderInstruction3rc invokeInstruction = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, dest + 0, 2, onEnterReference);
			nImpl.addInstruction(instrOffset + 2, invokeInstruction);
		}

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

			// loads method signature
			BuilderInstruction31i loadIntInstruction = new BuilderInstruction31i(Opcode.CONST, dest + 0, signature.hashCode());

			nImpl.addInstruction(retPos + offset, loadIntInstruction);

			// move labels
			for (Label movingLabel : movingLabels) {
				retLocation.getLabels().remove(movingLabel);
				loadIntInstruction.getLocation().getLabels().add(movingLabel);
			}

			MethodReference onExitReference = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "exitBody", "V", int.class);
			if (DexInstrumentationUtil.numBits(dest + 1) == 4) {
				BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, dest + 0, 0, 0, 0, 0, onExitReference);
				nImpl.addInstruction(retPos + offset + 1, invokeInstruction);
			} else {
				BuilderInstruction3rc invokeInstruction = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, dest + 0, 1, onExitReference);
				nImpl.addInstruction(retPos + offset + 1, invokeInstruction);
			}

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
