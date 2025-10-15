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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class BATsection extends TableSectionExtendedSyntax{

	private List<Descriptor>		networkDescriptorList;
	private List<TransportStream>	transportStreamList;
	private int						networkDescriptorsLength;
	private int						transportStreamLoopLength;

	public BATsection(PsiSectionData raw_data, PID parent){
		super(raw_data, parent);

		networkDescriptorsLength = getInt(raw_data.getData(), 8, 2, MASK_12BITS);

		networkDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(), 10, networkDescriptorsLength,
				this);
		transportStreamLoopLength = getInt(raw_data.getData(), 10 + networkDescriptorsLength, 2, MASK_12BITS);
		transportStreamList = buildTransportStreamList(raw_data.getData(), 12 + networkDescriptorsLength,
				transportStreamLoopLength);
	}

	public int getBouqetID() {
		return getTableIdExtension();
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("BATsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", batID=").append(getBouqetID()).append(", ");

		return b.toString();
	}

	public List<Descriptor> getNetworkDescriptorList() {
		return networkDescriptorList;
	}

	public int getNetworkDescriptorsLength() {
		return networkDescriptorsLength;
	}

	public List<TransportStream> getTransportStreamList() {
		return transportStreamList;
	}

	public int getTransportStreamLoopLength() {
		return transportStreamLoopLength;
	}

	public int noTransportStreams() {
		return transportStreamList.size();
	}

	private List<TransportStream> buildTransportStreamList(byte[] data, int i, int programInfoLength) {
		List<TransportStream> r = new ArrayList<>();
		int t = 0;
		while (t < programInfoLength) {
			
			int transport_stream_id = Utils.getInt(data, i + t, 2, MASK_16BITS);
			int original_network_id = Utils.getInt(data, i + t + 2, 2, MASK_16BITS);
			int transport_descriptors_length = Utils.getInt(data, i + t + 4, 2, MASK_12BITS);
			List<Descriptor> descriptorList = DescriptorFactory.buildDescriptorList(data, i + t + 6, transport_descriptors_length, this, new DescriptorContext(original_network_id, transport_stream_id));
			
			TransportStream c = new TransportStream(transport_stream_id, original_network_id, transport_descriptors_length,descriptorList);
			t += 6 + transport_descriptors_length;
			r.add(c);

		}

		return r;
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		if(!transportStreamList.isEmpty()) {
			t.addTableSource(this::getTableModel, "bat");
		}
		t.add(new KVP("network_descriptors_length", getNetworkDescriptorsLength()));
		addListJTree(t, networkDescriptorList, modus, "network_descriptors");
		t.add(new KVP("transport_stream_loop_length", getTransportStreamLoopLength()));
		addListJTree(t, transportStreamList, modus, "transport_stream_loop");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "bouquet_id";
	}


	public TableModel getTableModel() {
		FlexTableModel<BATsection,TransportStream> tableModel =  new FlexTableModel<>(BAT.buildBatTableHeader());

		tableModel.addData(this, getTransportStreamList());

		tableModel.process();
		return tableModel;
	}


}
