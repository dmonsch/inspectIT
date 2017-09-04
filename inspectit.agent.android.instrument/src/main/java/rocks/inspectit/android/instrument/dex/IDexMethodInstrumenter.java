package rocks.inspectit.android.instrument.dex;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;

/**
 * @author David Monschein
 *
 */
public interface IDexMethodInstrumenter {

	public Method instrumentMethod(ClassDef parent, Method meth);

	public boolean isTargetMethod(Method method);

}
