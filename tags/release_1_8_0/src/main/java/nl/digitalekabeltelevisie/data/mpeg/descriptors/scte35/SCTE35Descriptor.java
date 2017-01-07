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
 * Based on TS 102 006
 */
package nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35;

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */
public class SCTE35Descriptor extends Descriptor {
	
	private final byte[]  identifier;


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public SCTE35Descriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		identifier = getBytes(b,offset+2,4);
	}

	@Override
	public String getDescriptorname(){
		return SCTE35Descriptor.getDescriptorname(descriptorTag, parentTableSection);
	}

	public static String getDescriptorname(final int tag, final TableSection tableSection){

		switch (tag) {
		case 0x00: return "avail_descriptor"; 
		case 0x01: return "DTMF_descriptor";
		case 0x02: return "segmentation_descriptor"; 
		case 0x03: return "DVB-time_descriptor";
		default:

			return "Reserved for future SCTE splice_descriptors";

		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("identifier",identifier ,null)));
		return t;
	}

}
