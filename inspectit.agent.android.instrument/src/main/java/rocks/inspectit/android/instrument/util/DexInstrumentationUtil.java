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
import org.jf.dexlib2.builder.instruction.BuilderInstruction11n;
import org.jf.dexlib2.builder.instruction.BuilderInstruction12x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21s;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction31i;
import org.jf.dexlib2.builder.instruction.BuilderInstruction32x;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction3rc;
import org.jf.dexlib2.builder.instruction.BuilderInstruction51l;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.util.MethodUtil;

import com.beust.jcommander.internal.Lists;

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

	public static BuilderInstruction createLongConstant(int firstDestReg, long val) {
		return new BuilderInstruction51l(Opcode.CONST_WIDE, firstDestReg, val);
	}

	public static BuilderInstruction createStringConstant(int destinationRegister, String constant) {
		return new BuilderInstruction21c(Opcode.CONST_STRING, destinationRegister, new ImmutableStringReference(constant));
	}

	public static BuilderInstruction createIntegerConstant(int register, int value) {
		int numBitsReg = numBits(register);
		int numBitsValue = numBits(value);

		if ((numBitsReg == 4) && (numBitsValue == 4)) {
			return new BuilderInstruction11n(Opcode.CONST_4, register, value);
		} else if (numBitsReg <= 8) {
			if (numBitsValue <= 16) {
				return new BuilderInstruction21s(Opcode.CONST_16, register, value);
			} else {
				return new BuilderInstruction31i(Opcode.CONST, register, value);
			}
		} else {
			throw new RuntimeException("Didn't except this to happen.");
		}
	}

	public static int addInstructions(MutableMethodImplementation impl, List<BuilderInstruction> instrs, int initialPos) {
		int k = initialPos;
		for (BuilderInstruction bi : instrs) {
			impl.addInstruction(k++, bi);
		}
		return instrs.size();
	}

	public static BuilderInstruction generateDelegationInvocation(int[] registers, MethodReference method) {
		boolean fourBits = registers.length < 6;

		// check if all registers are 4 bits
		if (fourBits) {
			for (int reg : registers) {
				if ((numBits(reg) == 8) || (numBits(reg) == 16)) {
					fourBits = false;
				}
			}
		}

		// check if registers are valid
		if (!fourBits) {
			for (int k = 0; k < registers.length; k++) {
				if (k != 0) {
					if ((registers[k] - 1) != registers[k - 1]) {
						throw new RuntimeException("Failed to instrument: Registers are > 4 bits or more than 5 registers are needed and the registers are not in ascending order.");
					}
				}
			}
		}

		// generate the invocation
		if (fourBits) {
			return new BuilderInstruction35c(Opcode.INVOKE_STATIC, registers.length, registers.length >= 1 ? registers[0] : 0, registers.length >= 2 ? registers[1] : 0,
					registers.length >= 3 ? registers[2] : 0, registers.length >= 4 ? registers[3] : 0, registers.length >= 5 ? registers[4] : 0, method);
		} else {
			return new BuilderInstruction3rc(Opcode.INVOKE_STATIC_RANGE, registers[0], registers.length, method);
		}
	}

	public static String getMethodSignature(MethodReference ref) {
		StringBuilder builder = new StringBuilder();
		builder.append(ref.getDefiningClass());
		builder.append(ref.getName());
		builder.append("(");
		String prefix = "";
		for (CharSequence para : ref.getParameterTypes()) {
			builder.append(prefix);
			builder.append(para);
			prefix = ",";
		}
		builder.append(")");
		builder.append(ref.getReturnType());
		return builder.toString();
	}

	public static MethodReference getMethodReference(String clazzType, String name, String returnType, String... parameterTypes) {
		List<String> parameters = Lists.newArrayList(parameterTypes);
		return getMethodReference(clazzType, name, returnType, parameters);
	}

	public static MethodReference getMethodReference(Class<?> clazz, String name, String returnType, String... paras) {
		return getMethodReference(DexInstrumentationUtil.getType(clazz), name, returnType, paras);
	}

	public static MethodReference getMethodReference(Class<?> clazz, String name, String returnType) {
		return getMethodReference(DexInstrumentationUtil.getType(clazz), name, returnType, Lists.newArrayList());
	}

	public static MethodReference getMethodReference(Class<?> clazz, String name, String returnType, Class<?>... parameters) {
		List<String> parameterList = new ArrayList<>();
		for (Class<?> paraClass : parameters) {
			parameterList.add(DexInstrumentationUtil.getType(paraClass));
		}

		return getMethodReference(DexInstrumentationUtil.getType(clazz), name, returnType, parameterList);
	}

	public static MethodReference getMethodReference(String clazzType, String name, String returnType, List<String> paras) {
		return new ImmutableMethodReference(clazzType, name, paras, returnType);
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

	public static BuilderInstruction moveRegister(int dest, int src, Opcode opcode) {
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
		} else if (reg < 0x00FFFFFF) {
			return 32;
		} else {
			return 64;
		}
	}
}
