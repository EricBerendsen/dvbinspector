/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class M7OperatorOptionsDescriptor extends M7Descriptor {

	private final int parental_control_rating;
	private final int default_char_set;
	private final int subtitles_enabled;
	private final int special_regions_setup;
	
	public M7OperatorOptionsDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		parental_control_rating = getInt(b, 2, 1, MASK_8BITS);
		default_char_set = getInt(b, 3, 1, MASK_8BITS);
		subtitles_enabled = getInt(b, 4, 1, 0x80) >>> 7;
		special_regions_setup = getInt(b, 4, 1, 0x40) >>> 6;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("parental_control_rating", parental_control_rating, getParentalControlString()));
		t.add(new KVP("default_char_set", default_char_set, getEncodingTypeString()));
		t.add(new KVP("subtitles_enabled", subtitles_enabled));
		t.add(new KVP("special_regions_setup", special_regions_setup, null));
		return t;
	}

	public String getEncodingTypeString() {
		return getEncodingType(default_char_set);
	}

	public String getParentalControlString() {
		if(parental_control_rating==0) {
			return "undefined";
		}
		return (parental_control_rating+3)+" +";
	}
	
	private static String getEncodingType(int charSet) {
		return switch (charSet) {
		case 0x01 -> "ISO/IEC 8859-5";
		case 0x02 -> "ISO/IEC 8859-6";
		case 0x03 -> "ISO/IEC 8859-7";
		case 0x04 -> "ISO/IEC 8859-8";
		case 0x05 -> "ISO/IEC 8859-9";
		case 0x06 -> "ISO/IEC 8859-10";
		case 0x07 -> "ISO/IEC 8859-11";
		case 0x08 -> "ISO/IEC 8859-12";
		case 0x09 -> "ISO/IEC 8859-13";
		case 0x0A -> "ISO/IEC 8859-14";
		case 0x0B -> "ISO/IEC 8859-15";
		case 0x10 -> "Illegal value";
		case 0x11 -> "ISO/IEC 10646-1";
		case 0x12 -> "KSX1001-2004";
		case 0x13 -> "GB-2312-1980";
		case 0x14 -> "Big5 subset of ISO/IEC 10646-1";
		case 0x15 -> "UTF-8 encoding of ISO/IEC 10646-1";
		default -> "reserved for future use";
		};

	}

	public int getParental_control_rating() {
		return parental_control_rating;
	}

	public int getDefault_char_set() {
		return default_char_set;
	}

	public int getSubtitles_enabled() {
		return subtitles_enabled;
	}

	public int getSpecial_regions_setup() {
		return special_regions_setup;
	}

}
