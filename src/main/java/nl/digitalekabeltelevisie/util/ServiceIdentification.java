package nl.digitalekabeltelevisie.util;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

import java.util.Comparator;

/**
 * http://www.digitalekabeltelevisie.nl/dvb_inspector
 * <p>
 * This code is Copyright 2020-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * <p>
 * This file is part of DVB Inspector.
 * <p>
 * DVB Inspector is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DVB Inspector is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * The author requests that he be notified of any application, applet, or
 * other binary that makes use of this code, but that's more out of curiosity
 * than anything and is not required.
 */


public record ServiceIdentification(int originalNetworkId, int transportStreamId, int serviceId) implements Comparable<ServiceIdentification>, TreeNode {



	@Override
	public int compareTo(ServiceIdentification other) {

		return Comparator.comparingInt(ServiceIdentification::originalNetworkId)
				.thenComparingInt(ServiceIdentification::transportStreamId)
				.thenComparingInt(ServiceIdentification::serviceId)
				.compare(this, other);
	}

	/**
	 * The presentation order in this method matches that of Near Video On Demand (NVOD) reference descriptor
	 * transport_stream_id
	 * original_network_id
	 * service_id
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("Service Identification (onid:" + originalNetworkId + ",tsid:" + transportStreamId + ",sid:" + serviceId + ")"));
		s.add(new DefaultMutableTreeNode(new KVP("transport_stream_id", transportStreamId, null)));
		s.add(new DefaultMutableTreeNode(new KVP("original_network_id", originalNetworkId, null)));
		s.add(new DefaultMutableTreeNode(new KVP("service_id", serviceId, null)));
		return s;
	}
}
