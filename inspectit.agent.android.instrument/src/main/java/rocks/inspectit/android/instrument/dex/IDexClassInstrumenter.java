package rocks.inspectit.android.instrument.dex;

import org.jf.dexlib2.iface.ClassDef;

/**
 * @author David Monschein
 *
 */
public interface IDexClassInstrumenter extends IDexMethodInstrumenter {

	public ClassDef instrumentClass(ClassDef clazz);

	public boolean isTargetClass(ClassDef clazz);

}
