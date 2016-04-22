package rocks.inspectit.ui.rcp.view.tree;

import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.DeferredTreeContentManager;

import rocks.inspectit.ui.rcp.model.Composite;
import rocks.inspectit.ui.rcp.model.TreeModelManager;
import rocks.inspectit.ui.rcp.util.ListenerList;

/**
 * The content provider for the tree viewer used for every single available CMR.
 *
 * @author Patrice Bouillet
 *
 */
public class TreeContentProvider implements ITreeContentProvider {

	/**
	 * The manager is used to access the deferred objects.
	 */
	private DeferredTreeContentManager manager;

	/**
	 * Listeners that will be passed to the {@link DeferredTreeContentManager}.
	 */
	private ListenerList<IJobChangeListener> updateCompleteListenerList;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (manager.isDeferredAdapter(parentElement)) {
			Object[] children = manager.getChildren(parentElement);

			return children;
		} else if (parentElement instanceof Composite) {
			// direct access to the children
			Composite composite = (Composite) parentElement;
			return composite.getChildren().toArray();
		}
		return new Object[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object element) {
		if (element instanceof Composite) {
			Composite composite = (Composite) element;
			return composite.getParent();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren(Object element) {
		if (null == element) {
			return false;
		}

		if (manager.isDeferredAdapter(element)) {
			return manager.mayHaveChildren(element);
		}

		if (element instanceof Composite) {
			Composite composite = (Composite) element;
			return composite.hasChildren();
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		TreeModelManager treeModelManager = (TreeModelManager) inputElement;
		return treeModelManager.getRootElements();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		manager = new DeferredTreeContentManager((AbstractTreeViewer) viewer);
		if (null != updateCompleteListenerList) {
			for (IJobChangeListener iJobChangeListener : updateCompleteListenerList) {
				manager.addUpdateCompleteListener(iJobChangeListener);
			}
		}
	}

	/**
	 * Adds the listener to the update job that updates the elements. The listener will be added to
	 * the {@link DeferredTreeContentManager} if one is initialized. In any case the listener will
	 * be added when the new manager is initialized in the future.
	 *
	 * @param listener
	 *            {@link IJobChangeListener}
	 */
	public void addUpdateCompleteListener(IJobChangeListener listener) {
		if (null == updateCompleteListenerList) {
			updateCompleteListenerList = new ListenerList<>();
		}
		updateCompleteListenerList.add(listener);
		if (null != manager) {
			manager.addUpdateCompleteListener(listener);
		}
	}

	/**
	 * Adds the listener to the update job that updates the elements. The listener will be removed
	 * from the {@link DeferredTreeContentManager} if one is initialized.
	 *
	 * @param listener
	 *            {@link IJobChangeListener}
	 */
	public void removeUpdateCompleteListener(IJobChangeListener listener) {
		if (null != updateCompleteListenerList) {
			updateCompleteListenerList.remove(listener);
		}
		if (null != manager) {
			manager.removeUpdateCompleteListener(listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
	}

}
