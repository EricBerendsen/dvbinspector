/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getEscapedHTML;
import static nl.digitalekabeltelevisie.util.Utils.getCharDecodedStringWithControls;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getString;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import nl.digitalekabeltelevisie.util.Utils;

/**
 *
 * Representation of an DVB string, where 1th byte indicates length of string, and optional control codes to select a character table.
 *
 * In DVB strings are arrays of bytes. It is not necessary to copy them while constructing objects (save memory), and we don't always know how to use them (GUI or HTML)
 * This is just a reference to the original bytes, let the presentation logic decide what to do with it.
 *
 * first byte of array (data[offset]) always indicates length, so text may start at data[offset+1]
 * data[offset+1] may start with control code to select a character table.
 *
 * @see "ETSI EN 300 468, Annex A (normative):Coding of text characters"
 *
 *
 */
public class DVBString {

	private final byte[]data;
	private final int offset;

	public DVBString(final byte[] data, final int offset) {
		super();
		this.data = data;
		this.offset = offset;
	}

	/**
	 * Create a DVBString where the length is explicitly specified. Most of the usage first byte of data array islength, 
	 * but sometimes (at end of descriptor) this is not needed because it can be infered differently. 
	 * In those cases use this constructor
	 * 
	 * @param dataIn
	 * @param offset
	 * @param len
	 */
	public DVBString(final byte[] dataIn, final int offset, int len) {
		super();
		if(len>255) {
			throw new RuntimeException("DVB String can not be longer than 255 chars:" + len); 
		}
		
		this.data = new byte[len+1];
		this.data[0] = (byte) len;
		this.offset = 0;
		System.arraycopy(dataIn, offset, this.data, 1, len);
	}
	/**
	 * @return HTML representation of this string, including linefeeds  (0x8A) and emphasis (0x86/0x87). Line length not limited
	 *
	 */
	public String toEscapedHTML(){
		return toEscapedHTML(0);
	}

	/**
	 * @param maxWidth maximum width of HTML fragment in chars.
	 * @return HTML representation of this string, including linefeeds  (0x8A) and emphasis (0x86/0x87). Line length max
	 */
	public String toEscapedHTML(final int maxWidth){
		ArrayList<DVBString> array = new ArrayList<>(Arrays.asList(this));
		return getEscapedHTML(array, maxWidth);
	}

	/**
	 * return plain text string representation where control chars have been removed
	 */
	@Override
	public String toString(){
		return getString(data,this.getOffset()+1, this.getLength());
	}
	
	
	/**
	 * @return tring representation where control chars are present
	 */
	public String toRawString() {
		return getCharDecodedStringWithControls(data,this.getOffset()+1, this.getLength());
	}
	
	/**
	 * If DVBString has no explicit charset defined in first byte(s), use parameter charSet as encoding.
	 * 
	 * @param defaultCharSet when null use normal "default (ISO 6937, latin)" encoding 
	 */
	public String toString(Charset defaultCharSet) {
		if((getCharSet()!=null) || (defaultCharSet == null)){
			return toString();
		}
		return new String(data, this.getOffset() + 1, this.getLength(), defaultCharSet);
	}

	public Charset getCharSet(){
		return Utils.getCharSet(data, this.getOffset()+1, this.getLength());
	}

	public String getEncodingString(){

		// empty string has no encoding
		if(getLength()==0){
			return "-";
		}

		final int fb = toUnsignedInt(data[offset+1]);
		if(0x20<=fb)
		{
			return "default (ISO 6937, latin)";
		}else if((0x01<=fb)&&(fb<=0x1F)){
			switch (fb) {
			case 0x01:
				return "ISO/IEC 8859-5";
			case 0x02:
				return "ISO/IEC 8859-6";
			case 0x03:
				return "ISO/IEC 8859-7";
			case 0x04:
				return "ISO/IEC 8859-8";
			case 0x05:
				return "ISO/IEC 8859-9";
			case 0x06:
				return "ISO/IEC 8859-10";
			case 0x07:
				return "ISO/IEC 8859-11";
			case 0x08:
				return "ISO/IEC 8859-12";
			case 0x09:
				return "ISO/IEC 8859-13";
			case 0x0A:
				return "ISO/IEC 8859-14";
			case 0x0B:
				return "ISO/IEC 8859-15";
			case 0x10:
				if(data[offset+2]==0x0){
					return "ISO/IEC 8859-"+data[offset+3];

				}
				return "Illegal value";
			case 0x11:
				return "ISO/IEC 10646-1";
			case 0x12:
				return "KSX1001-2004";
			case 0x13:
				return "GB-2312-1980";
			case 0x14:
				return "Big5 subset of ISO/IEC 10646-1";
			case 0x15:
				return "UTF-8 encoding of ISO/IEC 10646-1";
			case 0x1F:
				return "Described by encoding_type_id;"+data[offset+2];
			default:
				return "reserved for future use";
			}

		}
		return "illegal value";
	}




	public int getLength() {
		return getInt(data, offset, 1, MASK_8BITS);
	}



	public int getOffset() {
		return offset;
	}


	public byte[] getData() {
		return data;
	}

}
