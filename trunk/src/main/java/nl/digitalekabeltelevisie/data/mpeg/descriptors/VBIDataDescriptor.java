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

/**
 * The VBI data descriptor shall be used in the PSI PMT of a stream which carries VBI data
 *
 * @see "ETSI EN 300 468, 6.2.47 VBI data descriptor"
 */
public class VBIDataDescriptor extends Descriptor {

	private List<DataService> serviceList = new ArrayList<DataService>();

	public class DataService implements TreeNode{
		private final int dataServiceId;
		private final int dataServiceDescriptorLength;
		private List<VBILine> linesList;
		private byte[] reserved;

		public class VBILine implements TreeNode{

			private final int reserved;
			private final int fieldParity;
			private final int lineOffset;

			VBILine(final int t){
				reserved = (t & 0xC0)>>6;
				fieldParity = (t & 0x20)>>5;
				lineOffset= (t & 0x1F);
			}

			public DefaultMutableTreeNode getJTreeNode(final int modus){
				final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("vbi_line"));
				s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
				s.add(new DefaultMutableTreeNode(new KVP("field_parity",fieldParity,null)));
				s.add(new DefaultMutableTreeNode(new KVP("line_offset",lineOffset,null)));
				return s;
			}

		}


		DataService(final int id, final int length, final byte[]data){
			this.dataServiceId = id;
			this.dataServiceDescriptorLength = length;

			if ((dataServiceId==0x01) ||
					(dataServiceId==0x02) ||
					(dataServiceId==0x04) ||
					(dataServiceId==0x05) ||
					(dataServiceId==0x06) ||
					(dataServiceId==0x07)) {
				linesList = new ArrayList<VBILine>();
				for (int i = 0; i < data.length; i++) {
					final VBILine line = new VBILine(getInt(data, i, 1, 0xFF));
					linesList.add(line);
				}
			}else{
				reserved = data;
			}
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("data_service"));
			s.add(new DefaultMutableTreeNode(new KVP("data_service_id",dataServiceId,getDataServiceIDString(dataServiceId))));
			s.add(new DefaultMutableTreeNode(new KVP("data_service_descriptor_length",dataServiceDescriptorLength,null)));
			if(linesList!=null){
				addListJTree(s, linesList, modus, "vbi_lines");
			}else{
				s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			}
			return s;
		}

	}

	public VBIDataDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int serviceId = getInt(b, offset+t+2, 1,MASK_8BITS);
			final int serviceLength = getInt(b, offset+t+3, 1,MASK_8BITS);
			final DataService s = new DataService(serviceId,serviceLength, getBytes(b, offset+t+4, serviceLength));
			serviceList.add(s);
			t+=2+serviceLength;
		}
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,serviceList,modus,"data_service");
		return t;
	}



	/**
	 * @param dataServiceID
	 * @return String with description of the VBI service type
	 */
	public static String getDataServiceIDString(final int dataServiceID){
		switch (dataServiceID) {
		case 0x00 : return "reserved for future use";
		case 0x01 : return "EBU teletext (Requires additional teletext_descriptor)";
		case 0x02 : return "inverted teletext";
		case 0x03 : return "reserved";
		case 0x04 : return "VPS";
		case 0x05 : return "WSS";
		case 0x06 : return "Closed Captioning";
		case 0x07 : return "monochrome 4:2:2 samples";
		default:
			return "reserved for future use";


		}

	}
}
