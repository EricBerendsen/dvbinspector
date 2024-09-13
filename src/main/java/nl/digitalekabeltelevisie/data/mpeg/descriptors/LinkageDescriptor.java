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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.*;

public class LinkageDescriptor extends Descriptor {


	private static final Logger logger = Logger.getLogger(LinkageDescriptor.class.getName());

	private List<OUIEntry> ouiList = new ArrayList<>();
	private List<Platform> platformList = new ArrayList<>();
	private List<NordigBootLoader> bootLoaderList = new ArrayList<>();
	private List<BrandHomeTransponder> m7BrandHomeTransponderList = new ArrayList<>();

	public static record OUIEntry(int oui, int selectorLength, byte[] selectorByte) implements TreeNode{

		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP s = new KVP("OUI");
			s.add(new KVP("oui", oui).setDescription(getOUIString(oui)));
			s.add(new KVP("selector_length", selectorLength));
			s.add(new KVP("selector_bytes", selectorByte));
			return s;
		}

	}

	public static class Platform implements TreeNode{
		/**
		 *
		 */
		private final int platformId;
		private List<PlatformName> platformNameList = new ArrayList<>();

		public Platform(final int pID){
			platformId = pID;
		}
		public void addPlatformName(final PlatformName s){
			platformNameList.add(s);
		}


		@Override
		public KVP getJTreeNode(final int modus){
			final KVP s= new KVP("platforms");
			s.add(new KVP("platform_id",platformId).setDescription(getPlatformIDString(platformId)));
			addListJTree(s,platformNameList,modus,"platform_name_loop");
			return s;
		}
	}

	public static record PlatformName(String iso639LanguageCode, DVBString platformName) implements TreeNode {

		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP s = new KVP("platform_name");
			s.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			s.add(new KVP("platform_name", platformName, null));
			return s;
		}
	}

	public static record NordigBootLoader(int manufacturer_id, byte[] version_id, long private_id, byte[] start_time) implements TreeNode{

		@Override
		public KVP getJTreeNode(final int modus){
			final KVP s=new KVP("bootloader");
			s.add(new KVP("manufacturer_id",manufacturer_id));
			s.add(new KVP("version_id",version_id));
			s.add(new KVP("private_id",private_id));
			s.add(new KVP("start_time",start_time).setDescription(getUTCFormattedString(start_time)));
			return s;
		}
	}
	
	//EN 303 560 V1.1.1 (2018-05) 5.3.2.2.2
	
	public class FontInfo implements TreeNode{
		
		private int essential_font_download_flag;
		private int font_id;
		
		private FontInfo(int fontInfo) {
			essential_font_download_flag = (fontInfo & 0b1000_0000) >>7;
			font_id = fontInfo & 0b0111_1111;
		}

		@Override
		public KVP getJTreeNode(int modus) {
			final KVP s = new KVP("Donwloadable Font");
			s.add(new KVP("essential_font_download_flag", essential_font_download_flag).setDescription(
					essential_font_download_flag == 1 ? "font is required to present these subtitles" : "font is a supplementary font"));
			s.add(new KVP("font_id", font_id));
			return s;
		}

	}
	
	public record BrandHomeTransponder(int operator_network_id, int operator_sublist_id, int home_transport_stream_id, int home_original_network_id,
			int homebckp_transport_stream_id, int homebckp_original_network_id, int fst_pid, int fst_version_number, int reserved)
			implements TreeNode {

		@Override
		public KVP getJTreeNode(int modus) {
			final KVP s = new KVP("M7BrandHomeTransponder");
			s.add(new KVP("operator_network_id", operator_network_id));
			s.add(new KVP("operator_sublist_id", operator_sublist_id));
			s.add(new KVP("home_transport_stream_id", home_transport_stream_id));
			s.add(new KVP("home_original_network_id", home_original_network_id));
			s.add(new KVP("homebckp_transport_stream_id", homebckp_transport_stream_id));
			s.add(new KVP("homebckp_original_network_id", homebckp_original_network_id));
			s.add(new KVP("fst_pid", fst_pid));
			s.add(new KVP("FST_version_number", fst_version_number));
			s.add(new KVP("reserved", reserved));
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

	// linkage type 0x0D exent linkage
	private int target_event_id;
	private int target_listed;
	private int event_simulcast;
	private int reserved;

	// linkage type 0x20 downloadable font info 
	// EN 303 560 V1.1.1 5.3.2.2 Linkage descriptor with linkage type 0x20
	private int font_count;
	private List<FontInfo> fontList = new ArrayList<>();
	
	private int m7_code;


	// TODO handle linkage_type ==0x05
	// linkage type=09 is software update, oui = http://standards.ieee.org/regauth/oui/oui.txt


	public LinkageDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		transportStreamId = getInt(b, 2,2,MASK_16BITS);
		originalNetworkId = getInt(b, 4,2,MASK_16BITS);
		serviceId =  getInt(b, 6,2,MASK_16BITS);
		linkageType = getInt(b, 8,1,MASK_8BITS);
		if(linkageType==0x08){ // mobile hand-over
			int r =0;
			hand_over_type = getInt(b, 9,1,0xF0)>>>4;
			origin_type = getInt(b, 9,1,MASK_1BIT);
			if((hand_over_type ==0x01)
					|| (hand_over_type ==0x02)
					|| (hand_over_type ==0x03)){
				network_id = getInt(b, 10,2,MASK_16BITS);
				r+=2;
			}
			if (origin_type ==0x00){
				initial_service_id = getInt(b, 10+r,2,MASK_16BITS);
				r+=2;
			}
			privateDataByte = copyOfRange(b,  10+r,  descriptorLength+2);
		}else if(linkageType==0x09){ //System Software Update Service (TS 102 006)
			OUI_data_length = getInt(b, 9,1,MASK_8BITS);
			int r =0;
			while (r<OUI_data_length) {
				final int oui = getInt(b,  10+r, 3, Utils.MASK_24BITS);
				final int selectorLength= getInt(b,  r+13, 1, MASK_8BITS);
				final byte[] selector_byte = copyOfRange(b,  r+14,  r+14+selectorLength);

				final OUIEntry ouiEntry = new OUIEntry(oui,selectorLength,selector_byte);
				ouiList.add(ouiEntry);
				r=r+4+selectorLength;
			}
			privateDataByte = copyOfRange(b,  10+r,  descriptorLength+2);
		}else if(linkageType==0x0a){ // TS containing SSU BAT or NIT (TS 102 006)
			tableType =	getInt(b, 9,1,MASK_8BITS);
			privateDataByte = copyOfRange(b,  10,  descriptorLength+2);
		}else if(linkageType==0x0b){ // IP/MAC Notification Table
			platformIdDataLength =	getInt(b, 9,1,MASK_8BITS);
			int r =0;
			while (r<platformIdDataLength) {
				final int platform_id = getInt(b,  10+r, 3, Utils.MASK_24BITS);
				final Platform p = new Platform(platform_id);
				platformList.add(p);
				final int platform_name_loop_length= getInt(b,  r+13, 1, MASK_8BITS);
				int t=0;
				while(t<platform_name_loop_length){
					final String languageCode=getISO8859_1String(b,  r+t+14, 3);
					final DVBString platformName = new DVBString(b, r+t+17);
					final PlatformName pName = new PlatformName(languageCode, platformName);
					p.addPlatformName(pName);
					t+=4+platformName.getLength();
				}
				r+=t+4;
			}
			privateDataByte = Arrays.copyOfRange(b,  10+r,  descriptorLength+2);
		}else if(linkageType==0x0c){ // TS containing SSU BAT or NIT (TS 102 006)
			tableType =	getInt(b, 9,1,MASK_8BITS);
			if(tableType==0x02){
				bouquetID = getInt(b, 10,2,MASK_16BITS);
				privateDataByte = copyOfRange(b,  12,  descriptorLength+2);
			}else{
				privateDataByte = copyOfRange(b,  10,  descriptorLength+2);
			}

		}else if(linkageType==0x0D){ // event linkage
			target_event_id  = getInt(b, 9,2,MASK_16BITS);
			target_listed = getInt(b, 11,1,0x80)>>7;
			event_simulcast= getInt(b, 11,1,0x40)>>6;
			reserved = getInt(b, 11,1,MASK_6BITS);
			privateDataByte = copyOfRange(b,  12,  descriptorLength+2);

		}else if(linkageType>=0x0e && linkageType<=0x1f){ // extended event linkage 
			logger.info("extended event linkage not implemented");
		}else if(linkageType==0x20){ // downloadable font info linkage
			font_count = getInt(b, 9,1,MASK_8BITS);
			for (int i = 0; i < font_count; i++) {
				FontInfo font = new FontInfo(getInt(b, 10+i,1,MASK_8BITS));
				fontList.add(font);
			}
			privateDataByte = copyOfRange(b,   10 + font_count, descriptorLength + 2);

		}else if(linkageType==0x81){ //  NorDig Unified ver 2.1    ch 12.2.6 NorDig linkage for bootloader
			// TODO, this is a private usage, but not indicated by a private_data_specifier_descriptor: 0x5F
			// just assume it is nordig?
			int s = 9; // private data, if any, starts at position 9
			while((s+17) <= descriptorLength){ // bootloader = 19 bytes
				int manufacturer_id = getInt(b, s,2,MASK_16BITS);
				byte[] version_id = copyOfRange(b,  s+2,  s+10); // 64 bits, 8 bytes
				long private_id = getLong(b,  s+10, 4, MASK_32BITS);
				byte[] start_time =  copyOfRange(b,  s+14,  s+19);
				final NordigBootLoader nordigBootLoader = new NordigBootLoader(manufacturer_id, version_id, private_id, start_time);
				bootLoaderList.add(nordigBootLoader);
				s +=19;
			}
			privateDataByte = copyOfRange(b,  s,  descriptorLength+2);
			
			// M7 FASTSCAN HOME TP LOCATION DESCRIPTOR
		} else if (((linkageType == 0x88) || (linkageType == 0x89) || (linkageType == 0x8A))
				&& PreferencesManager.isEnableM7Fastscan()) {
			m7_code = getInt(b, 9,2,MASK_16BITS);
			int s = 11;
			while((s+9) <= descriptorLength){
				int operator_network_id = getInt(b, s,2,MASK_16BITS);
				int operator_sublist_id = getInt(b, s+2,1,MASK_8BITS);
				int home_transport_stream_id = getInt(b, s+3,2,MASK_16BITS);
				int home_original_network_id = getInt(b, s+5,2,MASK_16BITS);
				int homebckp_transport_stream_id = getInt(b, s+7,2,MASK_16BITS);
				int homebckp_original_network_id = getInt(b, s+9,2,MASK_16BITS);
				int fst_pid = getInt(b, s+11,2,MASK_16BITS);   
				int fst_version_number = getInt(b, s+13,1,0xF8)>>>3;   
				int reserved = getInt(b, s+13,1,MASK_3BITS);   
			
				BrandHomeTransponder brandHomeTransponder = new BrandHomeTransponder(operator_network_id, operator_sublist_id,
						home_transport_stream_id,home_original_network_id,homebckp_transport_stream_id,homebckp_original_network_id,
						fst_pid,fst_version_number,reserved);
				m7BrandHomeTransponderList.add(brandHomeTransponder);
				s+=14;
			}
			
			privateDataByte = copyOfRange(b,  s,  descriptorLength+2);
			
			// M7 FASTSCAN ONT LOCATION DESCRIPTOR
			
		} else if ((linkageType == 0x8D)&& PreferencesManager.isEnableM7Fastscan()){ 
			m7_code = getInt(b, 9,2,MASK_16BITS);
			reserved = getInt(b, 11,1,MASK_8BITS);
			privateDataByte = copyOfRange(b,  12,  descriptorLength+2);
		} else if (linkageType <= 0x07){ // no extra data
			privateDataByte = copyOfRange(b,  9,  descriptorLength+2);
		}else{
			logger.log(Level.INFO,"LinkageDescriptor, not implemented linkageType: "+linkageType +"("+getLinkageTypeString(linkageType)+"), called from:"+parent);
			privateDataByte = copyOfRange(b,  9,  descriptorLength+2);

		}
	}

	@Override
	public String toString() {
		return super.toString() + "transportStreamId="+transportStreamId;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new KVP("transport_stream_id",transportStreamId));
		t.add(new KVP("original_network_id",originalNetworkId).setDescription(getOriginalNetworkIDString(originalNetworkId)));
		t.add(new KVP("service_id",serviceId).setDescription(parentTableSection.getParentPID().getParentTransportStream().getPsi().getSdt().getServiceName(originalNetworkId,transportStreamId,serviceId)));
		t.add(new KVP("linkage_type",linkageType).setDescription(getLinkageTypeString(linkageType)));
		if(linkageType==0x08){
			t.add(new KVP("hand-over_type",hand_over_type).setDescription(getHandOverString(hand_over_type)));
			t.add(new KVP("origin_type",origin_type).setDescription(getOriginTypeString(origin_type)));
			if((hand_over_type ==0x01)
					|| (hand_over_type ==0x02)
					|| (hand_over_type ==0x03)){
				t.add(new KVP("network_id",network_id).setDescription(getParentPID().getParentTransportStream().getPsi().getNit().getNetworkName(network_id)));
			}
			if (origin_type ==0x00){
				t.add(new KVP("initial_service_id",initial_service_id));
			}

		}else if(linkageType==0x09){
			addListJTree(t,ouiList,modus,"Systems Software Update");
		}else if(linkageType==0x0a){
			t.add(new KVP("table_type",tableType).setDescription(getTableTypeString(tableType)));
		}else if(linkageType==0x0b){
			addListJTree(t,platformList,modus,"platform_list");
		}else if(linkageType==0x0c){
			t.add(new KVP("table_type",tableType).setDescription(getTableTypeString(tableType)));
			if(tableType==0x02){
				t.add(new KVP("bouquet_id",bouquetID));
			}
		}else if(linkageType==0x0d){ // event linkage
			t.add(new KVP("target_event_id",target_event_id));
			t.add(new KVP("target_listed",target_listed).setDescription(target_listed==1?"service shall be included in SDT":"service may not be included in SDT"));
			t.add(new KVP("event_simulcast",event_simulcast).setDescription(event_simulcast==1?"target and source are being simulcast":"events are offset in time"));
			t.add(new KVP("reserved",reserved));
		}else if(linkageType>=0x0e && linkageType<=0x1f){ // extended event linkage 
			t.add(GuiUtils.getNotImplementedKVP("extended event linkage"));
		}else if(linkageType==0x20){ // downloadable font info linkage
			
			t.add(new KVP("font_count",font_count));
			addListJTree(t,fontList,modus,"(downloadable fonts");
			t.add(new KVP("reserved_zero_future_use",privateDataByte));
			
		}else if(linkageType==0x81){
			addListJTree(t,bootLoaderList,modus,"Nordig BootLoader");

		} else if (((linkageType == 0x88) || (linkageType == 0x89) || (linkageType == 0x90))
				&& PreferencesManager.isEnableM7Fastscan()) {
			t.add(new KVP("m7_code",m7_code).setDescription("should contain values from 0x7701 to 0x77FF"));
			addListJTree(t,m7BrandHomeTransponderList,modus,"M7 Brand-HomeTransponderList");


		} else if ((linkageType == 0x8D)&& PreferencesManager.isEnableM7Fastscan()){ // ONT LOCATION DESCRIPTOR
			t.add(new KVP("m7_code",m7_code).setDescription("should contain values from 0x7701 to 0x77FF (service_id == ONT_PID)"));
			t.add(new KVP("reserved",reserved));
		}else {
			t.add(new KVP("private_data_byte",privateDataByte).setDescription("unimplmented linkage type"));
		}
		return t;
	}

	public int getOriginalNetworkId() {
		return originalNetworkId;
	}


	public int getTransportStreamId() {
		return transportStreamId;
	}

	public byte[] getPrivateDataByte() {
		return privateDataByte;
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
		case 0x0D : return "event linkage";

		case 0x20 : return "downloadable font info linkage";
		
		// DVB BlueBook A038r16 – (April 2023)
		case 0x21 : return "Native IP bootstrap MPE stream";
		
		case 0x81 : return "user defined: (linkage to NorDig bootloader)";
		case 0x82 : return "user defined: (NorDig Simulcast replacement service/linkage to Ziggo software update)"; // or NorDig Simulcast replacement service.
		
		case 0x88, 
			 0x89, 
			 0x8A : return "user defined: (M7 Fastscan Home TP location descriptor)";
		case 0x8D : return "user defined: (M7 Fastscan ONT location location descriptor)";

		case 0xA0 : return "user defined: link to OpenTV VOD service";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0xA6 : return "user defined: link to OpenTV ITV service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf
		case 0xA7 : return "user defined: link to WEB service (YOUSEE)";  // http://download.tdconline.dk/pub/kabeltv/pdf/CPE/Rules_of_Operation.pdf

		case 0xFF : return "reserved for future use";

		default:
			if((0x0E<=linkageType)&&(linkageType<=0x1F )){return "extended event linkage";}
			if((0x21<=linkageType)&&(linkageType<=0x7F )){return "reserved for future use";}
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

	public int getM7_code() {
		return m7_code;
	}

	public List<BrandHomeTransponder> getM7BrandHomeTransponderList() {
		return m7BrandHomeTransponderList;
	}

}
