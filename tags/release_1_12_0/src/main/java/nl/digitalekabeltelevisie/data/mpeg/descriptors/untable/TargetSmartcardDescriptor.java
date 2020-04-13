/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.getCASystemIDString;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class TargetSmartcardDescriptor extends UNTDescriptor {


	private final int super_CA_system_id;
	private final byte[] privateDataByte;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public TargetSmartcardDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		super_CA_system_id = Utils.getInt(b, offset + 2, 4, Utils.MASK_32BITS);
		privateDataByte = Utils.copyOfRange(b, offset+6, offset+descriptorLength+2);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		DefaultMutableTreeNode super_CA_system_idNode = new DefaultMutableTreeNode(new KVP("super_CA_system_id", super_CA_system_id, null));
		int caSystemID = super_CA_system_id>>>16;
		int caSubsystem_id = super_CA_system_id & Utils.MASK_16BITS;
		super_CA_system_idNode.add(new DefaultMutableTreeNode(new KVP("CA_system_id", caSystemID, getCASystemIDString(caSystemID))));
		super_CA_system_idNode.add(new DefaultMutableTreeNode(new KVP("CA_subsystem_id", caSubsystem_id, null)));
		t.add(super_CA_system_idNode);
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}


}
