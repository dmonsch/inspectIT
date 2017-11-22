package rocks.inspectit.android.instrument.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
public class AndroidManifestParser {
	private static int endDocTag = 0x00100101;
	private static int startTag = 0x00100102;
	private static int endTag = 0x00100103;
	private static int sitOff = 0x24;
	private static String USES_PERMISSION = "uses-permission";

	private static final int UTF8_FLAG = 1 << 8;
	private static final byte[] ZERO_ONE_BYTE_CONST = new byte[] { 0 };

	public static byte[] adjustXml(byte[] org, List<String> neededRights) {
		Pair<String, ManifestPermissionValues> decomp = decompressXML(org);
		System.out.println(decomp.getFirst());
		List<String> addRights = getMissingRights(decomp.getFirst(), neededRights);

		// utf8
		int utf8_flags = LEW(org, 6 * 4);
		boolean utf8 = (utf8_flags & UTF8_FLAG) != 0;

		// copy bytes
		List<Byte> cop = new ArrayList<Byte>(Arrays.asList(ArrayUtils.toObject(org)));

		// get string count and adjust string table
		int numbStrings = LEW(org, 4 * 4);
		byte[] nHead = revLEW(numbStrings + addRights.size());

		// apply head
		injectBytes(cop, nHead, 4 * 4);

		// get highest string table pos
		int xmlTagOffset = LEW(org, 3 * 4); // = end of string table

		// create string resources
		int nStrPos = xmlTagOffset + 4;
		int stOffNew = sitOff + ((numbStrings + addRights.size()) * 4);
		int sitIncSize = addRights.size() * 4;

		int[] a = new int[addRights.size()];

		for (int i = 0; i < addRights.size(); i++) {
			// create string index table value
			int strInd = numbStrings + i;
			a[i] = strInd;
			int strSitPos = sitOff + (strInd * 4);

			// -> this is because we add count of bytes of the sit entry to the str pos at the end
			byte[] nPointer = revLEW((nStrPos + (sitIncSize - (i * 4))) - stOffNew);
			for (int k = 0; k < nPointer.length; k++) {
				cop.add(strSitPos + k, nPointer[k]);
			}

			// create string table value
			byte[] encString = encodeString(addRights.get(i), utf8);
			int strLength = addRights.get(i).length();
			byte[] encLength = encodeStringLength(strLength, utf8);

			// prepare final byte array
			byte[] fullStrByteArray = concat(encLength, encString);
			if ((fullStrByteArray.length % 4) != 0) {
				// add null bytes
				int toAdd = 4 - (fullStrByteArray.length % 4);
				byte[] tZeroBytes = new byte[toAdd];
				for (int k = 0; k < toAdd; k++) {
					tZeroBytes[k] = 0;
				}

				fullStrByteArray = concat(fullStrByteArray, tZeroBytes);
			}

			// inject the byte array
			for (int k = 0; k < fullStrByteArray.length; k++) {
				cop.add(nStrPos + k + nPointer.length, fullStrByteArray[k]);
			}

			nStrPos += fullStrByteArray.length + nPointer.length;
		}

		// adjust the xml tag off header
		injectBytes(cop, revLEW(nStrPos - 4), 3 * 4);
		injectBytes(cop, revLEW(LEW(org, 7 * 4) + sitIncSize), 7 * 4);

		// add the new xml elements
		int xmlTreeInjectionPos = decomp.getSecond().getInjectionPoint() + (nStrPos - (xmlTagOffset + 4));
		for (int k = 0; k < addRights.size(); k++) {
			byte[] tagStart = revLEW(startTag);
			byte[] tagEnd = revLEW(endTag);
			byte[] tagSi = revLEW(getStringIndex(org, USES_PERMISSION, utf8));

			byte[] chunkSizeConst = revLEW(56);
			byte[] mOneConst = revLEW(-1); // namespace and const ref
			byte[] lineNumber = revLEW(decomp.getSecond().getLineNumber());

			byte[] zeroConst = revLEW(0);
			byte[] stringDatatypeByte = new byte[] { 3 };
			byte[] numberAttrs = revLEW(1);
			byte[] attrConst = revShort((short) 20);
			byte[] attrNameSi = revLEW(getStringIndex(org, "name", utf8));
			byte[] attrValueSi = revLEW(numbStrings + k);
			byte[] attrNs = revLEW(decomp.getSecond().getAttrNs());

			// create start tag
			byte[] stb = concat(tagStart, chunkSizeConst, lineNumber, mOneConst, mOneConst, tagSi, attrConst, attrConst, numberAttrs, zeroConst, attrNs, attrNameSi, attrValueSi, ZERO_ONE_BYTE_CONST,
					ZERO_ONE_BYTE_CONST, ZERO_ONE_BYTE_CONST, stringDatatypeByte, attrValueSi);

			byte[] ste = concat(tagEnd, chunkSizeConst, lineNumber, mOneConst, mOneConst, tagSi);

			for (int z = 0; z < stb.length; z++) {
				cop.add(xmlTreeInjectionPos + z, stb[z]);
			}
			for (int z = 0; z < ste.length; z++) {
				cop.add(xmlTreeInjectionPos + z + stb.length, ste[z]);
			}

			xmlTreeInjectionPos += stb.length + ste.length;
		}

		// adjust size header
		byte[] xmlSizeEnc = revLEW(cop.size());
		injectBytes(cop, xmlSizeEnc, 4);

		// convert to byte array
		Byte[] bytes = cop.toArray(new Byte[cop.size()]);
		System.out.println(decompressXML(ArrayUtils.toPrimitive(bytes)).getFirst());
		return ArrayUtils.toPrimitive(bytes);
	}

	private static Pair<String, ManifestPermissionValues> decompressXML(byte[] xml) {
		String result = "";

		int numbStrings = LEW(xml, 4 * 4);

		int stOff = sitOff + (numbStrings * 4);

		int utf8_flags = LEW(xml, 6 * 4);
		boolean utf8 = (utf8_flags & UTF8_FLAG) != 0;

		int xmlTagOff = scanForwardTill(xml, LEW(xml, 3 * 4), startTag);

		// Step through the XML tree element tags and attributes
		ManifestPermissionValues mpvalues = new ManifestPermissionValues();

		int off = xmlTagOff;
		while (off < xml.length) {
			int tag0 = LEW(xml, off);
			// int tag3 = LEW(xml, off+3*4);
			int nameSi = LEW(xml, off + (5 * 4));

			if (tag0 == startTag) { // XML START TAG
				int numbAttrs = LEW(xml, off + (7 * 4)); // Number of Attributes to follow
				// int tag8 = LEW(xml, off+8*4); // Expected to be 00000000

				int ooff = off; // hold old offset

				off += 9 * 4; // Skip over 6+3 words of startTag data
				String name = compXmlString(xml, sitOff, stOff, nameSi, utf8);

				if (name.equals(USES_PERMISSION)) {
					mpvalues.setInjectionPoint(ooff);
					mpvalues.setLineNumber(LEW(xml, ooff + 8));
				}

				// Look for the Attributes
				StringBuffer sb = new StringBuffer();
				for (int ii = 0; ii < numbAttrs; ii++) {
					if (name.equals(USES_PERMISSION)) {
						mpvalues.setAttrNs(LEW(xml, off));
					}
					int attrNameSi = LEW(xml, off + (1 * 4)); // AttrName String Index
					int attrValueSi = LEW(xml, off + (2 * 4)); // AttrValue Str Ind, or FFFFFFFF
					int attrResId = LEW(xml, off + (4 * 4)); // AttrValue ResourceId or dup
					// AttrValue StrInd
					off += 5 * 4; // Skip over the 5 words of an attribute

					String attrName = compXmlString(xml, sitOff, stOff, attrNameSi, utf8);
					String attrValue = attrValueSi != -1 ? compXmlString(xml, sitOff, stOff, attrValueSi, utf8) : "resourceID 0x" + Integer.toHexString(attrResId);
					sb.append(" " + attrName + "=\"" + attrValue + "\"");
					// tr.add(attrName, attrValue);
				}
				result += "<" + name + sb + ">";

			} else if (tag0 == endTag) { // XML END TAG
				off += 6 * 4; // Skip over 6 words of endTag data
				String name = compXmlString(xml, sitOff, stOff, nameSi, utf8);
				result += "</" + name + ">";
			} else if (tag0 == endDocTag) { // END OF XML DOC TAG
				break;
			} else {
				break;
			}
		}

		return new Pair<>(result, mpvalues);
	}

	private static List<String> getMissingRights(String xml, List<String> neededRights) {
		List<String> misses = new ArrayList<String>();

		Set<String> exRights = new HashSet<String>();

		// PARSING XML
		Document dom;
		// Make an instance of the DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();

			dom = db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}

		Node manifestNode = dom.getElementsByTagName("manifest").item(0);
		// this.packageName = manifestNode.getAttributes().getNamedItem("package").getTextContent();
		NodeList permissionNodes = manifestNode.getChildNodes();

		for (int k = 0; k < permissionNodes.getLength(); k++) {
			final Node permNode = permissionNodes.item(k);
			if (permNode.getNodeName().equals("uses-permission")) {
				Node name = permNode.getAttributes().getNamedItem("android:name");
				if (name == null) {
					name = permNode.getAttributes().getNamedItem("name");
				}
				exRights.add(name.getTextContent());
			}
		}

		for (String r : neededRights) {
			if (!exRights.contains(r)) {
				misses.add(r);
			}
		}

		return misses;
	}

	private static int getStringIndex(byte[] xml, String text, boolean utf8) {
		int numbStrings = LEW(xml, 4 * 4);

		for (int ind = 0; ind < numbStrings; ind++) {
			if (compXmlString(xml, sitOff, sitOff + (numbStrings * 4), ind, utf8).equals(text)) {
				return ind;
			}
		}

		return -1;
	}

	private static int scanForwardTill(byte[] xml, int start, int tag) {
		for (int ii = start; ii < (xml.length - 4); ii += 4) {
			if (LEW(xml, ii) == startTag) {
				start = ii;
				break;
			} else if (LEW(xml, ii) == 16842754) {
				// System.out.println(LEW(xml, start + 8));
				// System.out.println(LEWS(xml, start + 8));
			}
		}
		return start;
	}

	private static String compXmlString(byte[] xml, int sitOff, int stOff, int strInd, boolean utf8) {
		if (strInd < 0) {
			return null;
		}
		int strOff = stOff + LEW(xml, sitOff + (strInd * 4));
		return compXmlStringAt(xml, strOff, utf8);
	}

	private static String compXmlStringAt(byte[] arr, int strOff, boolean utf8) {
		if (!utf8) {
			return compXmlStringAt(arr, strOff);
		} else {
			return compXmlStringAtUtf8(arr, strOff);
		}
	}

	private static byte[] encodeString(String str, boolean utf8) {
		if (utf8) {
			return str.getBytes(Charset.forName("UTF-8"));
		} else {
			return Charset.forName("UTF-16LE").encode(str).array();
		}
	}

	private static String compXmlStringAtUtf8(byte[] arr, int strOff) {
		int[] strLen = getStrLen(arr, strOff, true);

		byte[] chars = new byte[strLen[1]];
		for (int i = 0; i < strLen[1]; i++) {
			chars[i] = arr[strOff + strLen[0] + i];
		}
		return new String(chars);
	}

	private static String compXmlStringAt(byte[] arr, int strOff) {
		int[] strLen = getStrLen(arr, strOff, false);
		ByteBuffer chars = ByteBuffer.allocate(strLen[1] * 2);
		for (int ii = 0; ii < strLen[1]; ii++) {
			chars.put(arr[strOff + strLen[0] + (ii * 2)]);
			chars.put(arr[strOff + strLen[0] + (ii * 2) + 1]);
		}
		chars.position(0);
		return Charset.forName("UTF-16LE").decode(chars).toString();
	}

	private static int[] getStrLen(byte[] arr, int strOff, boolean utf8) {
		int[] r = new int[2];
		int o = 0;

		if (utf8) {
			if (((arr[strOff + o]) & 0x80) != 0) {
				o = 2;
			} else {
				o = 1;
			}

			int l = (short) (arr[strOff + o] & 0xff);
			o++;
			if ((l & 0x80) != 0) {
				int k = 0;
				k |= (l & 0x7f) << 7;
				k += (short) (arr[strOff + 1] & 0xff);
				l = k;

				o++;
			}
			r[0] = o; // offset
			r[1] = l; // length

			return r;
		} else {
			o = 2;
			int first = LEWS(arr, strOff);
			int len = 0;

			if ((first & 0x8000) != 0) {
				// read another one
				len |= (first & 0x7fff) << 15;
				len += LEWS(arr, strOff + o);
				o += 2;
			} else {
				len = first;
			}

			r[0] = o;
			r[1] = len;
			return r;
		}
	}

	private static byte[] encodeStringLength(int length, boolean utf8) {
		if (utf8) {
			return concat(encodeUtf8(length * 2), encodeUtf8(length));
		} else {
			byte[] ret = new byte[2];

			ret[0] |= length & 0xff;
			ret[1] |= (length & 0xff00) >> 8;
			if ((LEWS(ret, 0) & 0x8000) != 0) {
				// TODO ==> but this is not necessary
			}
			return ret;
		}
	}

	private static byte[] encodeUtf8(int l) {
		byte temp = 0;
		if (l >= 128) {
			byte high = 0;
			high |= (l >> 8) & 0x7F;
			byte low = 0;
			low |= l & 0xFF;

			return new byte[] { high, low };
		} else {
			temp |= l;
			return new byte[] { temp };
		}
	}

	private static void injectBytes(List<Byte> org, byte[] inj, int pos) {
		for (int i = 0; i < inj.length; i++) {
			org.set(pos + i, inj[i]);
		}
	}

	private static int LEW(byte[] arr, int off) {
		return ((arr[off + 3] << 24) & 0xff000000) | ((arr[off + 2] << 16) & 0xff0000) | ((arr[off + 1] << 8) & 0xff00) | (arr[off] & 0xFF);
	}

	private static int LEWS(byte[] arr, int off) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(arr[off]);
		bb.put(arr[off + 1]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.position(0);

		return bb.getShort() & 0xffff; // makes it unsigned
	}

	private static byte[] concat(byte[]... b) {
		int s = 0;
		for (byte[] ba : b) {
			s += ba.length;
		}
		byte[] ret = new byte[s];

		int z = 0;
		for (byte[] ba : b) {
			System.arraycopy(ba, 0, ret, z, ba.length);
			z += ba.length;
		}

		return ret;
	}

	private static byte[] revLEW(int val) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(val);

		return bb.array();
	}

	private static byte[] revShort(short s) {
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putShort(s);

		return bb.array();
	}
}
