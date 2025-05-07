/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * <p>
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * <p>
 *  This file is part of DVB Inspector.
 * <p>
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 * <p>
 * Change log:
 * - Feb 8th 2022: Added constructor supporting boolean value
 */
package nl.digitalekabeltelevisie.controller;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.gui.*;

/**
 * Holder for names/values (and explanations) that takes care of formatting and presentation.
 * Main use is as userObject for DefaultMutableTreeNode, where it's toString method determines what is presented in the tree.
 * Started out as KeyValuePair, as soon as the third argument explanation was added the name was obsolete.
 * Now (ab)used for everything related to presentation.
 * <p>
 * Presentation is controlled by the static variables numberDisplay and stringDisplay. So all field in the same JVM will be presented in the same way (May be a problem if a application with GUI is also used to generate HTML files, using the same KVP mechanism).
 * <p>
 * For usage in JTree the method toString returns the presentation string.
 * <p>
 * Most simple form is {@code KVP kvp = KVP("label");}, this will create a KVP with no data value. Is used as parent for grouping descendants.<br>
 * Normal usage is   {@code KVP kvp = KVP("label", 11);},  this will create a KVP with data of type int, and value 11. When  numberDisplay == NUMBER_DISPLAY_BOTH it will be shown in both decimal and hexadecimal, so the output of toString will be "label: 0xB (11)".<br>
 * Extended usage is   {@code KVP kvp = KVP("label", 23,"explanation");},  this will create a KVP with data of type int, and value 23. When  numberDisplay == NUMBER_DISPLAY_BOTH it will be shown in both decimal and hexadecimal, so the output of toString will be "label: 0x17 (23) => explanation".<br>
 * <p>
 * Data type can be none, or one of String, int, long, byte[], DVBString.
 * <p>
 * Can also be used to associate some other attributes with the label/value, like a {@link HTMLSource} or a {@link ImageSource}
 * If either one of these fields is not null, it means there is extra data (HTML text and/or image) available for display. In DVB Inspector it is shown in the right panel.
 * <p>
 * Also there can be a {@code JMenuItem} and owner associated with the KVP (always together).
 * This is used by DVB Inspector to show a sub menu for some items, and associate it with a handler (owner)
 *
 *
 * @author Eric Berendsen
 *
 */
public class KVP extends DefaultMutableTreeNode{


	public record DetailView(DetailSource detailSource, String label) {}

	public enum NUMBER_DISPLAY {
		DECIMAL,
		HEX,
		BOTH
	}

	public enum STRING_DISPLAY{
		PLAIN, // plain
		JAVASCRIPT, // javascript escaped (quotes removed)
		HTML_FRAGMENTS, // HTML fragments '<' and '&' escaped,
		HTML_AWT // AWT HTML (html segments include <html> tag,  otherwise plain text
	}

	public enum FIELD_TYPE {
		STRING,
		BYTES,
		INT,
		LONG,

		LABEL, //used for a node that has no value associated with it
		DVBSTRING,

		BIGINT
	}

	/**
	 * maximum length of byte[] to be shown in JTree, only has meaning in case data is array of byte.
	 */
	private static final int BYTE_DATA_MAX_LEN = 100;
	private String		label;
	private String		stringValue;
	private String		description;

	private int			intValue;
	private long		longValue;
	private byte[]		byteValue;
	private int			byteStart					= 0;
	private int			byteLen						= 0;
	private DVBString	dvbStringValue;
	private BigInteger 	bigIntegerValue;

	private static NUMBER_DISPLAY	numberDisplay				= NUMBER_DISPLAY.DECIMAL;	// 1 - decimal, 2 -
	private static STRING_DISPLAY	stringDisplay				= STRING_DISPLAY.PLAIN;

	private FIELD_TYPE		fieldType;

    private List<DetailView> detailViews = new ArrayList<>();

	/**
	 * crumb's are used to be able to jump to any place in the tree, based on a url-like
	 * string. The crumb for each node defaults to its label, with any int or long value 
	 * appended to it separated by a ':'. 
	 * <p>
	 * This is called a trail of crumbs te prevent confusion with the Path used in JTree. 
	 * That is not text based, so can not be used in creating hyperlinks in HTMLEditor
	 */
	private String crumb;

	private JMenuItem subMenu;
	private Object owner;
	private String labelAppend = "";
	private String htmlLabel;

	public KVP(String label) {
        this.label = label;
		this.fieldType = FIELD_TYPE.LABEL;
	}

	public KVP(String label, String stringValue) {
        this.label = label;
        this.stringValue = stringValue;
		this.fieldType = FIELD_TYPE.STRING;

	}

	public KVP(String label, String stringValue, String description) {
		this(label,stringValue);
		setDescription(description);
 
	}
	
	public KVP(String label, int intValue) {
        this.label = label;
		this.intValue = intValue;
		this.fieldType = FIELD_TYPE.INT;
	}

	public KVP(String label, int intValue, String description) {
        this(label,intValue);
		setDescription(description);
	}

	public KVP(String label, long longValue) {
        this.label = label;
		this.longValue = longValue;
		this.fieldType = FIELD_TYPE.LONG;
	}

	public KVP(String label, long longValue, String description) {
        this(label,longValue);
		setDescription(description);
	}

	public KVP(String label, byte[] byteArray) {
		this.label = label;
		this.byteValue = byteArray;
		this.byteStart = 0;
		this.byteLen = byteArray.length;
		this.fieldType = FIELD_TYPE.BYTES;
		addHTMLSource(() -> getHTMLHexview(byteValue, byteStart, byteLen), "Hex View");
	}

	public KVP(String label, byte[] byteArray, String description) {
		this(label, byteArray);
		setDescription(description);
	}

	public KVP(String label, byte[] byteArray, int offset, int len) {
		this.label = label;
		this.byteValue = byteArray;
		this.byteStart = offset;
		this.byteLen = len;
		this.fieldType = FIELD_TYPE.BYTES;
		addHTMLSource(() -> getHTMLHexview(byteValue, byteStart, byteLen), "Hex View");
	}

	public KVP(String label, byte[] byteArray, int offset, int len, String description) {
		this(label, byteArray, offset, len);
		setDescription(description);
	}

	public KVP(String label, DVBString dvbStringValue) {
        this.label = label;
		this.dvbStringValue = dvbStringValue;
		this.fieldType = FIELD_TYPE.DVBSTRING;
		this.add(new KVP("encoding", dvbStringValue.getEncodingString()));
		this.add(new KVP("length", dvbStringValue.getLength()));
		this.addHTMLSource(
				() ->"<b>Encoding:</b> " + dvbStringValue.getEncodingString() 
						+ "<br><br><b>Data:</b><br>" + getHTMLHexview(dvbStringValue.getData(), dvbStringValue.getOffset() + 1, dvbStringValue.getLength())
						+ "<br><b>Formatted:</b><br>" + dvbStringValue.toEscapedHTML(),
				"DVB String");
	}

	
	public KVP(String label, BigInteger bigIntegerValue) {

        this.label = label;
		this.bigIntegerValue = bigIntegerValue;
		this.fieldType = FIELD_TYPE.BIGINT;
	}

	public KVP(String label, BigInteger bigIntegerValue, String description) {
        this(label, bigIntegerValue);
		setDescription(description);
	}
	
	
	public KVP add(String label, int intValue) {
		add(new KVP(label, intValue));
		return this;
	}
	
	public String getDescription() {
		return description;
	}

	public KVP setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getLabel() {
		return label;
	}

	public KVP setLabel(String label) {
		this.label = label;
		return this;
	}

	// put appends in separate String, so original label is constant and available for path
	public void appendLabel(String labelAppend) {
		this.labelAppend  = this.labelAppend + labelAppend;
	}


	@Override
	public String toString() {
		return toString(stringDisplay,numberDisplay);
	}

	public String toString(STRING_DISPLAY stringFormat, NUMBER_DISPLAY numberFormat) {
		StringBuilder b = new StringBuilder();
		if ((htmlLabel != null) && (STRING_DISPLAY.PLAIN != stringFormat)) {
			b.append(htmlLabel);
		} else {
			b.append(label);
		}
		if(!labelAppend.isEmpty()) {
			b.append(labelAppend);
		}

		if (fieldType != FIELD_TYPE.LABEL) {
			appendValueAfterLabel(numberFormat, b);
		}
		if (stringFormat == STRING_DISPLAY.JAVASCRIPT) {
			return b.toString().replace("\"", "\\\"").replace("\'", "\\\'");
		}
		if ((htmlLabel != null) && (stringFormat == STRING_DISPLAY.HTML_AWT)) {
			return new StringBuilder("<html>").append(b).append("</html>").toString();
		}
		return b.toString();
	}


	/**
	 * @param numberFormat
	 * @param b
	 */
	private void appendValueAfterLabel(NUMBER_DISPLAY numberFormat, StringBuilder b) {
		b.append(": ");
		switch (fieldType){
			case STRING:
				appendString(b);
				break;
			case INT:
				appendInteger(numberFormat, b);
				break;
			case LONG:
				appendLong(numberFormat, b);
				break;
			case BYTES:
				appendHexBytes(b);
				break;
			case DVBSTRING:
				appendDVBString(b);
				break;
			case BIGINT:
			appendBigInteger(numberFormat, b);	
		}
		if (description != null) {
			b.append(" => ").append(description);
		}
	}

	/**
	 * @param b
	 */
	private void appendDVBString(StringBuilder b) {
		// TODO make distinction between plain text, and HTML view,
		// to support character emphasis on A.1 Control codes ETSI EN 300 468 V1.11.1 (2010-04)
		b.append(dvbStringValue.toString());
	}

	/**
	 * @param b
	 */
	private void appendHexBytes(StringBuilder b) {
		if (byteLen == 0) {
			b.append('-');

		} else {
			if(byteLen > BYTE_DATA_MAX_LEN) {
				b.append("[truncated] ");
			}
			int showLen=Math.min(byteLen,BYTE_DATA_MAX_LEN);
			b.append("0x").append(toHexString(byteValue, byteStart, showLen)).append(" \"").append(
					toSafeString(byteValue, byteStart, showLen)).append("\"");
		}
	}

	/**
	 * @param b
	 */
	private void appendString(StringBuilder b) {
		b.append(stringValue);
	}

	/**
	 * @param numberFormat
	 * @param b
	 */
	private void appendLong(NUMBER_DISPLAY numberFormat, StringBuilder b) {
		if (numberFormat == NUMBER_DISPLAY.DECIMAL) {
			b.append(longValue);
		} else if (numberFormat == NUMBER_DISPLAY.HEX) {
			b.append("0x").append(Long.toHexString(longValue).toUpperCase());
		} else { // assume both to be safe
			b.append("0x").append(Long.toHexString(longValue).toUpperCase()).append(" (").append(longValue)
			.append(")");
		}
	}

	/**
	 * @param numberFormat
	 * @param b
	 */
	private void appendInteger(NUMBER_DISPLAY numberFormat, StringBuilder b) {
		if (numberFormat == NUMBER_DISPLAY.DECIMAL) {
			b.append(intValue);
		} else if (numberFormat == NUMBER_DISPLAY.HEX) {
			b.append("0x").append(Integer.toHexString(intValue).toUpperCase());
		} else { // assume both to be safe
			b.append(getHexAndDecimalFormattedString(intValue));
		}
	}

	private void appendBigInteger(NUMBER_DISPLAY numberFormat, StringBuilder b) {
		if (numberFormat == NUMBER_DISPLAY.DECIMAL) {
			b.append(bigIntegerValue.toString());
		} else if (numberFormat == NUMBER_DISPLAY.HEX) {
			b.append("0x").append(bigIntegerValue.toString(16).toUpperCase());
		} else { // assume both to be safe
			b.append(getHexAndDecimalFormattedString(bigIntegerValue));
		}
	}

	
	public static NUMBER_DISPLAY getNumberDisplay() {
		return numberDisplay;
	}

	public static void setNumberDisplay(NUMBER_DISPLAY intDisplay) {
		numberDisplay = intDisplay;
	}

	public static String formatInt(int intValue) {
		if (numberDisplay == NUMBER_DISPLAY.DECIMAL) {
			return Integer.toString(intValue);
		} else if (numberDisplay == NUMBER_DISPLAY.HEX) {
			return ("0x") + Integer.toHexString(intValue).toUpperCase();
		} else { // assume both to be safe
			return ("0x" + Integer.toHexString(intValue).toUpperCase()) + " (" + intValue + (")");
		}
	}

	public static STRING_DISPLAY getStringDisplay() {
		return stringDisplay;
	}

	public static void setStringDisplay(STRING_DISPLAY stringDisplay) {
		KVP.stringDisplay = stringDisplay;
	}

	public String getPlainText(){
		return toString(STRING_DISPLAY.PLAIN, NUMBER_DISPLAY.BOTH);
	}


	/**
	 * @return the subMenu
	 */
	public JMenuItem getSubMenu() {
		return subMenu;
	}


	/**
	 * @param subMenu the subMenu to set
	 * @param owner the owner to set
	 */
	public KVP setSubMenuAndOwner(JMenuItem subMenu, Object owner) {
		this.subMenu = subMenu;
		this.owner = owner;
		return this;
	}

	/**
	 * @param subMenu the subMenu to set
	 */
	public KVP setSubMenu(JMenuItem subMenu) {
		this.subMenu = subMenu;
		return this;
	}

	/**
	 * @return the owner
	 */
	public Object getOwner() {
		return owner;
	}


	public boolean  isBytes(){
		return fieldType == FIELD_TYPE.BYTES;
	}


	public byte[] getByteValue() {
		if(fieldType == FIELD_TYPE.BYTES){
			return copyOfRange(byteValue, byteStart, byteStart+byteLen);
		}
		
		return new byte[0];
	}


	public String getCrumb() {
		if(crumb!=null) {
			return crumb;
		}

		return switch (fieldType){
			case LABEL -> label;
			case INT -> label+":"+intValue;
			case LONG -> label+":"+longValue;
			default -> null;
		};

	}
	
	

	public KVP setCrumb(String path) {
		this.crumb = path;
		return this;
	}

	public List<DetailView> getDetailViews() {
		return detailViews;
	}
	
	public KVP addHTMLSource(HTMLSource htmlSource, String label) {
		detailViews.add(new DetailView(htmlSource, label));
		return this;
	}

	public KVP addImageSource(ImageSource imageSource, String label) {
		detailViews.add(new DetailView(imageSource, label));
		return this;
	}

	public KVP addTableSource(TableSource tableSource, String label) {
		detailViews.add(new DetailView(tableSource, label));
		return this;
	}

	public KVP addXMLSource(XMLSource xmlSource, String label) {
		detailViews.add(new DetailView(xmlSource, label));
		return this;
	}

	@Override
    public Object getUserObject() {
        return this;
    }

	public KVP setHtmlLabel(String htmlLabel) {
		this.htmlLabel = htmlLabel;
		return this;
	}


	
}
