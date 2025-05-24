package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyFunc;
import static nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.OperatorFastscan.m7FastScanCharset;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;


public class FSTsection extends TableSectionExtendedSyntax{

	private final List<Service> serviceList;

	public static class Service implements TreeNode{
		
		private int original_network_id;
		private int transport_stream_id;
		private int service_id;
		private int default_video_PID;
		private int default_audio_PID;
		private int default_video_ECM_PID;
		private int default_audio_ECM_PID;                
		private int default_PCR_PID;            
		private int reserved;
		private int transportDescriptorsLength;

		private List<Descriptor> descriptorList;

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}

		public int getOriginalNetworkID() {
			return original_network_id;
		}

		public void setOriginalNetworkID(int originalNetworkID) {
			this.original_network_id = originalNetworkID;
		}

		public int getTransportDescriptorsLength() {
			return transportDescriptorsLength;
		}

		public void setTransportDescriptorsLength(int transportDescriptorsLength) {
			this.transportDescriptorsLength = transportDescriptorsLength;
		}

		public int getTransportStreamID() {
			return transport_stream_id;
		}

		public void setTransportStreamID(int transportStreamID) {
			this.transport_stream_id = transportStreamID;
		}

		public int getService_id() {
			return service_id;
		}

		public void setService_id(int service_id) {
			this.service_id = service_id;
		}


		@Override
		public String toString(){
			StringBuilder stringBuilder = new StringBuilder("Service, transportStreamID=");
			stringBuilder.append(transport_stream_id).append(", originalNetworkID=").append(original_network_id).append(", ");
			for (Descriptor descriptor : descriptorList) {
				stringBuilder.append(descriptor).append(", ");

			}
			return stringBuilder.toString();

		}

		public KVP getJTreeNode(int modus) {
			KVP kvp = new KVP("service:", service_id);

			kvp.add(new KVP("original_network_id", original_network_id, Utils.getOriginalNetworkIDString(original_network_id)));
			kvp.add(new KVP("transport_stream_id", transport_stream_id));
			kvp.add(new KVP("service_id", service_id));
			kvp.add(new KVP("default_video_PID", default_video_PID));
			kvp.add(new KVP("default_audio_PID", default_audio_PID));
			kvp.add(new KVP("default_video_ECM_PID", default_video_ECM_PID));
			kvp.add(new KVP("default_audio_ECM_PID", default_audio_ECM_PID));
			kvp.add(new KVP("default_PCR_PID", default_PCR_PID));
			kvp.add(new KVP("default_video_ECM_PID", default_video_ECM_PID));

			kvp.add(new KVP("descriptor_loop_length", transportDescriptorsLength));

			kvp.addList(descriptorList, modus, "service_descriptor");

			return kvp;
		}

		public int getDefault_video_PID() {
			return default_video_PID;
		}

		public void setDefault_video_PID(int default_video_PID) {
			this.default_video_PID = default_video_PID;
		}

		public int getDefault_audio_PID() {
			return default_audio_PID;
		}

		public void setDefault_audio_PID(int default_audio_PID) {
			this.default_audio_PID = default_audio_PID;
		}

		public int getDefault_video_ECM_PID() {
			return default_video_ECM_PID;
		}

		public void setDefault_video_ECM_PID(int default_video_ECM_PID) {
			this.default_video_ECM_PID = default_video_ECM_PID;
		}

		public int getDefault_audio_ECM_PID() {
			return default_audio_ECM_PID;
		}

		public void setDefault_audio_ECM_PID(int default_audio_ECM_PID) {
			this.default_audio_ECM_PID = default_audio_ECM_PID;
		}

		public int getDefault_PCR_PID() {
			return default_PCR_PID;
		}

		public void setDefault_PCR_PID(int default_PCR_PID) {
			this.default_PCR_PID = default_PCR_PID;
		}

		public int getReserved() {
			return reserved;
		}

		public void setReserved(int reserved) {
			this.reserved = reserved;
		}



	}



	public FSTsection(PsiSectionData raw_data, PID parent){
		super(raw_data,parent);
		serviceList = buildTransportStreamList(raw_data.getData(), 8, sectionLength - 9);

	}


	public int getOperatorNetworkID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){

        return "FSTsection section=" + getSectionNumber() + ", lastSection=" + getSectionLastNumber() + ", tableType=" + getTableType(tableId) + ", NetworkID=" + getOperatorNetworkID() + ", ";
	}






	private List<Service> buildTransportStreamList(byte[] data, int offset, int programInfoLength) {
		List<Service> r = new ArrayList<>();
		int t =0;
		while(t<programInfoLength){
			Service service = new Service();
			service.setOriginalNetworkID(Utils.getInt(data, offset+t, 2, Utils.MASK_16BITS));
			service.setTransportStreamID(Utils.getInt(data, offset+t+2, 2, Utils.MASK_16BITS));
			service.setService_id(Utils.getInt(data, offset+t+4, 2, Utils.MASK_16BITS));
			service.setDefault_video_PID(Utils.getInt(data, offset+t+6, 2, Utils.MASK_16BITS));
			service.setDefault_audio_PID(Utils.getInt(data, offset+t+8, 2, Utils.MASK_16BITS));
			
			service.setDefault_video_ECM_PID(Utils.getInt(data, offset+t+10, 2, Utils.MASK_16BITS));
			
			service.setDefault_audio_ECM_PID(Utils.getInt(data, offset+t+12, 2, Utils.MASK_16BITS));

			service.setDefault_PCR_PID(Utils.getInt(data, offset+t+14, 2, Utils.MASK_16BITS));
			
			
			service.setTransportDescriptorsLength(Utils.getInt(data, offset+t+16, 2, Utils.MASK_12BITS));
			service.setDescriptorList(DescriptorFactory.buildDescriptorList(data,offset+t+18,service.getTransportDescriptorsLength(),this));
			t+=18+service.getTransportDescriptorsLength();
			r.add(service);

		}

		return r;
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP kvp = (KVP) super.getJTreeNode(modus);
		kvp.addTableSource(this::getTableModel, "FST");
		kvp.addList(serviceList, modus, "service_loop");
		return kvp;
	}


	@Override
	protected String getTableIdExtensionLabel() {
		return "operator_network_id";
	}
	
	
	public TableModel getTableModel() {
		FlexTableModel<FSTsection,Service> tableModel =  new FlexTableModel<>(buildFstTableHeader());

		tableModel.addData(this, serviceList);

		tableModel.process();
		return tableModel;
	}


	public List<Service> getServiceList() {
		return serviceList;
	}
	

	static TableHeader<FSTsection,Service>  buildFstTableHeader() {

		return new TableHeaderBuilder<FSTsection,Service>().
				addRequiredRowColumn("onid", Service::getOriginalNetworkID, Integer.class).
				addRequiredRowColumn("tsid", Service::getTransportStreamID, Integer.class).
				addRequiredRowColumn("sid", Service::getService_id, Integer.class).

				addRequiredRowColumn("default_video_pid", Service::getDefault_video_PID, Integer.class).
				addRequiredRowColumn("default_audio_pid", Service::getDefault_audio_PID, Integer.class).
				addRequiredRowColumn("default_pcr_pid", Service::getDefault_PCR_PID, Integer.class).

				addOptionalRowColumn("service name",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceName().toString(m7FastScanCharset)),
						String.class).
				addOptionalRowColumn("service provider",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceProviderName().toString(m7FastScanCharset)),
						String.class).
				addOptionalRowColumn("service type",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> Descriptor.getServiceTypeString(sd.getServiceType())),
						String.class).
				build();
	}

}


