package nl.digitalekabeltelevisie.data.mpeg.psi;
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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;


import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.util.Utils;


public class SITsection extends TableSectionExtendedSyntax{

	private final List<Descriptor> transmissionDescriptorList;
	private final List<Service> serviceList;
	private final int transmissionInfoLoopLength;

	public static class Service implements TreeNode{
		private int serviceID;
		private int reservedFutureUse;
		private int runningStatus;
		private int serviceLoopLength;
		private List<Descriptor> descriptorList;

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}


		@Override
		public String toString(){
			StringBuilder b = new StringBuilder("Service, serviceID=");
			b.append(serviceID).append(", reservedFutureUse=").append(reservedFutureUse).append(", ");
			for (Descriptor d : descriptorList) {
				b.append(d).append(", ");
			}
			return b.toString();

		}
		
		@Override
		public KVP getJTreeNode(int modus) {

			KVP t = new KVP("transport_stream:", serviceID);

			t.add(new KVP("service_id", serviceID));
			t.add(new KVP("original_network_id", reservedFutureUse, Utils.getOriginalNetworkIDString(reservedFutureUse)));
			t.add(new KVP("service_loop_length", serviceLoopLength));
			addListJTree(t, descriptorList, modus, "transport_descriptors");
			return t;
		}

		public int getServiceID() {
			return serviceID;
		}

		public void setServiceID(int serviceID) {
			this.serviceID = serviceID;
		}

		public int getReservedFutureUse() {
			return reservedFutureUse;
		}

		public void setReservedFutureUse(int reservedFutureUse) {
			this.reservedFutureUse = reservedFutureUse;
		}

		public int getRunningStatus() {
			return runningStatus;
		}

		public void setRunningStatus(int runningStatus) {
			this.runningStatus = runningStatus;
		}

		public int getServiceLoopLength() {
			return serviceLoopLength;
		}

		public void setServiceLoopLength(int serviceLoopLength) {
			this.serviceLoopLength = serviceLoopLength;
		}
	}

	public SITsection(PsiSectionData raw_data, PID parent){
		super(raw_data,parent);
		transmissionInfoLoopLength = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_12BITS);
		transmissionDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),10,transmissionInfoLoopLength,this);
		serviceList = buildServiceList(raw_data.getData(), 10+transmissionInfoLoopLength,  sectionLength - transmissionInfoLoopLength - 15);
	}


	@Override
	public String toString(){
		StringBuilder b = new StringBuilder("SITsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=").append(getTableType(tableId)).append(", ");

		return b.toString();
	}


	private List<Service> buildServiceList(byte[] data, int start, int len ) {
		ArrayList<Service> r = new ArrayList<>();
		int t =0;
		while(t<len){
			int serviceId = Utils.getInt(data, start+t, 2, Utils.MASK_16BITS);
			int dvbReservedFutureUse = (Utils.getInt(data, start+t+2, 1, 0x80)>>7);
			int runningStatus = (Utils.getInt(data, start+t+2, 1, 0x70) >> 4);
			int serviceLoopLength = Utils.getInt(data, start+t+2, 2, Utils.MASK_12BITS);
			List<Descriptor> descriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),10,serviceLoopLength,this);
			Service c = new Service();
			c.setServiceID(serviceId);
			c.setReservedFutureUse(dvbReservedFutureUse);
			c.setRunningStatus(runningStatus);
			c.setServiceLoopLength(serviceLoopLength);
			c.setDescriptorList(descriptorList);

			r.add(c);
			t += 4 + serviceLoopLength;
		}
		return r;
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("transmission_info_loop_length", transmissionInfoLoopLength));
		addListJTree(t, transmissionDescriptorList, modus, "transmission_descriptors");
		addListJTree(t, serviceList, modus, "transport_stream_loop");
		return t;
	}

	public List<Descriptor> getTransmissionDescriptorList() {
		return transmissionDescriptorList;
	}


	public List<Service> getServiceList() {
		return serviceList;
	}


	public int getTransmissionInfoLoopLength() {
		return transmissionInfoLoopLength;
	}


}
