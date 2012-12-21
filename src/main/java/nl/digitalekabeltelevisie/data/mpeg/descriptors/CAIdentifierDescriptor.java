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

public class CAIdentifierDescriptor extends Descriptor {

	private final List<CASystemId> CA_system_id = new ArrayList<CASystemId>();

	public class CASystemId implements TreeNode{
		private final int ca_system_id;

		public CASystemId(final int ca_system_id) {
			super();
			this.ca_system_id = ca_system_id;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			return new DefaultMutableTreeNode(new KVP("CA_system_id",ca_system_id ,getCASystemIDString(ca_system_id)));
		}
	}



	public CAIdentifierDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while(t<descriptorLength){
			final int caSystemID = getInt(b, offset + 2 + t, 2, MASK_16BITS);
			final CASystemId caID= new CASystemId(caSystemID);
			CA_system_id.add(caID);
			t+=2;
		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,CA_system_id,modus,"CA_system_id");
		return t;
	}


}
