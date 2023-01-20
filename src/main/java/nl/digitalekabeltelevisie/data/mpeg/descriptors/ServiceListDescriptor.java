/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.NITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.data.mpeg.psi.TransportStream;
import nl.digitalekabeltelevisie.gui.TableSource;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class ServiceListDescriptor extends Descriptor implements TableSource {

	private final List<Service> serviceList = new ArrayList<>();
	private DescriptorContext descriptorContext;


	public class Service implements TreeNode{
		private int serviceID;
		private int serviceType;

		public Service(final int id, final int type){
			serviceID = id;
			serviceType = type;
		}

		public int getServiceID() {
			return serviceID;
		}

		public void setServiceID(final int serviceID) {
			this.serviceID = serviceID;
		}

		public int getServiceType() {
			return serviceType;
		}

		public String getServiceTypeString() {
			return 	Descriptor.getServiceTypeString(serviceType);
		}



		public void setServiceType(final int serviceType) {
			this.serviceType = serviceType;
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus){
		
			String nodeLabel = "service";
			if(descriptorContext.hasOnidTsid()) {
				String serviceName = getServiceName();
				if(serviceName != null){
					nodeLabel = "service ("+serviceName+")";
				}
				
			}

			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP(nodeLabel));
			s.add(new DefaultMutableTreeNode(new KVP("service_id",serviceID,null)));
			s.add(new DefaultMutableTreeNode(new KVP("service_type",serviceType,Descriptor.getServiceTypeString(serviceType))));
			return s;

		}

		public String getServiceName() {
			return getPSI().getSdt().getServiceName(descriptorContext.original_network_id, descriptorContext.transport_stream_id, serviceID);
		}
		
		public Integer getLCN() {
			NITsection[] networkSections = getPSI().getNit().getNetworks().get(descriptorContext.getNetwork_id());
			for (NITsection section : networkSections) {
				if (section != null) {
					TransportStream stream = section.getTransportStream(descriptorContext.transport_stream_id);
					if (stream != null) {
						for (Descriptor descriptor : stream.descriptorList()) {
							if (descriptor instanceof AbstractLogicalChannelDescriptor abstractLogicalChannelDescriptor) {
								Integer lcn = abstractLogicalChannelDescriptor.getLCN(serviceID);
								if(lcn !=null) {
									return lcn;
								}
							}
						}
						return null;
					}
				}
			}
			return null;
		}		
		
		
	}

	public ServiceListDescriptor(final byte[] b, final int offset, final TableSection parent, DescriptorContext descriptorContext) {
		super(b, offset,parent);
		this.descriptorContext = descriptorContext;
		int t=0;
		while (t<descriptorLength) {
			final int serviceId=getInt(b, offset+2+t,2,MASK_16BITS);
			final int serviceType=getInt(b, offset+4+t,1,MASK_8BITS);
			final Service s = new Service( serviceId,serviceType);
			serviceList.add(s);
			t+=3;
		}
	}

	public int getNoServices(){
		return serviceList.size();
	}


	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (int i = 0; i < getNoServices(); i++) {
			final Service s = serviceList.get(i);
			buf.append("(").append(i).append(";").append(s.getServiceID()).append(":").append(s.getServiceTypeString()).append("),");
		}

		return buf.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus, this);
		addListJTree(t,serviceList,modus,"service_list",this);
		return t;
	}

	public List<Service> getServiceList() {
		return serviceList;
	}

	private static TableHeader<Service,Service>  buildTableHeader() {

		return new TableHeaderBuilder<Service,Service>().
				addRequiredRowColumn("service_id", Service::getServiceID, Integer.class).
				addRequiredRowColumn("service_type", Service::getServiceType, Integer.class).
				addRequiredRowColumn("service type description", Service::getServiceTypeString, String.class).
				addOptionalRowColumn("service name", Service::getServiceName, String.class).
				build();
	}


	@Override
	public TableModel getTableModel() {
		FlexTableModel<Service,Service> tableModel =  new FlexTableModel<>(buildTableHeader());

		for(Service service:serviceList) {
			tableModel.addData(service, List.of(service));
		}

		tableModel.process();
		return tableModel;
	}

}
