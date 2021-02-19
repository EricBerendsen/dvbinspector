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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;


public class SDTsection extends TableSectionExtendedSyntax{

	private List<Service> serviceList;
	private int originalNetworkID;
	public class Service implements TreeNode{
		private int serviceID;
		private int reserved;
		private int eitScheduleFlag;
		private int eitPresentFollowingFlag;
		private int runningStatus;
		private int freeCAmode;


		private int serviceDescriptorsLength;

		private List<Descriptor> descriptorList;

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(final List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}

		public int getServiceDescriptorsLength() {
			return serviceDescriptorsLength;
		}

		public void setServiceDescriptorsLength(final int transportDescriptorsLength) {
			this.serviceDescriptorsLength = transportDescriptorsLength;
		}

		public int getServiceID() {
			return serviceID;
		}

		public void setServiceID(final int transportStreamID) {
			this.serviceID = transportStreamID;
		}

		@Override
		public String toString(){
			final StringBuilder b = new StringBuilder("Service, serviceID=");
			b.append(getServiceID()).append(", reserved_future_use=").append(getReserved()).append(", EIT_schedule_flag=");
			b.append(getEitScheduleFlag()).append(", EIT_present_following_flag=").append(getEitPresentFollowingFlag()).append(", running_status=");
			b.append(getRunningStatus()).append(", free_CA_mode=").append(getFreeCAmode()).append(", descriptors_loop_length=").append(getServiceDescriptorsLength()).append(",descriptors=");
			for (Descriptor d : descriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}
		public DefaultMutableTreeNode getJTreeNode(final int modus){

			String s = getPSI().
					getSdt().getServiceNameDVBString(originalNetworkID, tableIdExtension, serviceID).map(DVBString::toString).
					orElse("Service " + serviceID);
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("service (" +s+")"));

			t.add(new DefaultMutableTreeNode(new KVP("service_id",serviceID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_future_use",reserved,null)));
			t.add(new DefaultMutableTreeNode(new KVP("EIT_schedule_flag",eitScheduleFlag,getEitScheduleFlagString(eitScheduleFlag))));
			t.add(new DefaultMutableTreeNode(new KVP("EIT_present_following_flag",eitPresentFollowingFlag,getEitPresentFollowingFlagString(eitPresentFollowingFlag))));
			t.add(new DefaultMutableTreeNode(new KVP("running_status",runningStatus,getRunningStatusString(runningStatus))));
			t.add(new DefaultMutableTreeNode(new KVP("free_CA_mode",freeCAmode,getFreeCAmodeString(freeCAmode))));
			t.add(new DefaultMutableTreeNode(new KVP("service_descriptors_length",getServiceDescriptorsLength(),null)));

			Utils.addListJTree(t,descriptorList,modus,"service_descriptors");

			return t;
		}

		public int getEitPresentFollowingFlag() {
			return eitPresentFollowingFlag;
		}

		public void setEitPresentFollowingFlag(final int eitPresentFollowingFlag) {
			this.eitPresentFollowingFlag = eitPresentFollowingFlag;
		}

		public int getEitScheduleFlag() {
			return eitScheduleFlag;
		}

		public void setEitScheduleFlag(final int eitScheduleFlag) {
			this.eitScheduleFlag = eitScheduleFlag;
		}

		public int getFreeCAmode() {
			return freeCAmode;
		}

		public void setFreeCAmode(final int freeCAmode) {
			this.freeCAmode = freeCAmode;
		}

		public int getRunningStatus() {
			return runningStatus;
		}

		public void setRunningStatus(final int runningStatus) {
			this.runningStatus = runningStatus;
		}

		public int getReserved() {
			return reserved;
		}

		public void setReserved(final int reserved) {
			this.reserved = reserved;
		}

	}



	public SDTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);

		originalNetworkID = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_16BITS);
		serviceList = buildServicesList(raw_data.getData(), 11, sectionLength -12);
	}


	public int getTransportStreamID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("SDTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=").append(getTableType(tableId)).append(", TransportStreamID=").append(getTransportStreamID()).append(", OriginalNetworkID=").append(getOriginalNetworkID()).append(", ");



		return b.toString();
	}


	public List<Service> getServiceList() {
		return serviceList;
	}


	public void setServiceList(
			final List<Service> transportStreamList) {
		this.serviceList = transportStreamList;
	}


	public int noServices() {
		return serviceList.size();
	}

	private List<Service> buildServicesList(final byte[] data, final int offset, final int programInfoLength) {
		final ArrayList<Service> r = new ArrayList<>();
		int t =0;
		while(t<programInfoLength){
			final Service c = new Service();
			c.setServiceID(Utils.getInt(data, offset+t, 2, 0xFFFF));
			c.setReserved(Utils.getInt(data, offset+t+2, 1, 0xFC)>>2);
			c.setEitScheduleFlag(Utils.getInt(data, offset+t+2, 1, 0x02)>>1);
			c.setEitPresentFollowingFlag(Utils.getInt(data, offset+t+2, 1, 0x01));
			c.setRunningStatus(Utils.getInt(data, offset+t+3, 1, 0xE0)>>5);
			c.setFreeCAmode(Utils.getInt(data, offset+t+3, 1, 0x10)>>4);
			c.setServiceDescriptorsLength(Utils.getInt(data, offset+t+3, 2, 0x0FFF));

			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,offset+t+5,c.getServiceDescriptorsLength(),this));
			t+=5+c.getServiceDescriptorsLength();
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		((KVP) t.getUserObject()).setTableSource(this::getTableModel);

		t.add(new DefaultMutableTreeNode(new KVP("original_network_id",originalNetworkID,Utils.getOriginalNetworkIDString(originalNetworkID))));

		Utils.addListJTree(t,serviceList,modus,"services_loop");

		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "transport_stream_id";
	}
    
	public int getOriginalNetworkID() {
		return originalNetworkID;
	}


	public void setOriginalNetworkID(final int originalNetworkID) {
		this.originalNetworkID = originalNetworkID;
	}

	public static String getEitScheduleFlagString(final int eitScheduleFlag) {

		switch (eitScheduleFlag) {
		case 0: return "No EIT schedule information";
		case 1: return "EIT schedule information present in TS";

		default:
			return "Illegal value";
		}

	}

	public static String getEitPresentFollowingFlagString(final int eitPresentFollowingFlag) {

		switch (eitPresentFollowingFlag) {
		case 0: return "No EIT_present_following information";
		case 1: return "EIT_present_following information present in TS";

		default:
			return "Illegal value";
		}
	}
	
	public TableModel getTableModel() {
		FlexTableModel<SDTsection,Service> tableModel =  new FlexTableModel<>(SDT.buildSdtTableHeader());
		
		tableModel.addData(this,getServiceList());
		tableModel.process();
		return tableModel;
	}
}
