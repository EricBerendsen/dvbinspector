package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import javax.swing.tree.DefaultMutableTreeNode;

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

	public final List<Service> serviceList;

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

		public void setDescriptorList(final List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}

		public int getOriginalNetworkID() {
			return original_network_id;
		}

		public void setOriginalNetworkID(final int originalNetworkID) {
			this.original_network_id = originalNetworkID;
		}

		public int getTransportDescriptorsLength() {
			return transportDescriptorsLength;
		}

		public void setTransportDescriptorsLength(final int transportDescriptorsLength) {
			this.transportDescriptorsLength = transportDescriptorsLength;
		}

		public int getTransportStreamID() {
			return transport_stream_id;
		}

		public void setTransportStreamID(final int transportStreamID) {
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
			final StringBuilder b = new StringBuilder("Service, transportStreamID=");
			b.append(getTransportStreamID()).append(", originalNetworkID=").append(getOriginalNetworkID()).append(", ");
			for (Descriptor d : descriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}
		public DefaultMutableTreeNode getJTreeNode(final int modus){

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("service:",service_id,null));

			t.add(new DefaultMutableTreeNode(new KVP("original_network_id",original_network_id,Utils.getOriginalNetworkIDString(original_network_id) )));
			t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transport_stream_id,null)));
			
			t.add(new DefaultMutableTreeNode(new KVP("service_id",service_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("default_video_PID",default_video_PID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("default_audio_PID",default_audio_PID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("default_video_ECM_PID",default_video_ECM_PID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("default_audio_ECM_PID",default_audio_ECM_PID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("default_PCR_PID",default_PCR_PID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("default_video_ECM_PID",default_video_ECM_PID,null)));
			
			t.add(new DefaultMutableTreeNode(new KVP("descriptor_loop_length",getTransportDescriptorsLength(),null)));

			Utils.addListJTree(t,descriptorList,modus,"service_descriptor");

			return t;
		}

		public int getOriginal_network_id() {
			return original_network_id;
		}

		public void setOriginal_network_id(int original_network_id) {
			this.original_network_id = original_network_id;
		}

		public int getTransport_stream_id() {
			return transport_stream_id;
		}

		public void setTransport_stream_id(int transport_stream_id) {
			this.transport_stream_id = transport_stream_id;
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



	public FSTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		serviceList = buildTransportStreamList(raw_data.getData(), 8, sectionLength - 9);

	}


	public int getOperatorNetworkID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("FSTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=").append(getTableType(tableId)). append(", NetworkID=").append(getOperatorNetworkID()).append(", ");

		return b.toString();
	}






	private List<Service> buildTransportStreamList(final byte[] data, final int offset, final int programInfoLength) {
		final ArrayList<Service> r = new ArrayList<>();
		int t =0;
		while(t<programInfoLength){
			final Service c = new Service();
			c.setOriginalNetworkID(Utils.getInt(data, offset+t, 2, Utils.MASK_16BITS));
			c.setTransportStreamID(Utils.getInt(data, offset+t+2, 2, Utils.MASK_16BITS));
			c.setService_id(Utils.getInt(data, offset+t+4, 2, Utils.MASK_16BITS));
			c.setDefault_video_PID(Utils.getInt(data, offset+t+6, 2, Utils.MASK_16BITS));
			c.setDefault_audio_PID(Utils.getInt(data, offset+t+8, 2, Utils.MASK_16BITS));
			
			c.setDefault_video_ECM_PID(Utils.getInt(data, offset+t+10, 2, Utils.MASK_16BITS));
			
			c.setDefault_audio_ECM_PID(Utils.getInt(data, offset+t+12, 2, Utils.MASK_16BITS));

			c.setDefault_PCR_PID(Utils.getInt(data, offset+t+14, 2, Utils.MASK_16BITS));
			
			
			c.setTransportDescriptorsLength(Utils.getInt(data, offset+t+16, 2, Utils.MASK_12BITS));
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,offset+t+18,c.getTransportDescriptorsLength(),this));
			t+=18+c.getTransportDescriptorsLength();
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		KVP kvp = (KVP) t.getUserObject();
		kvp.setTableSource(this::getTableModel);


		Utils.addListJTree(t,serviceList,modus,"service_loop");
		return t;
	}

	
	protected String getTableIdExtensionLabel() {
		return "operator_network_id";
	}
	
	
	public TableModel getTableModel() {
		FlexTableModel<FSTsection,Service> tableModel =  new FlexTableModel<>(buildFstTableHeader());

		tableModel.addData(this, getServiceList());

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


