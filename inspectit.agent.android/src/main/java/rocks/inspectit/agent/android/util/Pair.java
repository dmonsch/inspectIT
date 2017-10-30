package rocks.inspectit.agent.android.util;

/**
 * @author David Monschein
 *
 */
public class Pair<A, B> {

	private A left;
	private B right;

	public Pair(A a, B b) {
		this.setLeft(a);
		this.setRight(b);
	}

	public A getFirst() {
		return left;
	}

	public B getSecond() {
		return right;
	}

	/**
	 * Gets {@link #left}.
	 *
	 * @return {@link #left}
	 */
	public A getLeft() {
		return left;
	}

	/**
	 * Sets {@link #left}.
	 *
	 * @param left
	 *            New value for {@link #left}
	 */
	public void setLeft(A left) {
		this.left = left;
	}

	/**
	 * Gets {@link #right}.
	 *
	 * @return {@link #right}
	 */
	public B getRight() {
		return right;
	}

	/**
	 * Sets {@link #right}.
	 *
	 * @param right
	 *            New value for {@link #right}
	 */
	public void setRight(B right) {
		this.right = right;
	}

}
