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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class LinkageDescriptor extends Descriptor {

	private static Logger logger = Logger.getLogger(LinkageDescriptor.class.getName());

	private List<OUIEntry> ouiList = new ArrayList<OUIEntry>();
	private List<Platform> platformList = new ArrayList<Platform>();
	private List<NordigBootLoader> bootLoaderList = new ArrayList<NordigBootLoader>();

	public static class OUIEntry implements TreeNode{

		private final int oui;
		private final int selectorLength;
		private final byte[] selectorByte;


		public OUIEntry(final int oui, final int selectorLength, final byte[] selectorByte) {
			super();
			this.oui = oui;
			this.selectorLength = selectorLength;
			this.selectorByte = selectorByte;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("OUI"));
			s.add(new DefaultMutableTreeNode(new KVP("oui",oui,Utils.getOUIString(oui))));
			s.add(new DefaultMutableTreeNode(new KVP("selector_length",selectorLength,null)));
			s.add(new DefaultMutableTreeNode(new KVP("selector_bytes",selectorByte,null)));
			return s;
		}

	}


	public static class Platform implements TreeNode{
		/**
		 *
		 */
		private final int platformId;
		private List<PlatformName> platformNameList = new ArrayList<PlatformName>();

		public Platform(final int pID){
			platformId = pID;
		}
		public void addPlatformName(final PlatformName s){
			platformNameList.add(s);
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("platforms"));
			s.add(new DefaultMutableTreeNode(new KVP("platform_id",platformId,getPlatformIDString(platformId))));
			addListJTree(s,platformNameList,modus,"platform_name_loop");
			return s;
		}
	}

	public static class PlatformName implements TreeNode{

		private final String iso639LanguageCode;
		private final DVBString platformName;

		public PlatformName(final String lCode, final DVBString pName){
			iso639LanguageCode = lCode;
			platformName = pName;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("platform_name"));
			s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("platform_name_length",platformName.getLength() ,null)));
			s.add(new DefaultMutableTreeNode(new KVP("platform_name",platformName ,null)));
			return s;
		}
	}

	public static class NordigBootLoader implements TreeNode{

		/**
		 * @param manufacturer_id
		 * @param version_id
		 * @param private_id
		 * @param start_time
		 */
		private NordigBootLoader(int manufacturer_id, byte[] version_id,
				long private_id, byte[] start_time) {
			super();
			this.manufacturer_id = manufacturer_id;
			this.version_id = version_id;
			this.private_id = private_id;
			this.start_time = start_time;
		}

		private final int manufacturer_id;
		private final byte[] version_id; // 64 bits, 8 bytes
		private final long private_id;
		private final byte[] start_time;

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("bootloader"));
			s.add(new DefaultMutableTreeNode(new KVP("manufacturer_id",manufacturer_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("version_id",version_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("private_id",private_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("start_time",start_time,Utils.getUTCFormattedString(start_time))));
			return s;
		}
	}

	private int transportStreamId;
	private int originalNetworkId;
	private final int serviceId;
	private final int linkageType;

	// linkage type 8

	private int hand_over_type;
	private int origin_type;
	private int network_id;
	private int initial_service_id;

	// linkage type 9
	private int OUI_data_length;
	// linkage type 0x0a
	private int tableType;
	private byte[] privateDataByte;

	// linkage type 0x0b

	private int platformIdDataLength;

	// linkage type 0x0c
	private int bouquetID;

	// TODO handle linkage_type ==0x05
	// TODO handle linkage_type ==0x0D 300468

	// linkage type=09 is software update, oui = http://standards.ieee.org/regauth/oui/oui.txt


	public LinkageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		transportStreamId = getInt(b,offset+2,2,MASK_16BITS);
		originalNetworkId = getInt(b,offset+4,2,MASK_16BITS);
		serviceId =  getInt(b,offset+6,2,MASK_16BITS);
		linkageType = getInt(b,offset+8,1,MASK_8BITS);
		if(linkageType==0x08){ // mobile hand-over
			int r =0;
			hand_over_type = getInt(b,offset+9,1,0xF0)>>>4;
			origin_type = getInt(b,offset+9,1,MASK_1BIT);
			if((hand_over_type ==0x01)
					|| (hand_over_type ==0x02)
					|| (hand_over_type ==0x03)){
				network_id = getInt(b,offset+10,2,MASK_16BITS);
				r+=2;
			}
			if (origin_type ==0x00){
				initial_service_id = getInt(b,offset+10+r,2,MASK_16BITS);
				r+=2;
			}
			privateDataByte = Utils.copyOfRange(b, offset+10+r, offset+descriptorLength+2);
		}else if(linkageType==0x09){ //System Software Update Service (TS 102 006)
			OUI_data_length = getInt(b,offset+9,1,MASK_8BITS);
			int r =0;
			while (r<OUI_data_length) {
				final int oui = getInt(b,offset+ 10+r, 3, Utils.MASK_24BITS);
				final int selectorLength= getInt(b, offset+r+13, 1, MASK_8BITS);
				final byte[] selector_byte = Utils.copyOfRange(b, offset+r+14, offset+r+14+selectorLength);

				final OUIEntry ouiEntry = new OUIEntry(oui,selectorLength,selector_byte);
				ouiList.add(ouiEntry);
				r=r+4+selectorLength;
			}
			privateDataByte = Utils.copyOfRange(b, offset+10+r, offset+descriptorLength+2);
		}else if(linkageType==0x0a){ // TS containing SSU BAT or NIT (TS 102 006)
			tableType =	getInt(b,offset+9,1,MASK_8BITS);
			privateDataByte = Utils.copyOfRange(b, offset+10, offset+descriptorLength+2);
		}else if(linkageType==0x0b){ // IP/MAC Notification Table
			platformIdDataLength =	getInt(b,offset+9,1,MASK_8BITS);
			int r =0;
			while (r<platformIdDataLength) {
				final int platform_id = getInt(b,offset+ 10+r, 3, Utils.MASK_24BITS);
				final Platform p = new Platform(platform_id);
				platformList.add(p);
				final int platform_name_loop_length= getInt(b, offset+r+13, 1, MASK_8BITS);
				int t=0;
				while(t<platform_name_loop_length){
					final String languageCode=getISO8859_1String(b, offset+r+t+14, 3);
					final DVBString platformName = new DVBString(b,offset+r+t+17);
					final PlatformName pName = new PlatformName(languageCode, platformName);
					p.addPlatformName(pName);
					t+=4+platformName.getLength();
				}
				r+=t+4;
			}
			privateDataByte = Utils.copyOfRange(b, offset+10+r, offset+descriptorLength+2);
		}else if(linkageType==0x0c){ // TS containing SSU BAT or NIT (TS 102 006)
			tableType =	getInt(b,offset+9,1,MASK_8BITS);
			if(tableType==0x02){
				bouquetID = getInt(b,offset+10,2,MASK_16BITS);
				privateDataByte = Utils.copyOfRange(b, offset+12, offset+descriptorLength+2);
			}else{
				privateDataByte = Utils.copyOfRange(b, offset+10, offset+descriptorLength+2);
			}

		}else if(linkageType==0x81){ // 13.2.6 NorDig linkage for bootloader
			// TODO, this is a private usage, but not indicated by a private_data_specifier_descriptor: 0x5F
			// just assume it is nordig?
			int s = 9; // private data, if any, starts at position 9
			while((s+17) <= descriptorLength){ // bootloader = 19 bytes
				int manufacturer_id = getInt(b,offset+s,2,MASK_16BITS);
				byte[] version_id = Utils.copyOfRange(b, offset+s+2, offset+s+10); // 64 bits, 8 bytes
				long private_id = getLong(b, offset+s+10, 4, MASK_32BITS);
				byte[] start_time =  Utils.copyOfRange(b, offset+s+14, offset+s+19);
				final NordigBootLoader nordigBootLoader = new NordigBootLoader(manufacturer_id, version_id, private_id, start_time);
				bootLoaderList.add(nordigBootLoader);
				s +=19;
			}
			privateDataByte = Utils.copyOfRange(b, offset+s, offset+descriptorLength+2);

		}else{
			logger.log(Level.INFO,"LinkageDescriptor, not implemented linkageType: "+linkageType +"("+getLinkageTypeString(linkageType)+"), called from:"+parent);
			privateDataByte = Utils.copyOfRange(b, offset+9, offset+descriptorLength+2);

		}
	}

	@Override
	public String toString() {
		return super.toString() + "transportStreamId="+transportStreamId;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamId ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("original_network_id",originalNetworkId ,Utils.getOriginalNetworkIDString(originalNetworkId))));
		t.add(new DefaultMutableTreeNode(new KVP("service_id",serviceId ,parentTableSection.getParentPID().getParentTransportStream().getPsi().getSdt().getServiceName(serviceId))));
		t.add(new DefaultMutableTreeNode(new KVP("linkage_type",linkageType ,getLinkageTypeString(linkageType))));
		if(linkageType==0x08){
			t.add(new DefaultMutableTreeNode(new KVP("hand-over_type",hand_over_type ,getHandOverString(hand_over_type))));
			t.add(new DefaultMutableTreeNode(new KVP("origin_type",origin_type ,getOriginTypeString(origin_type))));
			if((hand_over_type ==0x01)
					|| (hand_over_type ==0x02)
					|| (hand_over_type ==0x03)){
				t.add(new DefaultMutableTreeNode(new KVP("network_id",network_id ,getParentPID().getParentTransportStream().getPsi().getNit().getNetworkName(network_id))));
			}
			if (origin_type ==0x00){
				t.add(new DefaultMutableTreeNode(new KVP("initial_service_id",initial_service_id ,null)));
			}

		}else if(linkageType==0x09){
			addListJTree(t,ouiList,modus,"Systems Software Update");
		}else if(linkageType==0x0a){
			t.add(new DefaultMutableTreeNode(new KVP("table_type",tableType ,getTableTypeString(tableType))));
		}else if(linkageType==0x0b){
			addListJTree(t,platformList,modus,"platform_list");
		}else if(linkageType==0x0c){
			t.add(new DefaultMutableTreeNode(new KVP("table_type",tableType ,getTableTypeString(tableType))));
			if(tableType==0x02){
				t.add(new DefaultMutableTreeNode(new KVP("bouquet_id",bouquetID ,null)));
			}
		}else if(linkageType==0x81){
			addListJTree(t,bootLoaderList,modus,"Nordig BootLoader");
		}
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",privateDataByte ,null)));
		return t;
	}

	public int getOriginalNetworkId() {
		return originalNetworkId;
	}


	public void setOriginalNetworkId(final int caPID) {
		this.originalNetworkId = caPID;
	}

	public int getTransportStreamId() {
		return transportStreamId;
	}

	public void setTransportStreamId(final int caSystemID) {
		this.transportStreamId = caSystemID;
	}

	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}

	public void setPrivateDataByte(final byte[] privateDataByte) {
		this.privateDataByte = privateDataByte;
	}

	public static String getLinkageTypeString(final int linkageType) {
		switch (linkageType) {
		case 0x00 : return "reserved for future use";
		case 0x01 : return "information service";
		case 0x02 : return "EPG service";
		case 0x03 : return "CA replacement service";
		case 0x04 : return "TS containing complete Network/Bouquet SI";
		case 0x05 : return "service replacement service";
		case 0x06 : return "data broadcast service";
		case 0x07 : return "RCS Map";
		case 0x08 : return "mobile hand-over";
		case 0x09 : return "System Software Update Service";
		case 0x0A : return "TS containing SSU BAT or NIT";
		case 0x0B : return "IP/MAC Notification Service";
		case 0x0C : return "TS containing INT BAT or NIT";

		case 0x81 : return "linkage to NorDig bootloader";
		case 0x82 : return "Undocumented: linkage to Ziggo software update"; // or NorDig Simulcast replacement service.

		case 0xA0 : return "link to OpenTV VOD service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0xA6 : return "link to OpenTV ITV service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0xA7 : return "link to WEB service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf

		case 0xFF : return "reserved for future use";

		default:
			if((0x0D<=linkageType)&&(linkageType<=0x7F )){return "reserved for future use";}
			if((0x80<=linkageType)&&(linkageType<=0xFE )){return "user defined";}

			return "Illegal value";

		}
	}


	public static String getTableTypeString(final int tableType) {
		switch (tableType) {
		case 0x00 : return "not defined";
		case 0x01 : return "NIT";
		case 0x02 : return "BAT";

		default:

			return "reserved for future use";

		}
	}

	public static String getHandOverString(final int handOver) {
		switch (handOver) {
		case 0x00 : return "reserved for future use";
		case 0x01 : return "DVB hand-over to an identical service in a neighbouring country";
		case 0x02 : return "DVB hand-over to a local variation of the same service";
		case 0x03 : return "DVB hand-over to an associated service";
		default:
			return "reserved for future use";
		}
	}
	public static String getOriginTypeString(final int originType) {
		switch (originType) {
		case 0x00 : return "NIT";
		case 0x01 : return "SDT";

		default:

			return "illegal value";

		}
	}

	public int getBouquetID() {
		return bouquetID;
	}

	public int getHand_over_type() {
		return hand_over_type;
	}

	public int getInitial_service_id() {
		return initial_service_id;
	}

	public int getLinkageType() {
		return linkageType;
	}

	public int getNetwork_id() {
		return network_id;
	}

	public int getOrigin_type() {
		return origin_type;
	}

	public int getOUI_data_length() {
		return OUI_data_length;
	}

	public List<OUIEntry> getOuiList() {
		return ouiList;
	}

	public int getPlatformIdDataLength() {
		return platformIdDataLength;
	}

	public List<Platform> getPlatformList() {
		return platformList;
	}

	public int getServiceId() {
		return serviceId;
	}

	public int getTableType() {
		return tableType;
	}

}
