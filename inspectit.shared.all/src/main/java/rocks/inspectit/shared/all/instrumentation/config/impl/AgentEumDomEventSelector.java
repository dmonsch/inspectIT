package rocks.inspectit.shared.all.instrumentation.config.impl;

/**
 * Dom event selectors are used by the Listener-Module of the EUM Agent in order to filter which
 * events shall be monitored and which information to extract from monitored events.
 *
 * @author Jonas Kunz
 *
 */
public class AgentEumDomEventSelector {

	/**
	 * Contains a comma separated lsit of the dom events to capture. See the HTML standard for a
	 * description of which elements are available. Alternatively, a single "*" as wildcard
	 * representing all events.
	 */
	private String eventsList;

	/**
	 * A CSS-Selector string for selecting the elements on which the events will be monitored.
	 */
	private String selector;

	/**
	 * A comma-separated list of attributes which wil lbe extracted form the dom element on which a
	 * monitored event occured. The events are tried to be extracted in the following order form the
	 * following sources:
	 * <ol>
	 * <li>From the HTML attributes, like 'href' or 'id'</li>
	 * <li>From the javaScript-element attributes, like 'tagName'</li>
	 * </ol>
	 * In addition, the implementation provides special attributes starting with a dollar sign, like
	 * "$label". These attributes have a custom implementation on how they are queried. The $label
	 * attribute for example is querried by searching the surrounding DOM for a label which belongs
	 * to the target element.
	 */
	private String attributesToExtractList;

	/**
	 * If this flag is set to true, any event matching this selector will be sent back to the CMR.
	 * If it is false, only matching events which triggered a relevant action, like an AJAX request,
	 * are sent.
	 */
	private boolean alwaysRelevant;

	/**
	 * @param eventsList
	 *            see {@link #eventsList}
	 * @param selector
	 *            see {@link #selector}
	 * @param attributesToExtractList
	 *            see {@link #attributesToExtractList}
	 * @param alwaysRelevant
	 *            see {@link #alwaysRelevant}
	 */
	public AgentEumDomEventSelector(String eventsList, String selector, String attributesToExtractList, boolean alwaysRelevant) {
		super();
		this.eventsList = eventsList;
		this.selector = selector;
		this.attributesToExtractList = attributesToExtractList;
		this.alwaysRelevant = alwaysRelevant;
	}

	/**
	 *
	 */
	public AgentEumDomEventSelector() {
	}

	/**
	 * Gets {@link #eventsList}.
	 *
	 * @return {@link #eventsList}
	 */
	public String getEventsList() {
		return this.eventsList;
	}

	/**
	 * Sets {@link #eventsList}.
	 *
	 * @param eventsList
	 *            New value for {@link #eventsList}
	 */
	public void setEventsList(String eventsList) {
		this.eventsList = eventsList;
	}

	/**
	 * Gets {@link #selector}.
	 *
	 * @return {@link #selector}
	 */
	public String getSelector() {
		return this.selector;
	}

	/**
	 * Sets {@link #selector}.
	 *
	 * @param selector
	 *            New value for {@link #selector}
	 */
	public void setSelector(String selector) {
		this.selector = selector;
	}

	/**
	 * Gets {@link #attributesToExtractList}.
	 *
	 * @return {@link #attributesToExtractList}
	 */
	public String getAttributesToExtractList() {
		return this.attributesToExtractList;
	}

	/**
	 * Sets {@link #attributesToExtractList}.
	 *
	 * @param attributesToExtractList
	 *            New value for {@link #attributesToExtractList}
	 */
	public void setAttributesToExtractList(String attributesToExtractList) {
		this.attributesToExtractList = attributesToExtractList;
	}

	/**
	 * Gets {@link #alwaysRelevant}.
	 *
	 * @return {@link #alwaysRelevant}
	 */
	public boolean isAlwaysRelevant() {
		return this.alwaysRelevant;
	}

	/**
	 * Sets {@link #alwaysRelevant}.
	 *
	 * @param alwaysRelevant
	 *            New value for {@link #alwaysRelevant}
	 */
	public void setAlwaysRelevant(boolean alwaysRelevant) {
		this.alwaysRelevant = alwaysRelevant;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (this.alwaysRelevant ? 1231 : 1237);
		result = (prime * result) + ((this.attributesToExtractList == null) ? 0 : this.attributesToExtractList.hashCode());
		result = (prime * result) + ((this.eventsList == null) ? 0 : this.eventsList.hashCode());
		result = (prime * result) + ((this.selector == null) ? 0 : this.selector.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AgentEumDomEventSelector other = (AgentEumDomEventSelector) obj;
		if (this.alwaysRelevant != other.alwaysRelevant) {
			return false;
		}
		if (this.attributesToExtractList == null) {
			if (other.attributesToExtractList != null) {
				return false;
			}
		} else if (!this.attributesToExtractList.equals(other.attributesToExtractList)) {
			return false;
		}
		if (this.eventsList == null) {
			if (other.eventsList != null) {
				return false;
			}
		} else if (!this.eventsList.equals(other.eventsList)) {
			return false;
		}
		if (this.selector == null) {
			if (other.selector != null) {
				return false;
			}
		} else if (!this.selector.equals(other.selector)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AgentEumDomEventSelector [eventsList=" + this.eventsList + ", selector=" + this.selector + ", attributesToExtractList=" + this.attributesToExtractList + ", alwaysRelevant="
				+ this.alwaysRelevant + "]";
	}

}
