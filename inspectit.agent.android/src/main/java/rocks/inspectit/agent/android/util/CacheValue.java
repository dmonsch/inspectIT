package rocks.inspectit.agent.android.util;

/**
 * Class for the easy creation of values which will be cached for a specified amount of time.
 *
 * @author David Monschein
 *
 * @param <T>
 *            Type of the value to store.
 */
public class CacheValue<T> {

	/**
	 * Value of the cache.
	 */
	private T value;

	/**
	 * Timestamp of the cache.
	 */
	private long timestamp;

	/**
	 * Amount of time how long the cache is valid.
	 */
	private long validity;

	/**
	 * Determines whether the cache value is valid for ever or not.
	 */
	private boolean ever;

	/**
	 * Specifies whether the value of the cache has been ever set or not.
	 */
	private boolean inited;

	/**
	 * Creates a new cache from a specified value and a specified valid time.
	 *
	 * @param val
	 *            value of the cache
	 * @param validity
	 *            amount of time how long the cache is valid
	 */
	public CacheValue(T val, long validity) {
		this.set(val);
		this.validity = validity;
		this.ever = false;
		this.inited = true;
	}

	/**
	 * Initializes a new cache without a present value.
	 *
	 * @param val
	 *            amount of time how long the cache is valid
	 */
	public CacheValue(long val) {
		this.timestamp = 0;
		this.validity = val;
		this.ever = false;
		this.inited = false;
	}

	/**
	 * Creates a new cache without a present value which is valid for ever.
	 */
	public CacheValue() {
		this.timestamp = 0;
		this.ever = true;
		this.inited = false;
	}

	/**
	 * Determines whether the cache is valid or not valid anymore.
	 *
	 * @return true if the cache is valid - false other wise
	 */
	public boolean valid() {
		return (ever && inited) || ((System.currentTimeMillis() - timestamp) <= this.validity);
	}

	/**
	 * Gets the value of the cache.
	 *
	 * @return value of the cache
	 */
	public T value() {
		return value;
	}

	/**
	 * Sets the value of the cache to a specified value.
	 *
	 * @param val
	 *            the new value which should be set
	 * @return the value of the input parameter
	 */
	public T set(T val) {
		this.inited = true;
		this.value = val;
		this.timestamp = System.currentTimeMillis();
		return val;
	}

}
