package rocks.inspectit.agent.android.business;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author David Monschein
 *
 */
public class BusinessLogicManager {

	public BusinessLogicManager() {
	}

	public void registerListenerFor(final Activity act) {
		ViewGroup root = (ViewGroup) act.findViewById(android.R.id.content).getRootView();
		List<View> allViews = getChildsRecursive(root);
	}

	private List<View> getChildsRecursive(ViewGroup group) {
		return _getChildsRecursive(new ArrayList<View>(), group);
	}

	private List<View> _getChildsRecursive(List<View> base, ViewGroup group) {
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			if (child instanceof ViewGroup) {
				_getChildsRecursive(base, (ViewGroup) child);
			} else {
				base.add(child);
			}
		}
		return base;
	}

}
