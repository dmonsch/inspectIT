package rocks.inspectit.android.instrument;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.ImmutableMethod;

import rocks.inspectit.agent.android.core.AndroidAgent;
import rocks.inspectit.android.instrument.util.DexInstrumentationUtil;

/**
 * @author David Monschein
 */
public class DexNetworkInstrumenter {

	private static final String HTTPURLREQUEST_CLASS = DexInstrumentationUtil.getType(HttpURLConnection.class);

	private static Map<String, String> requestMethodMapping = new HashMap<String, String>() {
		private static final long serialVersionUID = 3902930092114143771L;

		{
			put("connect", "httpConnect");
			put("getOutputStream", "httpOutputStream");
			put("getResponseCode", "httpResponseCode");
		}
	};

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
		int pos = 0;

		for (Instruction instr : impl.getInstructions()) {
			if (instr instanceof BuilderInstruction35c) {
				BuilderInstruction35c invoc = (BuilderInstruction35c) instr;
				if (invoc.getReference() instanceof MethodReference) {
					MethodReference ref = (MethodReference) invoc.getReference();
					if (addCallInvocation(impl, ref, invoc.getRegisterC(), pos)) {
						modified = true;
					}
				}
			} else if (instr instanceof BuilderInstruction3rc) {
				BuilderInstruction3rc invoc = (BuilderInstruction3rc) instr;
				if (invoc.getReference() instanceof MethodReference) {
					MethodReference ref = (MethodReference) invoc.getReference();
					if (addCallInvocation(impl, ref, invoc.getStartRegister(), pos)) {
						modified = true;
					}
				}
			} else if (instr instanceof BuilderInstruction21c) {
				BuilderInstruction21c cast = (BuilderInstruction21c) instr;
				if (cast.getReference() instanceof TypeReference) {
					TypeReference type = (TypeReference) cast.getReference();
					int urlReqRegister = cast.getRegisterA();
					if (type.getType().equals(HTTPURLREQUEST_CLASS)) {
						// invoke agent with register
						modified = true;
						addConnectInvocation(impl, HttpURLConnection.class, urlReqRegister, pos + 1);
					}
				}
			}

			++pos;
		}

		return Pair.of(modified, impl);
	}

	private boolean addCallInvocation(MutableMethodImplementation impl, MethodReference ref, int reg, int pos) {
		if (ref.getDefiningClass().equals(HTTPURLREQUEST_CLASS)) {
			if (requestMethodMapping.containsKey(ref.getName())) {
				String methodLink = requestMethodMapping.get(ref.getName());
				_addCallInvocation(impl, ref.getReturnType(), methodLink, reg, pos);
				return true;
			} else {
				// TODO not linked method -> implement this to increase accuracy
			}
		}
		return false;
	}

	private void _addCallInvocation(MutableMethodImplementation impl, String returnType, String agentMethodName, int param, int pos) {
		MethodReference nRef = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, agentMethodName, returnType, HttpURLConnection.class);

		if (DexInstrumentationUtil.numBits(param) == 4) {
			impl.replaceInstruction(pos, new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, param, 0, 0, 0, 0, nRef));
		} else {
			impl.replaceInstruction(pos, new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, param, 1, nRef));
		}
	}

	private void addConnectInvocation(MutableMethodImplementation impl, Class<?> type, int register, int pos) {
		MethodReference methRef = DexInstrumentationUtil.getMethodReference(AndroidAgent.class, "httpConnect", "V", type);

		if (DexInstrumentationUtil.numBits(register) == 4) {
			BuilderInstruction35c invInstr = new BuilderInstruction35c(Opcode.INVOKE_STATIC, 1, register, 0, 0, 0, 0, methRef);
			impl.addInstruction(pos, invInstr);
		} else {
			BuilderInstruction3rc invInstr = new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, register, 1, methRef);
			impl.addInstruction(pos, invInstr);
		}
	}

}
