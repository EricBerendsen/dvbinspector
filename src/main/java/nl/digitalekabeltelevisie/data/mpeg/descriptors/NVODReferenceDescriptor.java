/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.ServiceIdentification;

public class NVODReferenceDescriptor extends Descriptor {

	private List<ServiceIdentification> servicesList = new ArrayList<>();

	public NVODReferenceDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			int transportStreamId = getInt(b, 2 + t, 2, MASK_16BITS);
			int originalNetworkId = getInt(b, 4 + t, 2, MASK_16BITS);
			int serviceId = getInt(b, 6 + t, 2, MASK_16BITS);
			ServiceIdentification serviceIdentification = new ServiceIdentification(originalNetworkId, transportStreamId, serviceId);
			servicesList.add(serviceIdentification);
			t += 6;
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		addListJTree(t, servicesList, modus, "service_list");
		return t;
	}

}
