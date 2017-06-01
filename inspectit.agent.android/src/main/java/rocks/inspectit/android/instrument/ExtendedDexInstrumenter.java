package rocks.inspectit.android.instrument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.base.reference.BaseMethodReference;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction21c;
import org.jf.dexlib2.builder.instruction.BuilderInstruction35c;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.ExceptionHandler;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.TryBlock;
import org.jf.dexlib2.iface.debug.DebugItem;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.rewriter.ClassDefRewriter;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.RewriterUtils;
import org.jf.dexlib2.rewriter.Rewriters;
import org.jf.dexlib2.util.MethodUtil;

import rocks.inspectit.android.instrument.config.InstrumentationConfiguration;

/**
 * @author David Monschein
 *
 */
public class ExtendedDexInstrumenter {

	public ExtendedDexInstrumenter(InstrumentationConfiguration config) {
	}

	public void instrument(File input, File output) throws IOException {
		DexBackedDexFile dex = DexFileFactory.loadDexFile(input, Opcodes.forApi(19));

		final Map<String, String> superClassMapping = new HashMap<>();
		RewriterModule rew_module = new RewriterModule() {

			/*
			 * @Override public Rewriter<Instruction> getInstructionRewriter(Rewriters rewriters) {
			 * return new Rewriter<Instruction>() {
			 * @Override public Instruction rewrite(Instruction value) { if (value.getOpcode() ==
			 * Opcode.INVOKE_STATIC) { // DexBackedInstruction35c i = (DexBackedInstruction35c)
			 * value; // System.out.println("_______ BEGIN ________"); //
			 * System.out.println(i.getRegisterCount()); //
			 * System.out.println("_______________________"); //
			 * System.out.println(i.getRegisterC()); // System.out.println(i.getRegisterD()); //
			 * System.out.println(i.getRegisterE()); // System.out.println(i.getRegisterF()); //
			 * System.out.println(i.getRegisterG()); // System.out.println(i.getReference()); //
			 * System.out.println("_______________________"); } else if (value.getOpcode() ==
			 * Opcode.IPUT) { DexBackedInstruction22c i = (DexBackedInstruction22c) value; } else if
			 * (value.getOpcode() == Opcode.IGET_OBJECT) { DexBackedInstruction22c i =
			 * (DexBackedInstruction22c) value; if (i.getReference().toString().contains("this")) {
			 * System.out.println("_______ BEGIN ________");
			 * System.out.println(i.getReferenceType()); System.out.println(i.getCodeUnits());
			 * System.out.println(i.getRegisterA()); System.out.println(i.getRegisterB());
			 * DexBackedFieldReference ref = (DexBackedFieldReference) i.getReference();
			 * System.out.println(ref.getDefiningClass()); System.out.println(ref.getName());
			 * System.out.println(ref.getType()); } } else if (value.getOpcode() ==
			 * Opcode.MOVE_OBJECT) { DexBackedInstruction12x i = (DexBackedInstruction12x) value; //
			 * System.out.println(i.getRegisterA()); } else if (value.getOpcode() ==
			 * Opcode.CONST_STRING) { DexBackedInstruction21c i = (DexBackedInstruction21c) value;
			 * System.out.println("_______ BEGIN ________"); System.out.println(i.getRegisterA());
			 * DexBackedStringReference ref = (DexBackedStringReference) i.getReference();
			 * System.out.println(ref.getString()); } else { System.out.println(value.getOpcode() +
			 * " - " + value.getClass()); } return null; } }; }
			 */

			@Override
			public Rewriter<Method> getMethodRewriter(final Rewriters rewriters) {
				return new Rewriter<Method>() {
					@Override
					public Method rewrite(Method value) {
						return new RewrittenMethod(rewriters, value);
					}
				};
			}

			/*
			 * @Override public Rewriter<Method> getMethodRewriter(Rewriters rewriters) { return new
			 * Rewriter<Method>() {
			 * @Override public Method rewrite(Method value) { if
			 * (value.getName().equals("wantToKnow")) {
			 * System.out.println(value.getImplementation().getRegisterCount()); for (Instruction
			 * instr : value.getImplementation().getInstructions()) { if (instr.getOpcode() ==
			 * Opcode.CONST_STRING) { DexBackedInstruction21c i = (DexBackedInstruction21c) instr;
			 * DexBackedStringReference strRef = (DexBackedStringReference) i.getReference();
			 * System.out.println(i.getRegisterA()); } else if (instr.getOpcode() ==
			 * Opcode.INVOKE_STATIC) { DexBackedInstruction35c i = (DexBackedInstruction35c) instr;
			 * DexBackedMethodReference ref = (DexBackedMethodReference) i.getReference();
			 * System.out.println(ref.getReturnType()); } else if (instr.getOpcode() ==
			 * Opcode.MUL_FLOAT) { DexBackedInstruction23x i = (DexBackedInstruction23x) instr;
			 * System.out.println(i.getRegisterA() + "," + i.getRegisterB() + "," +
			 * i.getRegisterC()); } else if (instr.getOpcode() == Opcode.INT_TO_FLOAT) {
			 * DexBackedInstruction12x i = (DexBackedInstruction12x) instr;
			 * System.out.println(i.getRegisterA() + "." + i.getRegisterB()); } else if
			 * (instr.getOpcode() == Opcode.SGET_OBJECT) { DexBackedInstruction21c i =
			 * (DexBackedInstruction21c) instr; System.out.println(i.getRegisterA()); } else if
			 * (instr.getOpcode() == Opcode.INVOKE_VIRTUAL) { DexBackedInstruction35c i =
			 * (DexBackedInstruction35c) instr; System.out.println("Reg: " + i.getRegisterC());
			 * System.out.println("Count:" + i.getRegisterCount()); System.out.println("Target: " +
			 * i.getRegisterD()); } System.out.println(instr.getOpcode()); } } return value; } }; }
			 */

			@Override
			public Rewriter<ClassDef> getClassDefRewriter(Rewriters rewriters) {
				return new ClassDefRewriter(rewriters) {
					@Override
					public ClassDef rewrite(ClassDef classDef) {
						superClassMapping.put(classDef.getType(), classDef.getSuperclass());
						return super.rewrite(classDef);
					}
				};
			}
		};

		DexRewriter rewriter = new DexRewriter(rew_module);
		DexFile ooo = rewriter.rewriteDexFile(dex);

		DexFileFactory.writeDexFile(output.getAbsolutePath(), ooo);
	}

	protected class RewrittenMethod extends BaseMethodReference implements Method {
		protected Method method;
		protected Rewriters rewriters;

		public RewrittenMethod(Rewriters rewriters, Method method) {
			this.method = method;
			this.rewriters = rewriters;
		}

		@Override
		public String getDefiningClass() {
			return rewriters.getMethodReferenceRewriter().rewrite(method).getDefiningClass();
		}

		@Override
		public String getName() {
			return rewriters.getMethodReferenceRewriter().rewrite(method).getName();
		}

		@Override
		public List<? extends CharSequence> getParameterTypes() {
			return rewriters.getMethodReferenceRewriter().rewrite(method).getParameterTypes();
		}

		@Override
		public List<? extends MethodParameter> getParameters() {
			return RewriterUtils.rewriteList(rewriters.getMethodParameterRewriter(), method.getParameters());
		}

		@Override
		public String getReturnType() {
			return rewriters.getMethodReferenceRewriter().rewrite(method).getReturnType();
		}

		@Override
		public int getAccessFlags() {
			return method.getAccessFlags();
		}

		@Override
		public Set<? extends Annotation> getAnnotations() {
			return RewriterUtils.rewriteSet(rewriters.getAnnotationRewriter(), method.getAnnotations());
		}

		@Override
		public MethodImplementation getImplementation() {
			if (method.getName().equals("onCreate") && method.getDefiningClass().contains("MainActivity")) {
				System.out.println(method.getDefiningClass());
				return new RewrittenMethodImplementation(rewriters, getParameters(), AccessFlags.STATIC.isSet(getAccessFlags()), method.getImplementation());
			} else {
				return RewriterUtils.rewriteNullable(rewriters.getMethodImplementationRewriter(), method.getImplementation());
			}
		}
	}

	protected class RewrittenMethodImplementation implements MethodImplementation {
		protected MutableMethodImplementation methodImplementation;
		protected Rewriters rewriters;

		private boolean isStatic;
		private List<? extends MethodParameter> parameters;

		public RewrittenMethodImplementation(Rewriters rewriters, List<? extends MethodParameter> parameterCount, boolean isStatic, MethodImplementation methodImplementation) {
			this.methodImplementation = new MutableMethodImplementation(methodImplementation);
			this.rewriters = rewriters;

			this.isStatic = isStatic;
			this.parameters = parameterCount;
		}

		@Override
		public int getRegisterCount() {
			return methodImplementation.getRegisterCount();
		}

		@Override
		public Iterable<? extends Instruction> getInstructions() {
			List<BuilderInstruction> toAdd = createHelloWorldPrint((this.getRegisterCount() - MethodUtil.getParameterRegisterCount(parameters, isStatic)) + 1);
			for (int i = toAdd.size() - 1; i >= 0; i--) {
				methodImplementation.addInstruction(0, toAdd.get(i));
			}
			return RewriterUtils.rewriteIterable(rewriters.getInstructionRewriter(), methodImplementation.getInstructions());

			// return RewriterUtils.rewriteIterable(rewriters.getInstructionRewriter(),
			// methodImplementation.getInstructions());
		}

		@Override
		public List<? extends TryBlock<? extends ExceptionHandler>> getTryBlocks() {
			return RewriterUtils.rewriteList(rewriters.getTryBlockRewriter(), methodImplementation.getTryBlocks());
		}

		@Override
		public Iterable<? extends DebugItem> getDebugItems() {
			return RewriterUtils.rewriteIterable(rewriters.getDebugItemRewriter(), methodImplementation.getDebugItems());
		}
	}

	private List<BuilderInstruction> createHelloWorldPrint(int thisRegister) {
		List<BuilderInstruction> l = new ArrayList<>();

		BuilderInstruction21c loadStringInstruction = new BuilderInstruction21c(Opcode.CONST_STRING, 0, new ImmutableStringReference("Tag"));
		// BuilderInstruction21c loadStringInstruction2 = new
		// BuilderInstruction21c(Opcode.CONST_STRING, 1, new ImmutableStringReference("Message"));

		// ImmutableInstruction22c loadInstruction = new ImmutableInstruction22c(opcode, registerA,
		// registerB, reference);

		List<String> parameterList = new ArrayList<String>();

		ImmutableMethodReference methodReference = new ImmutableMethodReference("Levaluation/mobile/iobserve/org/benchmarkapp/MainActivity;", "wantToKnow", Collections.unmodifiableList(parameterList),
				"V");
		BuilderInstruction35c invokeInstruction = new BuilderInstruction35c(Opcode.INVOKE_DIRECT, 1, thisRegister, 0, 0, 0, 0, methodReference);

		l.add(invokeInstruction);
		// l.add(loadStringInstruction2);
		// l.add(invokeInstruction);

		return l;
	}

}
