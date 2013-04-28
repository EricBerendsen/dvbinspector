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

import static nl.digitalekabeltelevisie.util.Utils.*;

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


	// TODO add toHTML() method, which keeps linefeeds
	// should escapeHTML as well, because if we do it later it will break the linefeeds...

	@Override
	public String toString(){
		return getString(data,this.getOffset()+1, this.getLength());
	}

	public String getEncodingString(){

		//getUnsignedByte(data[offset+1])

		final int fb = getUnsignedByte(data[offset+1]);
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

				}else{
					return "Illegal value";
				}
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

}
