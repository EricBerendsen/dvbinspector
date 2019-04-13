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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class UpdateDescriptor extends UNTDescriptor {


	private final int update_flag;
	private final int update_method;
	private final int update_priority;
	private final byte[] privateDataByte;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public UpdateDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		update_flag = Utils.getInt(b, offset + 2, 1, 0xC0)>>6;
		update_method = Utils.getInt(b, offset + 2, 1, 0x3C)>>2;
		update_priority = Utils.getInt(b, offset + 2, 1,Utils.MASK_2BITS);
		privateDataByte = Utils.copyOfRange(b, offset+3, offset+descriptorLength+2);



	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("update_flag", update_flag, getUpdateFlagString(update_flag))));
		t.add(new DefaultMutableTreeNode(new KVP("update_method", update_method, getUpdateMethodString(update_method))));
		t.add(new DefaultMutableTreeNode(new KVP("update_priority", update_priority, null)));
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}


	public static String getUpdateFlagString(final int t){

		switch (t) {
		case 0x00: return"The update has to be activated manually";
		case 0x01: return"The update may be performed automatically";
		case 0x02: return"Reserved for future use";
		case 0x03: return"Reserved for future use";

		default:
			return "illegal value";
		}
	}

	public static String getUpdateMethodString(final int t){

		if((t>=0x03)&&(t<=0x07)){
			return "reserved for future use";
		}


		switch (t) {
		case 0x00: return"immediate update: performed whatever the IRD state";
		case 0x01: return"IRD available: the update is available in the stream; it will be taken into account when it does not interfere with the normal user operation";
		case 0x02: return"next restart: the update is available in the stream; it will be taken into account at the next IRD restart";
		case 0x0F: return"reserved";

		default:
			return "private use";
		}
	}


}
