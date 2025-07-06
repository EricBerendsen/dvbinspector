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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;

public class DSMCC_StreamDescriptorList extends TableSectionExtendedSyntax {

	private List<Descriptor>	descriptorList;

	public DSMCC_StreamDescriptorList(PsiSectionData raw_data, PID parent){
		super(raw_data, parent);
		descriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(), 8, sectionLength - 9, this);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("DSMCC_StreamDescriptorList section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber());

		return b.toString();
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = (KVP)super.getJTreeNode(modus);
		t.addList(descriptorList, modus, "descriptors");
		return t;
	}
	
	// B.2.4.3.5 Encoding of table id extension, TS 102 809 V1.3.1 (2017-06)
	@Override
	protected String getTableIdExtensionDescription(int tableIdExtension) {
		int type = (tableIdExtension & 0b1100_0000_0000_0000) >>14;
        return switch (type) {
            case 0 -> "Section carries a single \"do it now\" event, eventID=" + (tableIdExtension & 0b0011_1111_1111_1111);
            case 1 -> "Section carries NPT reference descriptors";
            case 2 -> "Section carries one or more other stream descriptors";
            case 3 -> "reserved for future use";
            default -> "Illegal value";
        };

	}


	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public void setDescriptorList(List<Descriptor> descriptorList) {
		this.descriptorList = descriptorList;
	}

}
