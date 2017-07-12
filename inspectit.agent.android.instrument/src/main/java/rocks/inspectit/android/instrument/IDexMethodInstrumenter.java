package rocks.inspectit.android.instrument;

import org.jf.dexlib2.iface.Method;

/**
 * @author David Monschein
 *
 */
public interface IDexMethodInstrumenter {

	public Method instrumentMethod(Method meth);

}
