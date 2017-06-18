package rocks.inspectit.android.instrument;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.ImmutableMethod;

/**
 * @author David Monschein
 *
 */
public class DexNetworkInstrumenter {

	public Pair<Boolean, ? extends Method> instrumentMethod(Method meth) {
		if (meth.getImplementation() != null) {
			MutableMethodImplementation nImpl = new MutableMethodImplementation(meth.getImplementation());
			Pair<Boolean, MutableMethodImplementation> instrResult = instrument(nImpl);
			return Pair.of(instrResult.getLeft(),
					new ImmutableMethod(meth.getDefiningClass(), meth.getName(), meth.getParameters(), meth.getReturnType(), meth.getAccessFlags(), meth.getAnnotations(), instrResult.getRight()));
		} else {
			return Pair.of(false, meth);
		}
	}

	private Pair<Boolean, MutableMethodImplementation> instrument(MutableMethodImplementation impl) {

		boolean modified = false;

		for (Instruction instr : impl.getInstructions()) {
			if (instr instanceof BuilderInstruction35c) {
				BuilderInstruction35c invoc = (BuilderInstruction35c) instr;
				if (invoc.getReference() instanceof MethodReference) {
					MethodReference ref = (MethodReference) invoc.getReference();
				}
			} else if (instr instanceof BuilderInstruction3rc) {
				BuilderInstruction3rc invoc = (BuilderInstruction3rc) instr;
				if (invoc.getReference() instanceof MethodReference) {
					MethodReference ref = (MethodReference) invoc.getReference();
				}
			}
		}

		return Pair.of(modified, impl);
	}

}
