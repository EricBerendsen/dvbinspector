/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.util.Utils.getOriginalNetworkIDString;

import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.util.Utils;

public record TransportStream(int transport_stream_id, 
		int original_network_id, 
		int transport_descriptors_length,
		List<Descriptor> descriptorList) implements TreeNode {

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("Service, transport_stream_id=");
		b.append(transport_stream_id).append(", original_network_id=").append(original_network_id).append(", ");
		return b.toString();

	}

	@Override
	public KVP getJTreeNode(final int modus) {

		final KVP t = new KVP("transport_stream:", transport_stream_id);

		t.add(new KVP("transport_stream_id", transport_stream_id));
		t.add(new KVP("original_network_id", original_network_id, getOriginalNetworkIDString(original_network_id)));
		t.add(new KVP("transport_descriptors_length", transport_descriptors_length));

		Utils.addListJTree(t, descriptorList, modus, "transport_descriptors");

		return t;
	}

}