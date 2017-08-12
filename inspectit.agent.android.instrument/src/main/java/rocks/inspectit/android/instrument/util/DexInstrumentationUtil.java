package rocks.inspectit.android.instrument.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction32x;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.util.MethodUtil;

/**
 * @author David Monschein
 *
 */
public class DexInstrumentationUtil {

	public static int getMethodCount(File dexFile) throws IOException {
		DexBackedDexFile dex = DexFileFactory.loadDexFile(dexFile, Opcodes.forApi(19));
		return dex.getMethodCount();
	}

	// UTILS
	public static String getType(Class<?> clz) {
		// PRIMITIVE TYPES
		if (clz.isPrimitive()) {
			if (clz.equals(int.class)) {
				return "I";
			} else if (clz.equals(boolean.class)) {
				return "Z";
			} else if (clz.equals(double.class)) {
				return "D";
			} else if (clz.equals(float.class)) {
				return "F";
			} else if (clz.equals(long.class)) {
				return "J";
			} else if (clz.equals(short.class)) {
				return "S";
			} else if (clz.equals(char.class)) {
				return "C";
			} else if (clz.equals(void.class)) {
				return "V";
			}
		}

		return "L" + clz.getName().replaceAll("\\.", "/") + ";";
	}

	public static String getMethodSignature(Method method) {
		StringBuilder builder = new StringBuilder();
		builder.append(method.getDefiningClass().replaceAll("/", ".")).substring(1, method.getDefiningClass().length() - 1);
		builder.append(method.getName());
		builder.append("(");
		String prefix = "";
		for (MethodParameter para : method.getParameters()) {
			builder.append(para.getType());
			builder.append(prefix);
			prefix = ",";
		}
		builder.append(")");
		builder.append(method.getReturnType());
		return builder.toString();
	}

	public static MethodReference getMethodReference(Class<?> clazz, String name, String returnType, Class<?>... parameters) {
		List<String> parameterList = new ArrayList<>();
		for (Class<?> paraClass : parameters) {
			parameterList.add(DexInstrumentationUtil.getType(paraClass));
		}

		return new ImmutableMethodReference(DexInstrumentationUtil.getType(clazz), name, parameterList, returnType);
	}

	public static int getThisRegister(int maxRegisters, int parameters) {
		return maxRegisters - parameters;
	}

	public static Pair<Integer, MutableMethodImplementation> extendMethodRegisters(final Method meth, final int newRegisters) {
		final MethodImplementation original = meth.getImplementation();

		int numRegisters = original.getRegisterCount();
		final int numParameterRegisters = MethodUtil.getParameterRegisterCount(meth);
		int numNonParameterRegisters = numRegisters - numParameterRegisters;

		// this is necessary because dexlib2 has no constructor to set both implementation and
		// register count
		MethodImplementation tempMock = new MethodImplementation() {
			@Override
			public List<? extends TryBlock<? extends ExceptionHandler>> getTryBlocks() {
				return original.getTryBlocks();
			}

			@Override
			public int getRegisterCount() {
				return original.getRegisterCount() + newRegisters + numParameterRegisters;
			}

			@Override
			public Iterable<? extends Instruction> getInstructions() {
				return original.getInstructions();
			}

			@Override
			public Iterable<? extends DebugItem> getDebugItems() {
				return original.getDebugItems();
			}
		};

		MutableMethodImplementation ret = new MutableMethodImplementation(tempMock);

		// create move statements
		int addedInstructions = createMoveStatements(meth, ret, numNonParameterRegisters, newRegisters + numParameterRegisters);

		return new ImmutablePair<Integer, MutableMethodImplementation>(addedInstructions, ret);
	}

	// TODO rebuild
	private static int createMoveStatements(Method method, MutableMethodImplementation ret, int nonParameterRegs, int nreg) {
		int dest = nonParameterRegs;
		int instrs = 0;

		BuilderInstruction newInstruction;
		if (!MethodUtil.isStatic(method)) {
			newInstruction = moveRegister(dest, dest + nreg, Opcode.MOVE_OBJECT);
			ret.addInstruction(0, newInstruction);
			dest++;
			instrs++;
		}

		for (CharSequence paramType : method.getParameterTypes()) {
			int firstChar = paramType.charAt(0);
			if ((firstChar == 'J') || (firstChar == 'D')) {
				newInstruction = moveRegister(dest, dest + nreg, Opcode.MOVE_WIDE);
				dest += 2;
			} else {
				if ((firstChar == '[') || (firstChar == 'L')) {
					newInstruction = moveRegister(dest, dest + nreg, Opcode.MOVE_OBJECT);
				} else {
					newInstruction = moveRegister(dest, dest + nreg, Opcode.MOVE);
				}
				dest++;
			}
			ret.addInstruction(0, newInstruction);
			instrs++;
		}

		return instrs;
	}

	private static BuilderInstruction moveRegister(int dest, int src, Opcode opcode) {
		int destNumBits = numBits(dest);
		int srcNumBits = numBits(src);

		if (destNumBits == 4) {
			if (srcNumBits == 4) {
				return new BuilderInstruction12x(opcode, dest, src);
			} else {
				return new BuilderInstruction22x(opcode_FROM16(opcode), dest, src);
			}
		}
		if (destNumBits == 8) {
			return new BuilderInstruction22x(opcode_FROM16(opcode), dest, src);
		}
		if (destNumBits == 16) {
			return new BuilderInstruction32x(opcode_16(opcode), dest, src);
		}
		throw new RuntimeException("Unexpected: " + destNumBits + " " + srcNumBits);
	}

	private static Opcode opcode_FROM16(Opcode opcode) {
		switch (opcode) {
		case MOVE:
			return Opcode.MOVE_FROM16;
		case MOVE_OBJECT:
			return Opcode.MOVE_OBJECT_FROM16;
		case MOVE_WIDE:
			return Opcode.MOVE_WIDE_FROM16;
		default:
			throw new RuntimeException("unexpected " + opcode);
		}
	}

	private static Opcode opcode_16(Opcode opcode) {
		switch (opcode) {
		case MOVE:
			return Opcode.MOVE_16;
		case MOVE_OBJECT:
			return Opcode.MOVE_OBJECT_16;
		case MOVE_WIDE:
			return Opcode.MOVE_WIDE_16;
		default:
			throw new RuntimeException("unexpected " + opcode);
		}
	}

	public static int numBits(int reg) {
		if (reg < 0x0000000F) {
			return 4;
		} else if (reg < 0x000000FF) {
			return 8;
		} else if (reg < 0x0000FFFF) {
			return 16;
		} else {
			throw new RuntimeException("More than 16 bits is required to encode register " + reg);
		}
	}
}
