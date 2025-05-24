package nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan;
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
import static nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan.OperatorFastscan.m7FastScanCharset;
import static nl.digitalekabeltelevisie.util.Utils.getOriginalNetworkIDString;

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

	public record Service(int original_network_id, int transport_stream_id, int service_id, int default_video_PID,
						  int default_audio_PID, int default_video_ECM_PID, int default_audio_ECM_PID,
						  int default_PCR_PID, int transportDescriptorsLength,
						  List<Descriptor> descriptorList) implements TreeNode {


		public KVP getJTreeNode(int modus) {
			KVP kvp = new KVP("service:", service_id);

			kvp.add(new KVP("original_network_id", original_network_id, getOriginalNetworkIDString(original_network_id)));
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
		int t = 0;
		while (t < programInfoLength) {
			int originalNetworkID = Utils.getInt(data, offset + t, 2, Utils.MASK_16BITS);
			int transportStreamID = Utils.getInt(data, offset + t + 2, 2, Utils.MASK_16BITS);
			int serviceId = Utils.getInt(data, offset + t + 4, 2, Utils.MASK_16BITS);
			int defaultVideoPid = Utils.getInt(data, offset + t + 6, 2, Utils.MASK_16BITS);
			int defaultAudioPid = Utils.getInt(data, offset + t + 8, 2, Utils.MASK_16BITS);
			int defaultVideoEcmPid = Utils.getInt(data, offset + t + 10, 2, Utils.MASK_16BITS);
			int defaultAudioEcmPid = Utils.getInt(data, offset + t + 12, 2, Utils.MASK_16BITS);
			int defaultPcrPid = Utils.getInt(data, offset + t + 14, 2, Utils.MASK_16BITS);
			int transportDescriptorsLength = Utils.getInt(data, offset + t + 16, 2, Utils.MASK_12BITS);
			List<Descriptor> descriptorList = DescriptorFactory.buildDescriptorList(data, offset + t + 18, transportDescriptorsLength, this);

			Service service = new Service(originalNetworkID,
					transportStreamID,
					serviceId,
					defaultVideoPid,
					defaultAudioPid,
					defaultVideoEcmPid,
					defaultAudioEcmPid,
					defaultPcrPid,
					transportDescriptorsLength,
					descriptorList);
			t += 18 + transportDescriptorsLength;
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
				addRequiredRowColumn("onid", Service::original_network_id, Integer.class).
				addRequiredRowColumn("tsid", Service::transport_stream_id, Integer.class).
				addRequiredRowColumn("sid", Service::service_id, Integer.class).

				addRequiredRowColumn("default_video_pid", Service::default_video_PID, Integer.class).
				addRequiredRowColumn("default_audio_pid", Service::default_audio_PID, Integer.class).
				addRequiredRowColumn("default_pcr_pid", Service::default_PCR_PID, Integer.class).

				addOptionalRowColumn("service name",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceName().toString(m7FastScanCharset)),
						String.class).
				addOptionalRowColumn("service provider",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceProviderName().toString(m7FastScanCharset)),
						String.class).
				addOptionalRowColumn("service type",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								ServiceDescriptor.class,
								sd -> Descriptor.getServiceTypeString(sd.getServiceType())),
						String.class).
				build();
	}

}


