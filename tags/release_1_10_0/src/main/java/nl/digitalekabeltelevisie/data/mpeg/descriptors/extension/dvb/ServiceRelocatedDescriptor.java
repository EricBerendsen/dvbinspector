/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ServiceRelocatedDescriptor extends DVBExtensionDescriptor {


	private final int old_original_network_id;
	private final int old_transport_stream_id;
	private final int old_service_id;



	public ServiceRelocatedDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		old_original_network_id = getInt(b, privateDataOffset, 2, MASK_16BITS);
		privateDataOffset += 2;
		old_transport_stream_id = getInt(b, privateDataOffset, 2, MASK_16BITS);
		privateDataOffset += 2;
		old_service_id = getInt(b, privateDataOffset, 2, MASK_16BITS);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("old_original_network_id",old_original_network_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("old_transport_stream_id",old_transport_stream_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("old_service_id",old_service_id,null)));

		return t;
	}

}
