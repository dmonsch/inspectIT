package rocks.inspectit.agent.java.eum;

/**
 *
 * Utiltiy method for performing some text searching on a given text. A carret just represents a
 * position within this text and can be moved around.
 *
 * @author Jonas Kunz
 */
public class Carret implements Cloneable {

	/**
	 * The text this carret opperates on.
	 */
	private final CharSequence src;

	/**
	 * The current position of the carret, as an offset from the text beginning.
	 */
	private int offset;

	/**
	 * Creates a new carret.
	 * 
	 * @param src
	 *            the sourcetext
	 * @param offset
	 *            the carret position
	 */
	public Carret(CharSequence src, int offset) {
		super();
		this.src = src;
		this.offset = offset;
	}

	/**
	 * Creates a carret pointing to the beginning of the given text.
	 * 
	 * @param src
	 *            the text
	 */
	public Carret(CharSequence src) {
		this(src, 0);
	}

	/**
	 * Copy-Constructor.
	 * 
	 * @param copyOf
	 *            the carret to copy.
	 */
	public Carret(Carret copyOf) {
		this.src = copyOf.src;
		this.offset = copyOf.offset;
	}

	public int getOffset() {
		return offset;
	}

	/**
	 * Moves the carret n characters.
	 * 
	 * @param n
	 *            the number of characters to move (negative values for walking backwards)
	 */
	public void go(int n) {
		offset = Math.min(src.length(), Math.max(0, offset + n));

	}

	/**
	 * returns the character at the given offset from this carret.
	 * 
	 * @param additionOff
	 *            the additional offset
	 * @return the character at (carret-position + additionOff)
	 */
	public char get(int additionOff) {
		return src.charAt(offset + additionOff);
	}

	/**
	 * @return the number of characters left until the end of the text.
	 */
	public int wayToEnd() {
		return src.length() - offset;
	}

	@Override
	protected Carret clone() {
		return new Carret(this);
	}

	/**
	 * Walks forward with the carret until it points at the given character. Case-Sensitive
	 * comparison is used.
	 * 
	 * @param c
	 *            the character to walk to
	 * @return true if the character was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkToCharCC(char c) {
		while (!endReached() && src.charAt(offset) != c) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * Walks forward with the carret until it points at the given character. Then advances the
	 * position by one. Case-Sensitive comparison is used.
	 * 
	 * @param c
	 *            the character to walk to
	 * @return true if the character was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkAfterCharCC(char c) {
		boolean found = walkToCharCC(c);
		if (found) {
			offset++;
		}
		return found;
	}

	/**
	 * Walks forward with the carret until it points at a position which matches the given string.
	 * Case-Insensitive comparison is used.
	 * 
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkToMatchIC(String strToMatch) {
		while (!endReached() && !startsWithIC(strToMatch)) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * Walks forward with the carret until it points at a position which matches the given string.
	 * Case-sensitive comparison is used.
	 * 
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkToMatchCC(String strToMatch) {
		while (!endReached() && !startsWithIC(strToMatch)) {
			offset++;
		}
		return !endReached();
	}

	/**
	 * Walks forward with the carret until it points at a position which matches the given string.
	 * Then advances the position after this string. Case-Insensitive comparison is used.
	 * 
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkAfterMatchIC(String strToMatch) {
		boolean found = walkToMatchIC(strToMatch);
		if (found) {
			offset += strToMatch.length();
		}
		return found;
	}

	/**
	 * Walks forward with the carret until it points at a position which matches the given string.
	 * Then advances the position after this string. Case-Sensitive comparison is used.
	 * 
	 * @param strToMatch
	 *            the string to match
	 * @return true if the string was found, false if the end of the string was reached while
	 *         searching
	 */
	public boolean walkAfterMatchCC(String strToMatch) {
		boolean found = walkToMatchCC(strToMatch);
		if (found) {
			offset += strToMatch.length();
		}
		return found;
	}

	/**
	 * Checks if the substring of the source text starting at the carrets position starts with the
	 * give String. Case-Insensitive Comparison is used.
	 * 
	 * @param strToMatch
	 *            the string to match
	 * @return true if matched
	 */
	public boolean startsWithIC(String strToMatch) {
		int len = strToMatch.length();
		return src.subSequence(offset, Math.min(src.length(), offset + len)).toString().equalsIgnoreCase(strToMatch);
	}

	/**
	 * Checks if the substring of the source text starting at the carrets position starts with the
	 * give String. Case-Sensitive Comparison is used.
	 * 
	 * @param strToMatch
	 *            the string to match
	 * @return true if matched
	 */
	public boolean startsWithCC(String strToMatch) {
		int len = strToMatch.length();
		String subseq = src.subSequence(offset, Math.min(src.length(), offset + len)).toString();
		return subseq.equals(strToMatch);
	}

	/**
	 * Walks backwards until the carret points at a non-whitespace character.
	 */
	public void walkBackWhitespaces() {

		while (offset > 0 && Character.isWhitespace(src.charAt(offset - 1))) {
			offset--;
		}
	}

	/**
	 * Walks forward until the carret points at a non-whitespace character.
	 */
	public void walkAfterWhitespaces() {
		while (!endReached() && Character.isWhitespace(src.charAt(offset))) {
			offset++;
		}
	}

	/**
	 * @return true if the end was reached (the carret points after the last character)
	 *
	 */
	public boolean endReached() {
		return offset == src.length();
	}

	/**
	 * Moves the carret to the given position.
	 * 
	 * @param offset2
	 *            the offset from the beginning of the text
	 */
	public void goTo(int offset2) {
		go(offset2 - offset);
	}

	@Override
	public String toString() {
		return "Carret [..." + src.subSequence(Math.max(offset - 100, 0), offset) + "~C~" + src.subSequence(offset, Math.min(offset + 100, src.length())) + "]";
	}

}