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

package nl.digitalekabeltelevisie.data.mpeg;

import static nl.digitalekabeltelevisie.util.Utils.MASK_12BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DataBroadcastIDDescriptor.OUIEntry;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.*;
import nl.digitalekabeltelevisie.util.PreferencesManager;
import nl.digitalekabeltelevisie.util.Utils;


/**
 *
 * @author Eric
 *
 */
public class PsiSectionData {

	private byte [] data ;
	private int noBytes=0;
	private int packet_no=0;
	final TransportStream transportStream;
	private final PID parentPID;

	private boolean complete = false;
	
	

	private static final Logger logger = Logger.getLogger(PsiSectionData.class.getName());


	public PsiSectionData(final PID parent, final int packetNo,final TransportStream transportStream) {
		this.parentPID = parent;
		this.packet_no=packetNo;
		this.transportStream=transportStream;
		this.data= new byte[3];
	}

	public int readBytes(final byte [] payload, final int offset, final int len){
		int available = len;

		int need=0;
		int read1=0; // bytes read to get pre-amble
		int read2=0;// bytes read for main part

		if(noBytes<3){
			need = 3-noBytes; // how many more we need to get 3
			read1=Math.min(need,available); // we are going to read this number of bytes
			System.arraycopy(payload, offset, data, noBytes, read1);
			noBytes+=read1; // now we have read1 bytes more.
			available -= read1;// now we have read1 bytes less available .
			if(noBytes==3){
				final int section_length= getInt(data, 1, 2, MASK_12BITS);
				byte[] tmp = new byte[section_length+3];
				System.arraycopy(data,0,tmp,0,3);
				data=tmp;
				tmp=null;
			}
		}
		if(noBytes>=3){
			int section_length= getInt(data, 1, 2, MASK_12BITS);
			final int pid = parentPID.getPid();
			if(section_length>4093){
				logger.warning("PSi Section Data, section_length>4093 section_length="+section_length+", pid="+pid);
				section_length=4093;
			}
			need = (section_length +3) - noBytes; //section_length + 3 pre-amble - what we already got
			read2=Math.min(need,available); // we are going to read this number of bytes
			System.arraycopy(payload, offset+read1, data, noBytes, read2);
			noBytes+=read2; // now we have read2 bytes more.
			if(read2==need){
				// complete SI section, handle it in PSI
				updatePSI(pid);

				// now put it in general PID table
				// when it is not valid an exception will be thrown, caught and ignored. The section will be discarded

				if(PreferencesManager.isEnableGenericPSI()) {
        				try {
        					final TableSection psi= new TableSection(this,parentPID);
        					parentPID.getPsi().update(psi);
        				} catch (final RuntimeException re) {
        					logger.log(Level.WARNING, "RuntimeException in readBytes PIDs: pid="+pid, re);
        				}
				}
				complete=true;
			}
		}
		return(read1+read2);
	}

	/**
	 * @param pid
	 */
	private void updatePSI(final int pid) {
		try {
			if(pid==0){
				transportStream.getPsi().getPat().update(new PATsection(this,parentPID));
			} else {

				final int tableId = Byte.toUnsignedInt(data[0]);
				if((tableId==0x02)&&
						(transportStream.getPsi().getPat().inPAT(pid))){
					transportStream.getPsi().getPmts().update(new PMTsection(this,parentPID));
				}else if((tableId==0x01)&&(pid==0x01)){
					transportStream.getPsi().getCat().update(new CAsection(this,parentPID));
				}else if(pid==0x10){  // NIT
					transportStream.getPsi().getNit().update(new NITsection(this,parentPID));
				}else if((tableId==0x4A)&&(pid==0x11)){
					transportStream.getPsi().getBat().update(new BATsection(this,parentPID));
				}else if((0x4E<=tableId)&&(tableId<=0x6F)&&(pid==0x12)){
					transportStream.getPsi().getEit().update(new EITsection(this,parentPID));
				}else if((pid==0x14) &&(tableId==0x70)){
					transportStream.getPsi().getTdt().update(new TDTsection(this,parentPID));
				}else if((pid==0x14) &&(tableId==0x73)){
					transportStream.getPsi().getTot().update(new TOTsection(this,parentPID));
				}else if((pid==0x11) &&((tableId==0x42)||(tableId==0x46))){
					transportStream.getPsi().getSdt().update(new SDTsection(this,parentPID));
				}else if((pid==0x1F) &&(tableId==0x7F)){
					transportStream.getPsi().getSit().update(new SITsection(this,parentPID));
				}else if((tableId==0x4c)&&isINTSection(pid)){ // check for linkage descriptors 0x0B located in the NIT  //ETSI EN 301 192 V1.4.2
					transportStream.getPsi().getInt().update(new INTsection(this,parentPID));
				}else if((tableId==0x4b)&&isUNTSection(pid)){
					transportStream.getPsi().getUnts().update(new UNTsection(this,parentPID));
				}else if((tableId==0x74)&&isAITSection(pid)){
					transportStream.getPsi().getAits().update(new AITsection(this,parentPID));
				}else if((tableId==0x76)&&isRCTSection(pid)){
					transportStream.getPsi().getRcts().update(new RCTsection(this,parentPID));
				}else if((tableId==0xFC)&&isSpliceInfoSection(pid)){
					transportStream.getPsi().getScte35_table().update(new SpliceInfoSection(this,parentPID));
				}else if(isDIFTSection(pid)) { // no check for table ID, as this might change 
					transportStream.getPsi().getDfit_table().update(new DFITSection(this, parentPID));
				}else if((tableId>=0x37)&&(tableId<=0x3F)){
					// also include all PES streams component (ISO/IEC 13818-6 type B) which
					// do not have a data_broadcast_id_descriptor associated with it,
					// but do have a Association_tag_descriptor (or a stream_identifier_descriptor)
					// These might be referenced from DSI in other stream (or even from multiple)
					// Also, include PMTs to store the stream_identifier_descriptor
					// all handled in DSMCCs.
					if(PreferencesManager.isEnableDSMCC()) {
						transportStream.getPsi().getDsms().update(new TableSectionExtendedSyntax(this,parentPID));
					}
				}else if(PreferencesManager.isEnableM7Fastscan()) {
					if(tableId== 0xBC){
						transportStream.getPsi().getM7fastscan().update(new FNTsection(this, parentPID));
					}else if(tableId== 0xBD) {
						transportStream.getPsi().getM7fastscan().update(new FSTsection(this, parentPID));
					}else if((tableId== 0xBE) && transportStream.isONTSection(pid)) {
						transportStream.getPsi().getM7fastscan().update(new ONTSection(this, parentPID));
					}
				}
			}
		} catch (final RuntimeException re) {
			logger.log(Level.WARNING, "RuntimeException in updatePSI PSI data: pid="+pid, re);
		}
	}

	private boolean isSpliceInfoSection(int pid) {
		
		final Map<Integer, PMTsection[]> pmtList = transportStream.getPsi().getPmts().getPmts();

		for (final PMTsection[] pmtSections : pmtList.values()){
			final PMTsection pmt=pmtSections[0]; // PMT always one section
			// The registration descriptor shall be carried in the program_info loop of the PMT
			if(hasSCTE35RegistrationDescriptor(pmt.getDescriptorList())){
				for(final Component component : pmt.getComponentenList() ){
					if((component.getElementaryPID()==pid) && (component.getStreamtype()==0x86)){
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean hasSCTE35RegistrationDescriptor(final List<Descriptor> componentDescriptorList) {
		final List<RegistrationDescriptor> registration_descriptors = Descriptor.findGenericDescriptorsInList(componentDescriptorList, RegistrationDescriptor.class);
		for(RegistrationDescriptor registrationDescriptor:registration_descriptors){
			final byte[] formatIdentifier = registrationDescriptor.getFormatIdentifier();
			if(Utils.equals(formatIdentifier, 0, formatIdentifier.length,RegistrationDescriptor.SCTE_35,0,RegistrationDescriptor.SCTE_35.length)){
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the position of the first TSpacket of this PSISection in the TransportStream
	 */

	public int getPacket_no() {
		return packet_no;
	}
	
	private boolean isINTSection(final int pid){
		final List<LinkageDescriptor> linkageDescriptors = transportStream.getLinkageDescriptorsFromNitNetworkLoop();

		for (final LinkageDescriptor ld : linkageDescriptors) {
			if(ld.getLinkageType()==0x0B){
				final int linkedService = ld.getServiceId();
				final int streamId = ld.getTransportStreamId();
				if(streamId==transportStream.getStreamID()){ // current stream
					final PMTsection pmt = transportStream.getPsi().getPmts().getPmt(linkedService);
					final List<PMTsection.Component> componentenList = pmt.getComponentenList();
					for( final PMTsection.Component c: componentenList){
						if(c.getElementaryPID()==pid){
							final List<DataBroadcastIDDescriptor> databroadcastIdDescriptors = Descriptor.findGenericDescriptorsInList(c.getComponentDescriptorList(), DataBroadcastIDDescriptor.class);
							for(final DataBroadcastIDDescriptor databroadcastDesc: databroadcastIdDescriptors){
								if(databroadcastDesc.getDataBroadcastId()==0x000B){
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 *
	 *  is this PID a candidate to contain a UNT (Updata Notification Table)?
	 *  the tabletype is already checked by caller (0x4b) TableType: SSU Update Notification Table
	 *  now look through all PMTs looking for a component that refers to this pid and has a Descriptor: data_broadcast_id_descriptor: 0x66 (102)
	 *  with data_broadcast_id: 0xA (10) => System Software Update service [TS 102 006]
	 *  and update_type is set to the value 0x2 or 0x3.

	 * @param pid
	 * @return true if this PID contains the UNT
	 */
	public boolean isUNTSection(final int pid){
		final Map<Integer, PMTsection[]> pmtList = transportStream.getPsi().getPmts().getPmts();

		for (final PMTsection[] pmt : pmtList.values()){
			final PMTsection p=pmt[0]; // PMT always one section
			for(final Component component : p.getComponentenList() ){
				if(component.getElementaryPID()==pid){
					final List<DataBroadcastIDDescriptor> data_broadcast_id_descriptors = Descriptor.findGenericDescriptorsInList(component.getComponentDescriptorList(), DataBroadcastIDDescriptor.class);
					for (final DataBroadcastIDDescriptor dataBroadCastIDDescriptor : data_broadcast_id_descriptors) {
						if(dataBroadCastIDDescriptor.getDataBroadcastId()==0x0A){
							//get all  OUIs (prob 1) and check update type
							for(final OUIEntry oui: dataBroadCastIDDescriptor.getOuiList()){
								final int updateType = oui.getUpdateType();
								if((updateType==0x2)||(updateType==0x3)){
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 *
	 *  is this PID a candidate to contain a AIT ( Application Information Table)
	 *  the tabletype is already checked by caller  table_id: 0x74 (116) => MHP-application information section (AIT)
	 *  now look through all PMTs looking for a component that refers to this pid and has a Descriptor: application_signalling_descriptor: 0x6F (111)

	 * @param pid
	 * @return true if this PID contains a AIT
	 */
	public boolean isAITSection(final int pid){
		final Map<Integer, PMTsection[]> pmtList = transportStream.getPsi().getPmts().getPmts();

		for (final PMTsection[] pmt : pmtList.values()){
			final PMTsection p=pmt[0]; // PMT always one section
			for(final Component component : p.getComponentenList() ){
				if(component.getElementaryPID()==pid){
					final List<ApplicationSignallingDescriptor> application_signalling_descriptors = Descriptor.findGenericDescriptorsInList(component.getComponentDescriptorList(), ApplicationSignallingDescriptor.class);
					if(application_signalling_descriptors.size()>0){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 *
	 *  is this PID a candidate to contain a DFIT ( Downloadable Font Information Table)
	 *  the tabletype is not  checked by caller  because it might change
	 *  now look through all PMTs looking for a component that refers to this pid and has a Descriptor: application_signalling_descriptor: 0x6F (111)

	 * @param pid
	 * @return true if this PID contains a AIT
	 */
	public boolean isDIFTSection(final int pid){
		final Map<Integer, PMTsection[]> pmtList = transportStream.getPsi().getPmts().getPmts();

		for (final PMTsection[] pmt : pmtList.values()){
			final PMTsection p=pmt[0]; // PMT always one section
			for(final Component component : p.getComponentenList() ){
				if(component.getElementaryPID()==pid){
					final List<DataBroadcastIDDescriptor> databroadcatsid_descriptors = Descriptor.findGenericDescriptorsInList(component.getComponentDescriptorList(), DataBroadcastIDDescriptor.class);
					if(databroadcatsid_descriptors.size()>0){
						for(DataBroadcastIDDescriptor dataBroadcastIDDescriptor:databroadcatsid_descriptors) {
							if(dataBroadcastIDDescriptor.getDataBroadcastId()== 0x0D) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}


	/**
	 *
	 *  is this PID a candidate to contain a RCT
	 *  the tabletype is already checked by caller  table_id: 0x76 (118) => TVA-related content section (RCT)
	 *  now look through all PMTs looking for a component that refers to this pid and has a Descriptor: Descriptor: related_content_descriptor [0]: 0x74 (116)

	 * @param pid
	 * @return true if this PID contains a RCT
	 */
	public boolean isRCTSection(final int pid){
		final Map<Integer, PMTsection[]> pmtList = transportStream.getPsi().getPmts().getPmts();

		for (final PMTsection[] pmt : pmtList.values()){
			final PMTsection p=pmt[0]; // PMT always one section
			for(final Component component : p.getComponentenList() ){
				if(component.getElementaryPID()==pid){
					final List<RelatedContentDescriptor> application_signalling_descriptors = Descriptor.findGenericDescriptorsInList(component.getComponentDescriptorList(), RelatedContentDescriptor.class);
					if(application_signalling_descriptors.size()>0){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 *
	 *  is this PID a candidate to contain a DSM-CC
	 *  the tabletype is already checked by caller  table_id: > =0x38 && <=0x3F =>
	 *  now look through all PMTs looking for a component that refers to this pid and has a Descriptor: Data_broadcast_id: 0x66 (102)
	 *
	 *  // TODO also check data_broadcast_id == 0x06 (data carousel) Not implemented yet
	 *  now check for  0x07 (object carousel)
	 *  following are also object carousels
	 *  0x00f0	MHP Object
	 *  0x0106	MHEG5(The Digital Network)
	 *  0x0123	HBBTV Carousel
	 *  0x0150 	OIPF Object Carousel
	 *  0xBBB2 	BBG Object Caroussel

	 * @param pid
	 * @return true if this PID contains the DSM-CC
	 */
	public boolean isDSMSection(final int pid){
		final Map<Integer, PMTsection[]> pmtList = transportStream.getPsi().getPmts().getPmts();

		for (final PMTsection[] pmt : pmtList.values()){
			final PMTsection p=pmt[0]; // PMT always one section
			for(final Component component : p.getComponentenList() ){
				if(component.getElementaryPID()==pid){
					final List<DataBroadcastIDDescriptor> data_broadcast_id_descriptors = Descriptor.findGenericDescriptorsInList(component.getComponentDescriptorList(), DataBroadcastIDDescriptor.class);
					if(data_broadcast_id_descriptors.size()>0){
						// assume there is only one (should be!)
						final DataBroadcastIDDescriptor dataBroadcastIDDescriptor = data_broadcast_id_descriptors.get(0);
						// is the dataBroadcastId in this descriptor in the list of id that represent object carousels?
						if(Arrays.binarySearch(DataBroadcastIDDescriptor.OBJECT_CAROUSEL_BROADCASTID, dataBroadcastIDDescriptor.getDataBroadcastId())>=0){
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void setPacket_no(final int packet_no) {
		this.packet_no = packet_no;
	}

	public int getPid() {
		return parentPID.getPid();
	}



	public byte[] getData() {
		return data;
	}

	public void setData(final byte[] data) {
		this.data = data;
	}

	public int getNoBytes() {
		return noBytes;
	}

	public void setNoBytes(final int noBytes) {
		this.noBytes = noBytes;
	}

	public boolean isComplete() {
		return complete;
	}

	public int getTableId(){
		return getInt(data, 0, 1, MASK_8BITS);
	}

	public int getTableIdExtension(){
		return getInt(data,3,2,MASK_16BITS);
	}
	public int getSectionNumber(){
		return getInt(data,6,1,MASK_8BITS);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = (PRIME * result) + (complete ? 1231 : 1237);
		result = (PRIME * result) + Arrays.hashCode(data);
		result = (PRIME * result) + noBytes;
		result = (PRIME * result) + ((parentPID == null) ? 0 : parentPID.hashCode());
		result = (PRIME * result) + ((transportStream == null) ? 0 : transportStream.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PsiSectionData other = (PsiSectionData) obj;

		if (!Arrays.equals(data, other.data)) {
			return false;
		}
		if (noBytes != other.noBytes) {
			return false;
		}
		if (parentPID == null) {
			if (other.parentPID != null) {
				return false;
			}
		} else if (!parentPID.equals(other.parentPID)) {
			return false;
		}
		if (transportStream == null) {
			if (other.transportStream != null) {
				return false;
			}
		} else if (!transportStream.equals(other.transportStream)) {
			return false;
		}
		return true;
	}

}
