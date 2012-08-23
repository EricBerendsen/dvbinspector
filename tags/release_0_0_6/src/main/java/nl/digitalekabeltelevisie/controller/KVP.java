/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * 
 *  This file is part of DVB Inspector.
 * 
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 * 
 */
package nl.digitalekabeltelevisie.controller;

import javax.swing.JMenuItem;

import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.Utils;

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
 * Most simple form is <code>KVP kvp = KVP("label");</code>, this will create a KVP with no data value. Is used as parent for grouping descendants.<br>
 * Normal usage is   <code>KVP kvp = KVP("label", 11);</code>,  this will create a KVP with data of type int, and value 11. When  numberDisplay == NUMBER_DISPLAY_BOTH it will be shown in both decimal and hexadecimal, so the output of toString will be "label: 0xB (11)".<br> 
 * Extended usage is   <code>KVP kvp = KVP("label", 23,"explanation");</code>,  this will create a KVP with data of type int, and value 23. When  numberDisplay == NUMBER_DISPLAY_BOTH it will be shown in both decimal and hexadecimal, so the output of toString will be "label: 0x17 (23) => explanation".<br> 
 * <p> 
 * Data type can be none, or one of String, int, long, byte[], DVBString.
 * <p> 
 * Can also be used to associate some other attributes with the label/value, like a {@link HTMLSource} or a {@link ImageSource}
 * If either one of these fields is not null, it means there is extra data (HTML text and/or image) available for display. In DVB Inspector it is shown in the right panel.   
 * <p>
 * Also there can be a <code>JMenuItem</code> and owner associated with the KVP (always together). 
 * This is used by DVB Inspector to show a sub menu for some items, and associate it with a handler (owner)
 *  
 * 
 * @author Eric Berendsen
 *
 */
public class KVP{


	/**
	 * maximum length of byte[] to be shown in JTree, only has meaning in case data is array of byte.
	 */
	private static final int BYTE_DATA_MAX_LEN = 100;
	private String		label;
	private String		value;
	private String		description;

	private int			intValue;
	private long		longValue;
	private byte[]		byteValue;
	private int			byteStart					= 0;
	private int			byteLen						= 0;
	private DVBString	dvbStringValue;

	private static byte	numberDisplay				= 1;	// 1 - decimal, 2 -
	// hex, 3 both
	// ,example "0xA0
	// (160)"
	public static final byte	NUMBER_DISPLAY_DECIMAL		= 1;
	public static final byte	NUMBER_DISPLAY_HEX			= 2;
	public static final byte	NUMBER_DISPLAY_BOTH			= 3;

	private static byte	stringDisplay				= 1;	
	// 1 - plain, 
	// 2 - javascript escaped (quotes removed), 
	// 3 - HTML fragments '<' and '&' escaped, 
	// 4 - AWT HTML (html segments include <html> tag,  otherwise plain text
	public static final byte	STRING_DISPLAY_PLAIN		= 1;
	public static final byte	STRING_DISPLAY_JAVASCRIPT	= 2;
	public static final byte	STRING_DISPLAY_HTML_FRAGMENTS	= 3;
	public static final byte	STRING_DISPLAY_HTML_AWT		= 4;

	private byte		fieldType					= 1;	// 1 - string, 2
	// byte[], 3 int, 4
	// long,

	public static final byte	FIELD_TYPE_STRING			= 1;
	public static final byte	FIELD_TYPE_BYTES			= 2;
	public static final byte	FIELD_TYPE_INT				= 3;
	public static final byte	FIELD_TYPE_LONG				= 4;
	/**
	 * used for a node that has no value associated with it
	 */
	public static final byte	FIELD_TYPE_LABEL			= 5;
	public static final byte	FIELD_TYPE_DVBSTRING		= 6;
	/**
	 * 	used for a node that has no separate value associated with , but a HTML fragment as value  for presentation where possible, has to have a plain text alternative
	 * 	DO NOT confuse this with the KVP having a htmlSource, that is completely unrelated. 
	 *  The FIELD_TYPE_HTML is used for presentation in the JTREE, and serves as enhancement to the plain String label. 
	 *  For example, when presenting teletext lines, the label can show the plain ascii version of the line, while the value can have the same line as HTML fragment with colors
	 * 	both label and value should have the same semantics, only one of them will be presented.
 
	 */
	public static final byte	FIELD_TYPE_HTML				= 7; 
	


	/**
	 * if imageSource is set, means there is a image to display with this KVP. In DVB Inspector is will be shown in the right panel. (example, teletext page, or DVB subtitle)
	 */
	private ImageSource imageSource;
	/**
	 * if htmlSource is set, means there is a html fragment to display with this KVP. In DVB Inspector is will be shown in the right panel. (example, hex dump of byte[]. or EPG)
	 */
	private HTMLSource 	htmlSource;

//	public HTMLSource getHtmlSource() {
//		return htmlSource;
//	}

	public void setHtmlSource(final HTMLSource htmlSource) {
		this.htmlSource = htmlSource;
	}

	private JMenuItem subMenu;
	private Object owner;

	public KVP(final String label) {
		super();
		this.label = label;
		this.fieldType = FIELD_TYPE_LABEL;
	}

	public KVP(final String label,final ImageSource im) {
		super();
		this.label = label;
		this.fieldType = FIELD_TYPE_LABEL;
		this.imageSource = im;
	}
	public KVP(final String label, final String value, final String description) {
		super();
		this.label = label;
		if(value!=null){
			this.value = value;
			this.description = description;
			this.fieldType = FIELD_TYPE_STRING;
		}else{ // just a label
			this.fieldType = FIELD_TYPE_LABEL;
		}
	}

	public KVP(final String label, final int value, final String description) {
		super();
		this.label = label;
		this.intValue = value;
		this.description = description;
		this.fieldType = FIELD_TYPE_INT;
	}

	public KVP(final String label, final long value, final String description) {
		super();
		this.label = label;
		this.longValue = value;
		this.description = description;
		this.fieldType = FIELD_TYPE_LONG;
	}

	public KVP(final String html, final String label) {
		super();
		this.value = html;
		this.label = label; // text representation of the HTML string
		this.fieldType = FIELD_TYPE_HTML;
	}



	/**
	 * @param label
	 * @param value
	 * @param description
	 */
	public KVP(final String label, final byte[] value, final String description) {
		super();
		this.label = label;
		this.byteValue = value;
		this.byteStart = 0;
		//this.byteLen = Math.min(value.length,BYTE_DATA_MAX_LEN);
		this.byteLen = value.length;
		this.description = description;
		this.fieldType = FIELD_TYPE_BYTES;
	}

	public KVP(final String label, final byte[] value, final int offset, final int len, final String description) {
		super();
		this.label = label;
		this.byteValue = value;
		this.byteStart = offset;
		//this.byteLen = Math.min(len,BYTE_DATA_MAX_LEN);
		this.byteLen = len;
		this.description = description;
		this.fieldType = FIELD_TYPE_BYTES;
	}

	public KVP(final String label, final DVBString value, final String description) {
		super();
		this.label = label;
		this.dvbStringValue = value;
		this.description = description;
		this.fieldType = FIELD_TYPE_DVBSTRING;
	}

	public KVP(final String string, final HTMLSource htmlSource) {
		super();
		this.label = string;
		this.fieldType = FIELD_TYPE_LABEL;
		this.htmlSource = htmlSource;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void appendLabel(final String labelAppend) {
		this.label = label + labelAppend;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return toString(stringDisplay,numberDisplay);
	}

	public String toString(final byte stringFormat, final byte numberFormat) {
		StringBuilder b = new StringBuilder(label);

		if ((fieldType != FIELD_TYPE_LABEL)&&(fieldType != FIELD_TYPE_HTML)) {
			b.append(": ");
			if (fieldType == FIELD_TYPE_STRING) {
				b.append(value);
			} else if (fieldType == FIELD_TYPE_INT) {
				if (numberFormat == NUMBER_DISPLAY_DECIMAL) {
					b.append(intValue);
				} else if (numberFormat == NUMBER_DISPLAY_HEX) {
					b.append("0x").append(Integer.toHexString(intValue).toUpperCase());
				} else { // assume both to be safe
					b.append("0x").append(Integer.toHexString(intValue).toUpperCase()).append(" (").append(intValue)
					.append(")");
				}
			} else if (fieldType == FIELD_TYPE_LONG) {
				if (numberFormat == NUMBER_DISPLAY_DECIMAL) {
					b.append(longValue);
				} else if (numberFormat == NUMBER_DISPLAY_HEX) {
					b.append("0x").append(Long.toHexString(longValue).toUpperCase());
				} else { // assume both to be safe
					b.append("0x").append(Long.toHexString(longValue).toUpperCase()).append(" (").append(longValue)
					.append(")");
				}
			} else if (fieldType == FIELD_TYPE_BYTES) {
				if (byteLen == 0) {
					b.append('-');

				} else {
					final int showLen=Math.min(byteLen,BYTE_DATA_MAX_LEN);
					b.append("0x").append(Utils.toHexString(byteValue, byteStart, showLen)).append(" \"").append(
							Utils.toSafeString(byteValue, byteStart, showLen)).append("\"");
				}
			} else if (fieldType == FIELD_TYPE_DVBSTRING) {// TODO make distinction between plain text, and HTML view,
				// to support character emphasis on A.1 Control codes ETSI EN 300 468 V1.11.1 (2010-04) 
				
				b.append(dvbStringValue.toString());
			}
			if (description != null) {
				b.append(" => ").append(description);
			}
		}
		if(fieldType==FIELD_TYPE_HTML){
			if(stringFormat==STRING_DISPLAY_HTML_AWT){
				b = new StringBuilder("<html>").append(value).append("</html>");
			}else if(stringFormat==STRING_DISPLAY_HTML_FRAGMENTS){
				b = new StringBuilder(value);
			}
		}
		if (stringFormat == STRING_DISPLAY_JAVASCRIPT) {
			return  b.toString().replace("\"", "\\\"").replace("\'", "\\\'");
		} else {
			return b.toString();
		}
	}

	public static byte getNumberDisplay() {
		return numberDisplay;
	}

	public static void setNumberDisplay(final byte intDiplay) {
		KVP.numberDisplay = intDiplay;
	}

	public static String formatInt(final int intValue) {
		if (numberDisplay == NUMBER_DISPLAY_DECIMAL) {
			return Integer.toString(intValue);
		} else if (numberDisplay == NUMBER_DISPLAY_HEX) {
			return ("0x") + Integer.toHexString(intValue).toUpperCase();
		} else { // assume both to be safe
			return ("0x" + Integer.toHexString(intValue).toUpperCase()) + " (" + Integer.toString(intValue) + (")");
		}
	}

	public static byte getStringDisplay() {
		return stringDisplay;
	}

	public static void setStringDisplay(final byte stringDisplay) {
		KVP.stringDisplay = stringDisplay;
	}


	/**
	 * @return the imageSource
	 */
	public ImageSource getImageSource() {
		return imageSource;
	}


	/**
	 * @param imageSource the imageSource to set
	 */
	public void setImageSource(final ImageSource imageSource) {
		this.imageSource = imageSource;
	}

	public String getPlainText(){
		return toString(STRING_DISPLAY_PLAIN, NUMBER_DISPLAY_BOTH);
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
	public void setSubMenuAndOwner(final JMenuItem subMenu,final Object owner) {
		this.subMenu = subMenu;
		this.owner = owner;
	}


	/**
	 * @return the owner
	 */
	public Object getOwner() {
		return owner;
	}



	public HTMLSource getHTMLSource(){
		if(fieldType == FIELD_TYPE_BYTES){
			return new HTMLSource() {
				public String getHTML() {
					return Utils.getHTML(byteValue, byteStart, byteLen);
				}
			};

		}else{
			return htmlSource;
		}
	}
}
