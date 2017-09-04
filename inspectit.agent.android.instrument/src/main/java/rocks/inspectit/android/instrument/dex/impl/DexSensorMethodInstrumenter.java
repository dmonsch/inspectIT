package rocks.inspectit.android.instrument.dex.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction35c;
import org.jf.dexlib2.iface.instruction.formats.Instruction3rc;
import org.jf.dexlib2.iface.reference.MethodReference;

import rocks.inspectit.android.instrument.config.xml.TraceCollectionConfiguration;
import rocks.inspectit.android.instrument.dex.IDexMethodImplementationInstrumenter;

/**
 * @author David Monschein
 *
 */
public class DexSensorMethodInstrumenter implements IDexMethodImplementationInstrumenter {

	private static final Opcode[] INVOKE_OPCODES = new Opcode[] { Opcode.INVOKE_DIRECT, Opcode.INVOKE_DIRECT_EMPTY, Opcode.INVOKE_DIRECT_RANGE, Opcode.INVOKE_INTERFACE, Opcode.INVOKE_INTERFACE_RANGE,
			Opcode.INVOKE_OBJECT_INIT_RANGE, Opcode.INVOKE_POLYMORPHIC, Opcode.INVOKE_POLYMORPHIC_RANGE, Opcode.INVOKE_STATIC, Opcode.INVOKE_STATIC_RANGE, Opcode.INVOKE_SUPER,
			Opcode.INVOKE_SUPER_QUICK, Opcode.INVOKE_SUPER_QUICK_RANGE, Opcode.INVOKE_SUPER_RANGE, Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_QUICK, Opcode.INVOKE_VIRTUAL_QUICK_RANGE,
			Opcode.INVOKE_VIRTUAL_RANGE };

	private TraceCollectionConfiguration config;

	private Map<Class<?>[], MethodReference> parameterMethodMapping;

	public DexSensorMethodInstrumenter(TraceCollectionConfiguration traceConfig) {
		this.config = traceConfig;

		this.parameterMethodMapping = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Pair<Boolean, MutableMethodImplementation> instrument(MethodImplementation impl) {

		int offset = 0;
		int k = 0;
		List<Pair<Integer, MethodReference>> tracedInstructions = new ArrayList<>();

		for (Instruction instr : impl.getInstructions()) {
			Opcode o = instr.getOpcode();
			for (Opcode l : INVOKE_OPCODES) {
				if (o == l) {
					if (instr instanceof Instruction35c) {
						Instruction35c invoc = (Instruction35c) instr;
						MethodReference meth = (MethodReference) invoc.getReference();
						if (config.isTracedMethod(meth.getDefiningClass(), meth.getName(), meth.getParameterTypes())) {
							tracedInstructions.add(Pair.of(k, meth));
						}
					} else if (instr instanceof Instruction3rc) {
						Instruction3rc invoc = (Instruction3rc) instr;
						MethodReference meth = (MethodReference) invoc.getReference();
						if (config.isTracedMethod(meth.getDefiningClass(), meth.getName(), meth.getParameterTypes())) {
							tracedInstructions.add(Pair.of(k, meth));
						}
					} // else shouldn't happen => would be invalid bytecode
					break;
				}
			}

			// increment
			k++;
		}

		for (Pair<Integer, MethodReference> tracedMethodCall : tracedInstructions) {
			// TODO
		}

		return Pair.of(false, null);
	}

	private void resolveBelongingMethod() {
	}

	private List<Method> getMethodsByName(Class<?> clazz, String name) {
		List<Method> output = new ArrayList<>();
		for (Method meth : clazz.getMethods()) {
			if (meth.getName().equals(name)) {
				output.add(meth);
			}
		}
		return output;
	}

}
