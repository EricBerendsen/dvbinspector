/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.ses;

import static nl.digitalekabeltelevisie.util.Utils.MASK_12BITS;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;


import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * 
 */
public class SGTsection extends TableSectionExtendedSyntax {

	private int reserved1;
	private int service_list_descriptors_length;
	private List<Descriptor> serviceListDescriptorsList;

	public SGTsection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);

		reserved1 = Utils.getInt(raw_data.getData(), 8, 2, MASK_16BITS);
		service_list_descriptors_length = Utils.getInt(raw_data.getData(), 10, 2, MASK_12BITS);

		serviceListDescriptorsList = DescriptorFactory.buildDescriptorList(raw_data.getData(), 12, service_list_descriptors_length,
				this);

	}

	
	@Override
	protected String getTableIdExtensionLabel() {
		return "service_list_id";
	}

	
	public int getServiceListId() {
		return getTableIdExtension();
	}

	@Override
	public KVP getJTreeNode(final int modus) {

		final KVP t = (KVP)super.getJTreeNode(modus);
		t.add(new KVP("reserved", reserved1));
		t.add(new KVP("service_list_descriptors_length", service_list_descriptors_length));
		Utils.addListJTree(t, serviceListDescriptorsList, modus, "service_list_descriptors");
		return t;
	}
}
