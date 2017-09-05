package rocks.inspectit.android.instrument.dex;

import org.apache.commons.lang3.tuple.Pair;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.iface.Method;

/**
 * @author David Monschein
 *
 */
public interface IDexMethodImplementationInstrumenter {

	public Pair<Boolean, MutableMethodImplementation> instrument(Method reference);

}
