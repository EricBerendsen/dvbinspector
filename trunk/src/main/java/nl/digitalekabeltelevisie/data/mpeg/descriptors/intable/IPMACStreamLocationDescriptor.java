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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.intable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class IPMACStreamLocationDescriptor extends INTDescriptor {


	private final int networkId;
	private int originalNetworkId;
	private int transportStreamId;
	private final int serviceId;
	private final int componentTag;




	public IPMACStreamLocationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		networkId = getInt(b,offset+2,2,MASK_16BITS);
		originalNetworkId = getInt(b,offset+4,2,MASK_16BITS);
		transportStreamId = getInt(b,offset+6,2,MASK_16BITS);
		serviceId = getInt(b,offset+8,2,MASK_16BITS);
		componentTag = getInt(b,offset+10,1,MASK_8BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "transportStreamId="+transportStreamId;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("network_id",networkId ,Utils.getOriginalNetworkIDString(networkId))));
		t.add(new DefaultMutableTreeNode(new KVP("original_network_id",originalNetworkId ,Utils.getOriginalNetworkIDString(originalNetworkId))));
		t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamId ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("service_id",serviceId ,parentTableSection.getParentPID().getParentTransportStream().getPsi().getSdt().getServiceName(serviceId))));
		t.add(new DefaultMutableTreeNode(new KVP("component_tag",componentTag ,null)));
		return t;
	}

	public int getOriginalNetworkId() {
		return originalNetworkId;
	}


	public void setOriginalNetworkId(final int caPID) {
		this.originalNetworkId = caPID;
	}

	public int getTransportStreamId() {
		return transportStreamId;
	}

	public void setTransportStreamId(final int caSystemID) {
		this.transportStreamId = caSystemID;
	}





}
