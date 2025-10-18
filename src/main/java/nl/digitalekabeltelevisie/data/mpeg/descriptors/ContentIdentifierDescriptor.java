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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 *
 * Based on ETSI TS 102 323 V1.3.1 Carriage and signalling of TV-Anytime information in DVB transport streams 12.1 Content identifier descriptor
 * @author Eric
 *
 */
public class ContentIdentifierDescriptor extends Descriptor {

	private List<CridEntry> cridEntryList = new ArrayList<>();

	public static record CridEntry(int cridType, int cridLocation, int cridLength, byte[] cridByte, int cridRef) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("CRID");
			s.add(new KVP("crid_type", cridType, getCridTypeString(cridType)));
			s.add(new KVP("crid_location", cridLocation, getCridLocationString(cridLocation)));
			if (cridLocation == 0) {
				s.add(new KVP("crid_length", cridLength));
				s.add(new KVP("crid_byte", cridByte));
			} else if (cridLocation == 1) {
				s.add(new KVP("crid_ref", cridRef));
			}
			return s;
		}

	}

	public ContentIdentifierDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		int r = 0;
		while (r < descriptorLength) {
			byte[] crid_byte = null;
			int crid_len = 0;
			int cridRef = 0;
			final int type = getInt(b, 2 + r, 1, 0xFC) >> 2;
			final int location = getInt(b, 2 + r, 1, Utils.MASK_2BITS);
			if (location == 0) {
				crid_len = getInt(b, 3 + r, 1, Utils.MASK_8BITS);
				crid_byte = copyOfRange(b, 4 + r, r + 4 + crid_len);
				r += 2 + crid_len;
			} else if (location == 1) {
				cridRef = getInt(b, 3 + r, 2, Utils.MASK_16BITS);
				r += 3;
			} else { // location ==2 or 3, not defined, so we don't know how much data to expect.
						// Just break out of loop..

				r += 2;
				break;
			}
			final CridEntry cridEntry = new CridEntry(type, location, crid_len, crid_byte, cridRef);
			cridEntryList.add(cridEntry);

		}
	}

	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		addListJTree(t,cridEntryList,modus,"Crid Entries");
		return t;
	}



	public static String getCridTypeString(int type) {
		switch (type) {
		case 0x00 : return "No type defined";
		case 0x01 : return "CRID references the item of content that this event is an instance of.";
		case 0x02 : return "CRID references a series that this event belongs to.";
		case 0x03 : return "CRID references a recommendation. This CRID can be a group or a single item of content.";

		case 0x31 : return "User private; DTG programme CRID (equivalent to type 0x01); CRID references the item of content that this event is an instance of.";
		case 0x32 : return "User private; DTG series CRID (a restriction of type 0x02 to be used only for series); CRID references a series that this event belongs to.";
		case 0x33 : return "User private; DTG recommendation CRID (equivalent to type 0x03); CRID references a recommendation. This CRID can be a group or a single item of content.";
		default:
			if((0x04<=type)&&(type<=0x1F )){return "DVB reserved";}
			if((0x20<=type)&&(type<=0x3F )){return "User private";}

			return "Illegal value";

		}
	}

	public static String getCridLocationString(int type) {
		return switch (type) {
		case 0x00 -> "Carried explicitly within descriptor";
		case 0x01 -> "Carried in Content Identifier Table (CIT)";
		case 0x02 -> "DVB reserved";
		case 0x03 -> "DVB reserved";
		default -> "Illegal value";
		};
	}


}
