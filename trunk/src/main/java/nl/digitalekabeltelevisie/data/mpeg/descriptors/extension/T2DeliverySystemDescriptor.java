/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class T2DeliverySystemDescriptor extends ExtensionDescriptor {

	private static Logger logger = Logger.getLogger(T2DeliverySystemDescriptor.class.getName());



	// T2 delivery descriptor 0x04

	private int plp_id;
	private int t2_system_id;

	public T2DeliverySystemDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		plp_id = getInt(b, offset+3, 1, MASK_8BITS);
		t2_system_id = getInt(b, offset+4, 2, MASK_16BITS);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("plp_id",plp_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("T2_system_id",t2_system_id,null)));

		return t;
	}




}
