package rocks.inspectit.agent.android.speedindex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

/**
 * @author David Monschein
 *
 */
public class SIListenerManager {

	private Map<Class<? extends Activity>, Long> timeMapping;

	public SIListenerManager() {
		timeMapping = new HashMap<Class<? extends Activity>, Long>();
	}

	public void registerListenerFor(final Activity act) {
		ViewGroup root = (ViewGroup) act.findViewById(android.R.id.content).getRootView();
		List<View> allViews = getChildsRecursive(root);
		for (View view : allViews) {
		}

		root.getViewTreeObserver().addOnDrawListener(new OnDrawListener() {
			@Override
			public void onDraw() {
				if (timeMapping.containsKey(act.getClass())) {
					System.out.println("Consumed time: " + (System.currentTimeMillis() - timeMapping.get(act.getClass())));
					timeMapping.remove(act.getClass());
				} else {
					timeMapping.put(act.getClass(), System.currentTimeMillis());
				}
			}
		});

		root.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				if (timeMapping.containsKey(act.getClass())) {
					System.out.println("Consumed time: " + (System.currentTimeMillis() - timeMapping.get(act.getClass())));
					timeMapping.remove(act.getClass());
				} else {
					timeMapping.put(act.getClass(), System.currentTimeMillis());
				}
			}
		});
		;
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