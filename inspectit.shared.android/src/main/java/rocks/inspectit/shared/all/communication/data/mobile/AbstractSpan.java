package rocks.inspectit.shared.all.communication.data.mobile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.html.HTML.Tag;

/**
 * @author David Monschein
 *
 */
public abstract class AbstractSpan extends DefaultData {

	/**
	 * Duration of this span in milliseconds. We keep same resolution here as for the duration of
	 * our other monitoring data.
	 */
	private double duration;

	/**
	 * The unique identifier of the method.
	 */
	private long methodIdent;

	private PropagationType propagationType;

	/**
	 * Reference type.
	 */
	private String referenceType;

	/**
	 * ID of the span's parent. Can be 0 to denote that there is no parent.
	 */
	private long parentSpanId;

	/**
	 * Defined tags.
	 */
	private Map<String, String> tags;

	/**
	 * Identifier of this span.
	 */
	private SpanIdent spanIdent;

	/**
	 * Gets {@link #duration}.
	 *
	 * @return {@link #duration}
	 */
	public double getDuration() {
		return duration;
	}

	/**
	 * Sets {@link #duration}.
	 *
	 * @param duration
	 *            New value for {@link #duration}
	 */
	public void setDuration(double duration) {
		this.duration = duration;
	}

	/**
	 * Gets {@link #methodIdent}.
	 *
	 * @return {@link #methodIdent}
	 */
	public long getMethodIdent() {
		return methodIdent;
	}

	/**
	 * Sets {@link #methodIdent}.
	 *
	 * @param methodIdent
	 *            New value for {@link #methodIdent}
	 */
	public void setMethodIdent(long methodIdent) {
		this.methodIdent = methodIdent;
	}

	/**
	 * {@inheritDoc}
	 */
	public abstract boolean isCaller();

	/**
	 * Gets {@link #spanIdent}.
	 *
	 * @return {@link #spanIdent}
	 */
	public SpanIdent getSpanIdent() {
		return this.spanIdent;
	}

	/**
	 * Sets {@link #spanIdent}.
	 *
	 * @param spanIdent
	 *            New value for {@link #spanIdent}
	 */
	public void setSpanIdent(SpanIdent spanIdent) {
		this.spanIdent = spanIdent;
	}

	/**
	 * Gets {@link #propagationType}.
	 *
	 * @return {@link #propagationType}
	 */
	public PropagationType getPropagationType() {
		return this.propagationType;
	}

	/**
	 * Sets {@link #propagationType}.
	 *
	 * @param propagationType
	 *            New value for {@link #propagationType}
	 */
	public void setPropagationType(PropagationType propagationType) {
		this.propagationType = propagationType;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReferenceType() {
		return referenceType;
	}

	/**
	 * Sets {@link #ReferenceType}.
	 *
	 * @param referenceType
	 *            New value for {@link #ReferenceType}
	 */
	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	/**
	 * Gets {@link #parentSpanId}.
	 *
	 * @return {@link #parentSpanId}
	 */
	public long getParentSpanId() {
		return this.parentSpanId;
	}

	/**
	 * Sets {@link #parentSpanId}.
	 *
	 * @param parentId
	 *            New value for {@link #parentSpanId} or 0 to make this span a root.
	 */
	public void setParentSpanId(long parentId) {
		this.parentSpanId = parentId;
	}

	/**
	 * If this is span is a root span.
	 *
	 * @return If this is span identification for a root span.
	 */
	public boolean isRoot() {
		return this.parentSpanId == 0;
	}

	/**
	 * Same as {@link #isRoot()}. Needed for querying.
	 *
	 * @return {@link #isRoot()}
	 */
	public boolean getRoot() { // NOPMD
		return isRoot();
	}

	/**
	 * Adds tag to this span.
	 *
	 * @param tag
	 *            {@link Tag}, must not be <code>null</code>.
	 * @param value
	 *            String value, must not be <code>null</code>.
	 * @return Old value associated with same tag.
	 */
	public String addTag(String tag, String value) {
		if (null == tags) {
			tags = new HashMap<String, String>(1, 1f);
		}
		return tags.put(tag, value);
	}

	/**
	 * Adds all tags from the given map to the tags of this span.
	 *
	 * @param otherTags
	 *            Map of tags to add.
	 */
	public void addAllTags(Map<String, String> otherTags) {
		if (null == tags) {
			tags = new HashMap<String, String>(otherTags.size(), 1f);
		}
		tags.putAll(otherTags);
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> getTags() {
		if (null == tags) {
			return Collections.emptyMap();
		} else {
			return Collections.unmodifiableMap(tags);
		}
	}

}
