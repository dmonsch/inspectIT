package rocks.inspectit.agent.instrument;

import java.io.File;
import java.io.IOException;

import rocks.inspectit.android.instrument.DexInstrumenter;

/**
 * @author David Monschein
 *
 */
public class BytecodeTest {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		DexInstrumenter instr = new DexInstrumenter(null);
		instr.instrument(new File("classes.dex"), new File("instrumented-classes.dex"));

	}

}
