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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.untable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */
public class SSULocationDescriptor extends UNTDescriptor {


	private final int data_broadcast_id;
	private int association_tag;
	private final byte[] privateDataByte;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public SSULocationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		data_broadcast_id = getInt(b, offset + 2, 2,MASK_16BITS);
		int t=0;
		if(data_broadcast_id==0x000a){
			association_tag = getInt(b, offset + 4, 2,MASK_16BITS);
			t+=2;
		}
		privateDataByte = copyOfRange(b, offset+4+t, offset+descriptorLength+2);



	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("data_broadcast_id", data_broadcast_id, getDataBroadCastIDString(data_broadcast_id))));
		if(data_broadcast_id==0x000a){
			t.add(new DefaultMutableTreeNode(new KVP("association_tag", association_tag, null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}




}
