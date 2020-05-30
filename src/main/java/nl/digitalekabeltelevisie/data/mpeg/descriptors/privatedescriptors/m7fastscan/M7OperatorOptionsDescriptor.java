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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class M7OperatorOptionsDescriptor extends M7Descriptor {

	private final int parental_control_rating;
	private final int default_char_set;
	private final int subtitles_enabled;
	private final int special_regions_setup;
	
	public M7OperatorOptionsDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		parental_control_rating =getInt(b, offset+2,1,MASK_8BITS);
		default_char_set =getInt(b, offset+3,1,MASK_8BITS);
		subtitles_enabled =getInt(b, offset+4,1,0x80)>>>7;
		special_regions_setup =getInt(b, offset+4,1,0x40)>>>6;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("parental_control_rating",parental_control_rating ,getParentalControlString())));
		t.add(new DefaultMutableTreeNode(new KVP("default_char_set",default_char_set ,getEncodingTypeString())));
		t.add(new DefaultMutableTreeNode(new KVP("subtitles_enabled",subtitles_enabled ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("special_regions_setup",special_regions_setup ,null)));
		return t;
	}

	public String getEncodingTypeString() {
		return getEncodingType(default_char_set);
	}

	public String getParentalControlString() {
		if(parental_control_rating==0) {
			return "undefined";
		}
		return ""+ (parental_control_rating+3)+" +";
	}
	
	private String getEncodingType(int charSet) {
		switch (charSet) {
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
		default:
			return "reserved for future use";
		}

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
