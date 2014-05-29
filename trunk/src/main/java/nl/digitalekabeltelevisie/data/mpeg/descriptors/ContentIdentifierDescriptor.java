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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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

	private List<CridEntry> cridEntryList = new ArrayList<CridEntry>();

	public static class CridEntry implements TreeNode{

		private final int cridType;
		private final int cridLocation;
		private final int cridLength;
		private final byte[] cridByte;
		private final int cridRef;

		public CridEntry(final int cridType, final int cridLocation, final int cridLength, final byte[] cridByte, final int cridRef) {
			super();
			this.cridType = cridType;
			this.cridLocation = cridLocation;
			this.cridLength = cridLength;
			this.cridByte = cridByte;
			this.cridRef = cridRef;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("CRID"));
			s.add(new DefaultMutableTreeNode(new KVP("crid_type",cridType,getCridTypeString(cridType))));
			s.add(new DefaultMutableTreeNode(new KVP("crid_location",cridLocation,getCridLocationString(cridLocation))));
			if(cridLocation==0){
				s.add(new DefaultMutableTreeNode(new KVP("crid_length",cridLength,null)));
				s.add(new DefaultMutableTreeNode(new KVP("crid_byte",cridByte,null)));

			}else if(cridLocation==1){
				s.add(new DefaultMutableTreeNode(new KVP("crid_ref",cridRef,null)));
			}
			return s;
		}

	}





	public ContentIdentifierDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);

		int r =0;
		while (r<descriptorLength) {
			byte[] crid_byte=null;
			int crid_len=0;
			int cridRef=0;
			final int type = getInt(b,offset+2+r, 1, 0xFC)>>2;
			final int location= getInt(b,offset+2+r, 1, Utils.MASK_2BITS);
			if(location==0){
				crid_len=getInt(b,offset+3+r, 1, Utils.MASK_8BITS);
				crid_byte = Utils.copyOfRange(b, offset+4+r, offset+r+4+crid_len);
				r+=2+crid_len;
			}else if(location==1){
				cridRef=getInt(b,offset+3+r, 2, Utils.MASK_16BITS);
				r+=3;
			}else{ // location ==2 or 3, not defined, so we don't know how much data to expect. Just break out of loop..

				r+=2;
				break;
			}
			final CridEntry cridEntry = new CridEntry(type, location,crid_len,crid_byte,cridRef);
			cridEntryList.add(cridEntry);

		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,cridEntryList,modus,"Crid Entries");
		return t;
	}



	public static String getCridTypeString(final int type) {
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

	public static String getCridLocationString(final int type) {
		switch (type) {
		case 0x00 : return "Carried explicitly within descriptor";
		case 0x01 : return "Carried in Content Identifier Table (CIT)";
		case 0x02 : return "DVB reserved";
		case 0x03 : return "DVB reserved";

		default:

			return "Illegal value";

		}
	}


}
