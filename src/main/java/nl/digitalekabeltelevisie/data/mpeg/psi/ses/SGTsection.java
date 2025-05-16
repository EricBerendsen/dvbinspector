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
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

/**
 * 
 */
public class SGTsection extends TableSectionExtendedSyntax {
	
	public static class Service implements TreeNode{
		
		private int service_id;
		private int transport_stream_id;
		private int original_network_id;
		private int logical_channel_number;
		private int visible_service_flag;
		private int new_service_flag;
		private int genre_code;
		private int reserved_future_use;
		private int service_descriptors_length;

		private List<Descriptor> descriptorList;

		@Override
		public KVP getJTreeNode(int modus) {
			KVP kvp = new KVP("Service")
				.addChild("service_id", service_id)
				.addChild("transport_stream_id",transport_stream_id)
				.addChild("original_network_id",original_network_id)
				.addChild("logical_channel_number",logical_channel_number)
				.addChild("visible_service_flag",visible_service_flag)
				.addChild("new_service_flag",new_service_flag)
				.addChild("genre_code",genre_code)
				.addChild("reserved_future_use",reserved_future_use)
				.addChild("service_descriptors_length",service_descriptors_length);

			addListJTree(kvp,descriptorList,modus,"service_descriptors");
		
			return kvp;
		}

		public int getService_id() {
			return service_id;
		}

		public void setService_id(int service_id) {
			this.service_id = service_id;
		}

		public int getTransport_stream_id() {
			return transport_stream_id;
		}

		public void setTransport_stream_id(int transport_stream_id) {
			this.transport_stream_id = transport_stream_id;
		}

		public int getOriginal_network_id() {
			return original_network_id;
		}

		public void setOriginal_network_id(int original_network_id) {
			this.original_network_id = original_network_id;
		}

		public int getLogical_channel_number() {
			return logical_channel_number;
		}

		public void setLogical_channel_number(int logical_channel_number) {
			this.logical_channel_number = logical_channel_number;
		}

		public int getVisible_service_flag() {
			return visible_service_flag;
		}

		public void setVisible_service_flag(int visible_service_flag) {
			this.visible_service_flag = visible_service_flag;
		}

		public int getNew_service_flag() {
			return new_service_flag;
		}

		public void setNew_service_flag(int new_service_flag) {
			this.new_service_flag = new_service_flag;
		}

		public int getGenre_code() {
			return genre_code;
		}

		public void setGenre_code(int genre_code) {
			this.genre_code = genre_code;
		}

		public int getReserved_future_use() {
			return reserved_future_use;
		}

		public void setReserved_future_use(int reserved_future_use) {
			this.reserved_future_use = reserved_future_use;
		}

		public int getService_descriptors_length() {
			return service_descriptors_length;
		}

		public void setService_descriptors_length(int service_descriptors_length) {
			this.service_descriptors_length = service_descriptors_length;
		}

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}
	
		
		
	}

	private int reserved1;
	private int reserved_future_use1;
	private int service_list_descriptors_length;
	private List<Descriptor> serviceListDescriptorsList;
	private int reserved_future_use2;
	private int service_loop_length;
	private List<Service> serviceList;

	public SGTsection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);

		reserved1 = getInt(raw_data.getData(), 8, 2, MASK_16BITS);
		reserved_future_use1 = getInt(raw_data.getData(), 10, 1, 0b1111_0000) >>> 4;
		service_list_descriptors_length = getInt(raw_data.getData(), 10, 2, MASK_12BITS);
		serviceListDescriptorsList = DescriptorFactory.buildDescriptorList(raw_data.getData(), 12, service_list_descriptors_length, this);

		reserved_future_use2 = getInt(raw_data.getData(), 12 + service_list_descriptors_length, 1, 0b1111_0000) >>> 4;
		service_loop_length = getInt(raw_data.getData(), 12 + service_list_descriptors_length, 2, MASK_12BITS);
		serviceList = buildServicesList(raw_data.getData(), 14 + service_list_descriptors_length, service_loop_length);
	}
	
	private List<Service> buildServicesList(byte[] data, int offset, int programInfoLength) {
		List<Service> r = new ArrayList<>();
		int t = offset;
		while (t < (programInfoLength + offset)) {
			Service service = new Service();
			service.setService_id(getInt(data, t, 2, MASK_16BITS));
			service.setTransport_stream_id(getInt(data, t + 2, 2, MASK_16BITS));
			service.setOriginal_network_id(getInt(data, t + 4, 2, MASK_16BITS));
			service.setLogical_channel_number(getInt(data, t + 6, 2, 0b1111_1111_1111_1100) >>> 2);
			service.setVisible_service_flag(getInt(data, t + 7, 1, 0b0000_0010) >>> 1);
			service.setNew_service_flag(getInt(data, t + 7, 1, 0b0000_0001));
			service.setGenre_code(getInt(data, t + 8, 2, MASK_16BITS));
			service.setReserved_future_use(getInt(data, t + 10, 1, 0b1111_0000) >>> 4);

			int service_descriptors_length = getInt(data, t + 10, 2, MASK_12BITS);
			service.setService_descriptors_length(service_descriptors_length);
			service.setDescriptorList(DescriptorFactory.buildDescriptorList(data, t + 12, service_descriptors_length, this));

			t += 12 + service_descriptors_length;
			r.add(service);
		}
		return r;
	}
	
	@Override
	protected String getTableIdExtensionLabel() {
		return "service_list_id";
	}

	
	public int getServiceListId() {
		return getTableIdExtension();
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = (KVP)super.getJTreeNode(modus);
			t.addTableSource(this::getTableModel, "SGT Section")
			.addChild("reserved", reserved1)
			.addChild("reserved_future_use", reserved_future_use1)
			.addChild("service_list_descriptors_length", service_list_descriptors_length);
		addListJTree(t, serviceListDescriptorsList, modus, "service_list_descriptors");
		t.addChild("reserved_future_use", reserved_future_use2)
			.addChild("service_loop_length", service_loop_length);
		
		addListJTree(t,serviceList,modus,"services_loop");

		return t;
	}
	
	
	public TableModel getTableModel() {
		FlexTableModel<SGTsection,Service> tableModel =  new FlexTableModel<>(SGT.buildSgtTableHeader());
		
		tableModel.addData(this, serviceList);
		tableModel.process();
		return tableModel;
	}

	public int getReserved1() {
		return reserved1;
	}

	public int getReserved_future_use1() {
		return reserved_future_use1;
	}

	public int getService_list_descriptors_length() {
		return service_list_descriptors_length;
	}

	public List<Descriptor> getServiceListDescriptorsList() {
		return serviceListDescriptorsList;
	}

	public int getReserved_future_use2() {
		return reserved_future_use2;
	}

	public int getService_loop_length() {
		return service_loop_length;
	}

	public List<Service> getServiceList() {
		return serviceList;
	}

	
}
