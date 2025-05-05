/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitString;
import nl.digitalekabeltelevisie.util.Utils;

public class TxtDataField extends EBUDataField {

	/**
	 *
	 */
	public TxtDataField(byte[] data, int offset, int len, long pts) {
		super(data,offset,len,pts);
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = new KVP(EBUPESDataField.getDataUnitIdString(dataUnitId));
		addDetailsToJTree(s);
		addDetailsToJTree(s,modus);
		if((getPacketNo()>=0)&&(getPacketNo()<=25)){
			s.add(new KVP(getTeletextPlain()).setHtmlLabel(getTeletextHTML()));
		}
		return s;
	}


	protected void addDetailsToJTree(KVP s, int modus) {
		s.add(new KVP("framing_code",getInt(data_block, offset+3, 1, MASK_8BITS)));
		s.add(new KVP("magazine_and_packet_address",getInt(data_block, 4+offset, 2,MASK_16BITS),"Magazine:"+getMagazineNo()+" Packet:"+getPacketNo()));
		s.add(new KVP("raw data",data_block,offset,46));
		if(getPacketNo()==0){ // header
			s.add(new KVP("page number",toHexString((getPageNumberTens()* 16L)+getPageNumberUnits(),2))); // Tens is really hexadecimal
			s.add(new KVP("sub page",toHexString(getSubPage(),4)));
			s.add(new KVP("data bytes",getHeaderDataBytes()));
			StringBuilder flags =new StringBuilder();
			if(isErasePage()){ buildStringList(flags,"Erase Page");	}
			if(isNewsFlash()){ buildStringList(flags,"Newsflash");	}
			if(isSubtitle()){ buildStringList(flags,"Subtitle");	}
			if(isSuppresHeader()){ buildStringList(flags,"Suppress Header");	}
			if(isUpdateIndicator()){ buildStringList(flags,"Update Indicator");	}
			if(isInterruptedSequence()){ buildStringList(flags,"Interrupted Sequence");	}
			if(isInhibitDisplay()){ buildStringList(flags,"Inhibit Display");	}
			if(isMagazineSerial()){ buildStringList(flags,"Magazine Serial");	}
			s.add(new KVP("flags",flags.toString()));
			// internal we use reverted order of NationalOptionCharacterSubset, bevause of ProjectX legacy.
			// For display we want to conform to Table 32 of ETSI EN 300 706 V1.2.1 (2003-04) 
			int normalOrderNOS = invNationalOptionSet[getNationalOptionCharacterSubset()];
			s.add(new KVP("National Option Character Subset",normalOrderNOS,"C12 C13 C14: "+ toBinaryString(normalOrderNOS, 3)));

		}else if((getPacketNo()>0)&&(getPacketNo()<=25)){ // Packets X/1 to X/25, direct display
			s.add(new KVP("data bytes",getPageDataBytes()));

		}
		if((getPacketNo()>=26)&&(getPacketNo()<=31)){ // Page enhancement data packets
			s.add(new KVP("Designation Code",getDesignationCode()));

		}
		if((getPacketNo()==26)){ // 13 triplets, Hamming 24/18 coded 9.4.1 Packet X/26 addressing a character location and overwriting the existing character defined on the Level 1 page; VCR programming, see EN 300 231

			List<TxtTriplet> tripletList = getTxtTripletList();
			addListJTree(s,tripletList,modus,"triplet_list");

		}
		if((getPacketNo()==28)||  // page specific
				(getPacketNo()==29)){ addPacketX28Format1Details(s, modus);

		}
		if((getPacketNo()==27)&&(getDesignationCode()>=0)&&(getDesignationCode()<=3)){
			// 9.6.1 Packets X/27/0 to X/27/3 for Editorial Linking
			KVP linkControl =new KVP("Editorial Linking");
			s.add(linkControl);
			for(int t=0;t<6;t++){
				int pageNumber=getPageNumber(7+(t*6));
				int subPage = getSubPage(9+(t*6));
				int m=getMagazineComplement(10+(6*t))^getMagazineNo();
				String formattedPageNo = formatPageNo(m,pageNumber,subPage);
				KVP link = new KVP("Link "+t+": "+formattedPageNo);
				linkControl.add(link);

				link.add(new KVP("pageNumber",pageNumber));
				link.add(new KVP("sub page",subPage));
				link.add(new KVP("magazine modified",m));
			}
			if(getPacketNo()==27){
				int linkControlByte = getHammingByte(data_block[43+offset]);
				s.add(new KVP("Link Control Byte",linkControlByte));
			}
		}

		if((getPacketNo()>=30)&&(getPacketNo()<=31)){ // Independent Data Lines (IDL) ETSI EN 300 708 V1.2.1,
			s.add(new KVP("Data Channel",getDataChannel(),getDataChannelString(getDataChannel())));
		}
		if((getMagazineNo()==0)&&(getPacketNo()==30)&&(getDesignationCode()>=0)&&(getDesignationCode()<=1)){ // 9.8.1 Packet 8/30 Format 1
			addInititalPagePacket8_30ToJTree(s);
			addChannelDataLine30(s);

			s.add(new KVP("Status Display",getDataBytes(26, 46)));

		}
		if((getMagazineNo()==0)&&(getPacketNo()==30)&&(getDesignationCode()>=2)&&(getDesignationCode()<=3)){ // 9.8.2 Packet 8/30 Format 2
			addInititalPagePacket8_30ToJTree(s);
			addPDCDetails(s);
			s.add(new KVP("Status Display",getDataBytes(26, 46)));
		}

	}


	/**
	 * @return
	 */
	public List<TxtTriplet> getTxtTripletList() {
		List<TxtTriplet> tripletList = new ArrayList<>();
		for (int i = 1; i <= 13; i++) {
			TxtTriplet tr = new TxtTriplet(data_block,offset+4+(i*3));
			tripletList.add(tr);
		}
		return tripletList;
	}


	/**
	 * @param s
	 * @param modus
	 */
	private void addPacketX28Format1Details(KVP s, int modus) {
		// magazine specific
		// 13 triplets, Hamming 24/18 coded

		// § ETSI EN 300 706 V1.2.1 9.4.2.1 Page Function and Page Coding

		List<Triplet> tripletList = getTripletList();
		addListJTree(s, tripletList, modus, "triplet_list");

		Triplet tr1 = tripletList.getFirst(); // 1th triplet

		int pageFunction = tr1.getPageFunction();
		int pageCoding = (tr1.getVal() & 0x70) >> 4;
		s.add(new KVP("pageFunction", pageFunction, getPageFunctionString(pageFunction)));
		s.add(new KVP("pageCoding", pageCoding, getPageCodingString(pageCoding)));

		if (pageFunction == 0) { // 9.4.2.2 Coding for basic Level 1 Teletext pages
			int defaultCharset = (tr1.getVal() & 0x3F80) >> 7;
			int bits14_11 = (defaultCharset & 0b1111000)>>3;
			int bits10_8 = (defaultCharset & 0b111);
			s.add(new KVP("Default G0 and G2 Character Set Designation and National Option Selection", defaultCharset,
							"bits 14 13 12 11: "+ toBinaryString(bits14_11 , 4)+" bits 10 9 8: " + toBinaryString(bits10_8,3)));
			Triplet tr2 = tripletList.get(1);
			int secondCharset = (tr1.getVal()&0b11_1100_0000_0000_0000)>>14 | ((tr2.getVal() & 0b0111)<<4);
			s.add(new KVP("Second G0 Set Designation and National Option Selection",secondCharset));
			int leftSidePanel = (tr2.getVal() & 0x08) >> 3;
			int rightSidePanel = (tr2.getVal() & 0x10) >> 4;
			int sidePanelStatusFlag = (tr2.getVal() & 0x20) >> 5;
			int numberOfColumnsInSidePanels = (tr2.getVal() & 0x3C0) >> 6;
			s.add(new KVP("Left Side Panel", leftSidePanel, leftSidePanel == 0
					? "No left side panel is to be displayed" : "Left side panel is to be displayed"));
			s.add(new KVP("Right Side Panel", rightSidePanel, rightSidePanel == 0
					? "No right side panel is to be displayed" : "Right side panel is to be displayed"));
			s.add(new KVP("Side Panel Status Flag", sidePanelStatusFlag,
					sidePanelStatusFlag == 0 ? "Side panel(s) required at Level 3.5 only"
							: "Side panel(s) required at Levels 2.5 & 3.5"));
			s.add(new KVP("Number of Columns in Side Panels", numberOfColumnsInSidePanels));

			// The bits for Colour Map Entry Coding for CLUTs 2 and 3 are transmitted LSB first (as standard for
			// teletext)
			// in the tripletList these bits are already reversed into MSB first (standard and convenient for java)
			// For retrieving the colours we need to reverse back to LSB first, concatenate all together, and split.
			// After splitting the
			// separate colours reverse again to get java ints.

			BitString bs = new BitString();
			// bits 11-18 of triplet 2
			bs.addIntBitsReverse(tr2.getVal() & (0x3FC00 >> 10), 8);
			for (int i = 3; i <= 12; i++) {
				bs.addIntBitsReverse(tripletList.get(i - 1).getVal(), 18);
			}
			// bits 1-4 of triplet 13
			bs.addIntBitsReverse(tripletList.get(12).getVal() & 0xF, 4);

			KVP clut23 = new KVP("Colour Map Entry Coding for CLUTs 2 and 3");

			for (int i = 16; i <= 31; i++) {
				int r = bs.getIntBitsReverse(4); // 0..15
				int g = bs.getIntBitsReverse(4);
				int b = bs.getIntBitsReverse(4);

				int r_byte = (r * 255) / 15;
				int g_byte = (g * 255) / 15;
				int b_byte = (b * 255) / 15;

				String bgColor = "#" + toHexStringUnformatted(r_byte, 2)
						+ toHexStringUnformatted(g_byte, 2) + toHexStringUnformatted(b_byte, 2);
				KVP cmapEntry = new KVP("Colour Map entry " + i).setHtmlLabel(
						"Colour Map entry " + i + " <code><span style=\"background-color: " + bgColor
								+ "; color: white;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></code>"
						);
				cmapEntry.add(new KVP("R", r));
				cmapEntry.add(new KVP("G", g));
				cmapEntry.add(new KVP("B", b));
				clut23.add(cmapEntry);
			}
			s.add(clut23);

			Triplet tr13 = tripletList.get(12);
			int defaultScreenColour = (tr13.getVal() & 0x1F0) >> 4;
			int defaultRowColour = (tr13.getVal() & 0x3E00) >> 9;
			int blackBackgroundColourSubstitution = (tr13.getVal() & 0x4000) >> 14;
			int colourTableRemapping = (tr13.getVal() & 0x38000) >> 15;

			s.add(new KVP("Default Screen Colour", defaultScreenColour));
			s.add(new KVP("Default Row Colour", defaultRowColour));
			s.add(new KVP("Black Background Colour Substitution", blackBackgroundColourSubstitution,
							blackBackgroundColourSubstitution == 1
									? "black background is replaced by the full row colour applying to that row"
									: "No substitution of black background by the pertaining row colour"));
			s.add(new KVP("Colour Table Re-mapping for use with Spacing Attributes", colourTableRemapping));

		}
	}

	/**
	 * @return
	 */
	protected List<Triplet> getTripletList() {
		List<Triplet> tripletList = new ArrayList<>();
		for (int i = 1; i <= 13; i++) {
			Triplet tr = new Triplet(data_block, offset + 4 + (i * 3));
			tripletList.add(tr);
		}
		return tripletList;
	}


	public static String getPageFunctionString(int pageFunction) {

        return switch (pageFunction) {
            case 0x00 -> "Basic Level 1 Teletext page (LOP)";
            case 0x01 -> "Data broadcasting page coded according to EN 300 708";
            case 0x02 -> "Global Object definition page (GPOP)";
            case 0x03 -> "Normal Object definition page (POP)";
            case 0x04 -> "Global DRCS downloading page (GDRCS)";
            case 0x05 -> "Normal DRCS downloading page (DRCS)";
            case 0x06 -> "Magazine Organization table (MOT)";
            case 0x07 -> "Magazine Inventory page (MIP)";
            case 0x08 -> "Basic TOP table (BTT)";
            case 0x09 -> "Additional Information Table (AIT)";
            case 0x0a -> "Multi-page table (MPT)";
            case 0x0b -> "Multi-page extension table (MPT-EX)";
            case 0x0c ->
                    "Page contain trigger messages defined according to IEC/PAS 62297 Edition 1.0 (2002-01): Proposal for introducing a trigger mechanism into TV transmissions";
            default -> "reserved for future use";
        };
	}


	public static String getPageCodingString(int pageCoding) {

        return switch (pageCoding) {
            case 0x00 -> "All 8-bit bytes, each comprising 7 data bits and 1 odd parity bit";
            case 0x01 -> "All 8-bit bytes, each comprising 8 data bits";
            case 0x02 ->
                    "Per packet: One 8-bit byte coded Hamming 8/4, followed by thirteen groups of three 8-bit bytes coded Hamming 24/18. All packets coded in this way";
            case 0x03 -> "All 8-bit bytes, each code Hamming 8/4";
            case 0x04 ->
                    "Per packet: Eight 8-bit bytes coded Hamming 8/4, followed by twelve 8-bit bytes coded 7 data bits and 1 odd parity bit. This sequence is then repeated for the remaining 20 bytes. All packets coded in this way.";
            case 0x05 ->
                    "Per packet: First 8-bit byte coded Hamming 8/4. The data bits from this byte define the coding of the remaining 39 bytes of this packet only, according to the first five entries in this table.";
            default -> "reserved for future use";
        };
	}

	/**
	 * @param s
	 */
	private void addChannelDataLine30(KVP node) {
		KVP s = new KVP("TV channel related broadcast service data");
		node.add(s);
		s.add(new KVP("Multiplexing",getDesignationCode(),(getDesignationCode()==0)?"Multiplexed with video":"Non-multiplexed, all lines may be used to carry Teletext"));
		int netWorkIdent = getInt(data_block, offset+13, 2, MASK_16BITS);
		s.add(new KVP("Network Identification Code",netWorkIdent,getNIString(netWorkIdent)));
		int timeOffsetCode=getInt(data_block, offset+15, 1, 0x7E)>>1;
		s.add(new KVP("Time Offset Code",timeOffsetCode,getTimeOffsetCodeString(timeOffsetCode)));

		//Modified Julian Date
		int tenthousands = invtab[getInt(data_block, offset+16, 1, 0xF0)]-1;
		int hundreds = invtab[getInt(data_block, offset+17, 1, 0xF0)]-1;
		int thousands = invtab[getInt(data_block, offset+17, 1, 0x0F)<<4]-1;
		int units = invtab[getInt(data_block, offset+18, 1, 0xF0)]-1;
		int tens = invtab[getInt(data_block, offset+18, 1, 0x0F)<<4]-1;

		long mjd= (((((((tenthousands* 10L)+thousands)*10)+hundreds)*10)+tens)*10)+units;
		long y =  (long) ((mjd  - 15078.2) / 365.25);
		long m =  (long) ((mjd - 14956.1 - (long)(y * 365.25) ) / 30.6001);
		long d =  (mjd - 14956 - (long)(y * 365.25) - (long)(m * 30.6001));
		long k =  ((m == 14) || (m == 15)) ? 1 : 0;
		y = y + k + 1900;
		m = m - 1 - (k*12);

		s.add(new KVP("Modified Julian Date",mjd,y+"/"+m+"/"+d));
		int hoursUnits = invtab[getInt(data_block, offset+19, 1, 0xF0)]-1;
		int hoursTens = invtab[getInt(data_block, offset+19, 1, 0x0F)<<4]-1;
		int minutesUnits = invtab[getInt(data_block, offset+20, 1, 0xF0)]-1;
		int minutesTens = invtab[getInt(data_block, offset+20, 1, 0x0F)<<4]-1;
		int secondsUnits = invtab[getInt(data_block, offset+21, 1, 0xF0)]-1;
		int secondsTens = invtab[getInt(data_block, offset+21, 1, 0x0F)<<4]-1;
        s.add(new KVP("Universal Time Co-ordinated", String.valueOf(hoursTens) + hoursUnits + ':' + minutesTens + minutesUnits + ':' + secondsTens + secondsUnits));
	}


	private void addInititalPagePacket8_30ToJTree(KVP s) {
		int pageNumber=getPageNumber(7);
		int subPage = getSubPage(9);
		int m=getMagazineComplement(10)^getMagazineNo();
		String formattedPageNo = formatPageNo(m,pageNumber,subPage);
		KVP initialTxtPage = new KVP("Initial Teletext Page",formattedPageNo);
		s.add(initialTxtPage);

		initialTxtPage.add(new KVP("pageNumber",pageNumber));
		initialTxtPage.add(new KVP("sub page",subPage));
		initialTxtPage.add(new KVP("magazine modified",m));
	}

	/**
	 * @param m magazine number (0..7)
	 * @param pageNumber pagenumber (hex)
	 * @param subPage
	 * @return String whith pagenumber in form "mpp:ssss"
	 */
	public static String formatPageNo(int m, int pageNumber, int subPage) {
		StringBuilder b=new StringBuilder();

        if (pageNumber == 0xff) {
            b.append(" - ");
        } else {
            if (m == 0) {
                b.append("8");
            } else {
                b.append(m);
            }
            b.append(toHexStringUnformatted(pageNumber, 2));
            if (subPage != 0x3f7f) {
                b.append(':').append(toHexStringUnformatted(subPage, 4));
            }
        }

		return b.toString();
	}


	/**
	 * @param flags
	 * @param string
	 */
	private static void buildStringList(StringBuilder flags, String string) {
		if(!flags.isEmpty()){
			flags.append(",");
		}
		flags.append(string);
	}


	public KVP getHTMLJTreeNode(int modus) {
		KVP s = new KVP(getTeletextPlain()).setHtmlLabel(getTeletextHTML());
		addDetailsToJTree(s);
		addDetailsToJTree(s, modus);
		return s;
	}

	public byte[] getHeaderDataBytes(){
		byte[] r = new byte[32];
		for (int i = 14; i < 46; i++) {
			r[i-14] = (byte)(invtab[toUnsignedInt(data_block[i+offset])]&0x7f);
		}
		return r;
	}

	public byte[] getDataBytes(int start, int end){
		byte[] r = new byte[end-start];
		for (int i = start; i < end; i++) {
			r[i-start] = (byte)(invtab[toUnsignedInt(data_block[i+offset])]&0x7f);
		}
		return r;
	}

	public byte getRawByte(int i){
		return data_block[i+offset+6];
	}

	public String getTeletextHTML(){
		if(getPacketNo()==0){
			return getTeletextHTML(getHeaderDataBytes());
		}else if(getPacketNo()<26){
			return getTeletextHTML(getPageDataBytes());
		}
		return "";

	}

	public String getTeletextPlain(){
		if(getPacketNo()==0){
			return getTeletextPlain(getHeaderDataBytes());
		}else if(getPacketNo()<26){
			return getTeletextPlain(getPageDataBytes());
		}
		return "";

	}



	private static String getTeletextPlain(byte[] b){
		StringBuilder buf = new StringBuilder();
		for (byte ch : b) {
			if((ch>=32)&&(ch<127)){ // For text version of single line  national option charset subset is ignored because it can not be derived from only the line itself. Need pageheader for that.
				buf.append((char)ch);
			}else{  // empty space
				buf.append(' ');
			}
		}

		return buf.toString();
	}


	protected String getTeletextHTML(byte[] b){
		String bg="black";
		String fg="white";
		StringBuilder buf = new StringBuilder("<code><b><span style=\"background-color: black; color: white; \">");
		for (byte ch : b) {
			if(ch==0x20){ //nbsp
				buf.append("&nbsp;");
			}else if(ch==0x3c){ //<<
				buf.append("&lt;");
			}else if(ch==0x26){ //&
				buf.append("&amp;");
			}else if((ch>32)&&(ch<127)){  // For HTML version of single line  national option charset subset is ignored because it can not be derived from only the line itself. Need pageheader for that.
				buf.append((char)ch);
			}else if((ch>=0)&&(ch<=7)){ //12.2 Spacing attributes
				fg=getHTMLColorString(ch);
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else if((ch>=0x10)&&(ch<=0x17)){ //12.2 Spacing attributes Mosaic Colour Codes ("Set-After")
				fg=getHTMLColorString(ch-0x10);
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else if(ch==0x1c){ //Black Background ("Set-At")
				bg=getHTMLColorString(0);
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else if(ch==0x1d){ //1New Background ("Set-At")
				bg=fg;
				buf.append("</span><span style=\"background-color: ").append(bg).append("; color: ").append(fg).append(";\">&nbsp;");
			}else{  // not implemented, empty space
				buf.append("&nbsp;");
			}
		}
		buf.append("</span></b></code>");
		return buf.toString();
	}

	public static int getColorInt(int i){
		return Integer.decode("0x"+getRawColorString(i));

	}

	public static String getHTMLColorString(int i){
		return "#"+getRawColorString(i);

	}
	public static String getRawColorString(int i){
        return switch (i) {
            case 0 -> "000000";
            case 1 -> "ff0000";
            case 2 -> "00ff00";
            case 3 -> "ffff00";
            case 4 -> "0000ff";
            case 5 -> "ff00ff";
            case 6 -> "00ffff";
            case 7 -> "ffffff";
            case 8 -> "000000";
            case 9 -> "770000";
            case 10 -> "007700";
            case 11 -> "777700";
            case 12 -> "000077";
            case 13 -> "770077";
            case 14 -> "007777";
            case 15 -> "777777";
            case 16 -> "ff0055";
            case 17 -> "ff7700";
            case 18 -> "00ff77";
            case 19 -> "ffffbb";
            case 20 -> "00ccaa";
            case 21 -> "550000";
            case 22 -> "665522";
            case 23 -> "cc7777";
            case 24 -> "333333";
            case 25 -> "ff7777";
            case 26 -> "77ff77";
            case 27 -> "ffff77";
            case 28 -> "7777ff";
            case 29 -> "ff77ff";
            case 30 -> "77ffff";
            case 31 -> "dddddd";
            default -> "ffffff";
        };
	}


	private static String getDataChannelString(int i){
        return switch (i) {
            case 0 -> "Packet 8/30";
            case 1 -> "Packet 1/30";
            case 2 -> "Packet 2/30";
            case 3 -> "Packet 3/30";
            case 4 -> "Low bit rate audio";
            case 5 -> "Datavideo";
            case 6 -> "Datavideo";
            case 7 -> "Packet 7/30";
            case 8 -> "IDL Format A or B";
            case 9 -> "IDL Format A or B";
            case 10 -> "IDL Format A or B";
            case 11 -> "IDL Format A or B";
            case 12 -> "Low bit rate audio";
            case 13 -> "Datavideo";
            case 14 -> "Datavideo";
            case 15 -> "IDL Format B";
            default -> "Illegal value";
        };
	}

	public byte[] getPageDataBytes(){
		byte[] r = new byte[40];
		for (int i = 6; i < 46; i++) {
			r[i-6] = (byte)(invtab[toUnsignedInt(data_block[i+offset])]&0x7f);
		}
		return r;
	}

	public byte getPageDataByte(int i){
		return (byte)(invtab[toUnsignedInt(data_block[i+offset+6])]&0x7f);
	}

	public int getPageNumberUnits(){
		return getPageNumberUnits(6);
	}

	public int getPageNumberUnits(int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset]);
	}

	public int getPageNumberTens(){
		return getPageNumberTens(7);
	}

	public int getPageNumberTens(int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset]);
	}

	public int getPageNumber(){
		return getPageNumber(6);
	}

	public int getPageNumber(int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset])+(16*getHammingReverseByte(data_block[localOffset+1+offset]));
	}

	public int getSubPage(){
		return getSubPage(8);
	}

	public int getSubPage(int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset]) +(16*(getHammingReverseByte(data_block[localOffset+1+offset])&0x7)) +(256*getHammingReverseByte(data_block[localOffset+2+offset]))+ (4096*(getHammingReverseByte(data_block[localOffset+3+offset])&0x3));
	}

	public int getPageFunction(){ // assumses packet 28/0
		Triplet tr = new Triplet(data_block, offset + 7); // triplet 1, at 4 + 1*3
		return tr.getPageFunction();
	}


	private int getMagazineComplement(int localOffset){
		int m1 = (toUnsignedInt(data_block[offset+localOffset]) &0x01);
		int m2 = (toUnsignedInt(data_block[offset+localOffset+2]) &0x04)>>1;
		int m3 = (toUnsignedInt(data_block[offset+localOffset+2]) &0x01)<<2;
		return m1|m2|m3;
	}

	public int getDesignationCode(){
		return getHammingReverseByte(data_block[6+offset]);
	}

	public int getMagazineNo(){
		int magazine_and_packet_address = getInt(data_block, 4+offset, 2,MASK_16BITS);
		int r = (magazine_and_packet_address & 0x4000) >>14 ;
		r |= (magazine_and_packet_address & 0x1000) >>11 ;
			r |= (magazine_and_packet_address & 0x0400) >>8 ;
		return r;

	}
	public int getPacketNo(){
		int magazine_and_packet_address = getInt(data_block, 4+offset, 2,MASK_16BITS);
		int r = (magazine_and_packet_address & 0x0100) >> 8;
		r |= (magazine_and_packet_address & 0x0040) >> 5;
		r |= (magazine_and_packet_address & 0x0010) >>2 ;
		r |= (magazine_and_packet_address & 0x0004)<<1 ;
		r |= (magazine_and_packet_address & 0x0001)<<4 ;
		return r;

	}

	//ETSI EN 300 708 V1.2.1 § 6.4
	public int getDataChannel(){
		int magazine_and_packet_address = getInt(data_block, 4+offset, 2,MASK_16BITS);
		int r = (magazine_and_packet_address & 0x4000) >>13 ;
		r |= (magazine_and_packet_address & 0x1000) >>10 ;
		r |= (magazine_and_packet_address & 0x0400) >>7 ;
		r |= (magazine_and_packet_address & 0x0100) >>8 ;
		return r;

	}


	public boolean isErasePage(){
		return 0!= getInt(data_block, 9+offset,1,0x01);
	}

	public boolean isNewsFlash(){
		return 0!= getInt(data_block, 11+offset,1,0x04);
	}

	public boolean isSubtitle(){
		return 0!= getInt(data_block, 11+offset,1,0x01);
	}

	public boolean isSuppresHeader(){
		return 0!= getInt(data_block, 12+offset,1,0x40);
	}

	public boolean isUpdateIndicator(){
		return 0!= getInt(data_block, 12+offset,1,0x10);
	}

	public boolean isInterruptedSequence(){
		return 0!= getInt(data_block, 12+offset,1,0x04);
	}

	public boolean isInhibitDisplay(){
		return 0!= getInt(data_block, 12+offset,1,0x01);
	}

	public boolean isMagazineSerial(){
		return 0!= getInt(data_block, 13+offset,1,0x40);
	}

	public int getNationalOptionCharacterSubset(){
		return getHammingReverseByte(data_block[13+offset])>>1;
	}

	@Override
	public String toString(){
		StringBuilder b= new StringBuilder("TxtDataField ,Magazine:").append(getMagazineNo()).append("getPacketNo():").append(getPacketNo());
		if(getPacketNo()==0){
			b.append(", page=").append(getPageNumber()).append(", subPage=").append(getSubPage());
		}
		return b.toString();
	}


	protected void addPDCDetails(KVP node) {
		KVP s = new KVP("TV programme identification data for VCR control");
		node.add(s);

		s.add(new KVP("Label channel identifier",getLabelChannelIdentifier()));
		s.add(new KVP("Label Update Flag",getLabelUpdateFlag(),(getLabelUpdateFlag()==1)?"label does not relate to the current television programme, but is intended to update the label memories in video recorders":"label does relate to the current television programme"));
		s.add(new KVP("Prepare to Record Flag",getPrepareToRecordFlag()));
		s.add(new KVP("Status of analogue sound",getStatusOfAnalogueSound(),VPSDataField.getPCSAudioString(getStatusOfAnalogueSound())));
		s.add(new KVP("Mode identifier",getModeIdentifier(),(getModeIdentifier()==1)?"service code takes immediate effect":"effect of service codes is delayed by 30 s"));
		s.add(new KVP("CNI Country",getCNICountry()));
		s.add(new KVP("CNI Network",getCNINetwork()));
		s.add(new KVP("day",getDay()));
		s.add(new KVP("month",getMonth()));
		s.add(new KVP("hour",getHour()));
		s.add(new KVP("minute",getMinute()));
		s.add(new KVP("PTY",getPTY()));
	}


	protected int getCNICountry() {
		return (16*getHammingByte(data_block[13 +offset+2]))
				+ (4*(getHammingByte(data_block[13 +offset+8])&0x03))
				+ ((getHammingByte(data_block[13 +offset+9])&0x0C)>>2);
	}


	protected int getCNINetwork() {
		return ((64*(getHammingByte(data_block[13 +offset+3])&0xC))>>2)
				+ (16*(getHammingByte(data_block[13 +offset+9])&0x03))
				+ getHammingByte(data_block[13 +offset+10]);
	}


	protected int getLabelChannelIdentifier() {
		return (getHammingByte(data_block[13 + offset]) & 0xC) >> 2;
	}

	protected int getLabelUpdateFlag() {
		return (getHammingByte(data_block[13 + offset]) & 0x2) >> 1;
	}

	protected int getPrepareToRecordFlag() {
		return (getHammingByte(data_block[13 + offset]) & 0x1);
	}

	protected int getStatusOfAnalogueSound() {
		return (getHammingByte(data_block[13 + offset + 1]) & 0xC) >> 2;
	}

	protected int getModeIdentifier() {
		return (getHammingByte(data_block[13 + offset + 1]) & 0x2) >> 1;
	}

	protected int getDay() {
		return (8 * (getHammingByte(data_block[13 + offset + 3]) & 0x3)) + ((getHammingByte(data_block[13 + offset + 4]) & 0xe) >> 1);
	}

	protected int getMonth() {
		return (2 * (getHammingByte(data_block[13 + offset + 4]) & 0x1)) + ((getHammingByte(data_block[13 + offset + 5]) & 0xe) >> 1);
	}

	protected int getHour() {
		return (16 * (getHammingByte(data_block[13 + offset + 5]) & 0x1)) + getHammingByte(data_block[13 + offset + 6]);
	}

	protected int getMinute() {
		return (4 * getHammingByte(data_block[13 + offset + 7])) + ((getHammingByte(data_block[13 + offset + 8]) & 0xC) >> 2);
	}

	protected int getPTY() {
		return (16 * getHammingByte(data_block[13 + offset + 11])) + getHammingByte(data_block[13 + offset + 12]);
	}

	private static String getTimeOffsetCodeString(int timeOffset) {
		int uren = invtab[(timeOffset & 0x1E) << 3];
		StringBuilder b = new StringBuilder().append(uren);
		if ((timeOffset & 0x20) != 0) {
			b.append('½');
		}
		if ((timeOffset & 0x01) != 0) {
			b.append(" hour(s), negative offset (west of Greenwich)");
		} else {
			b.append(" hour(s), positive offset (east of Greenwich)");
		}

		return b.toString();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TxtDataField other = (TxtDataField) obj;
		if (dataUnitId != other.dataUnitId) {
			return false;
		}
		if (dataUnitLength != other.dataUnitLength) {
			return false;
		}
		if (!Utils.equals(data_block,offset,len, other.data_block,other.offset, other.len)) {
			return false;
		}
		if (field_parity != other.field_parity) {
			return false;
		}
		if (line_offset != other.line_offset) {
			return false;
		}
        return reserved_future_use == other.reserved_future_use;
    }



	public int getColor(int c){
		List<Triplet> tripletList= getTripletList();
		BitString bs = new BitString();
		// bits 11-18 of triplet 2
		bs.addIntBitsReverse(tripletList.get(1).getVal() & (0x3FC00 >> 10), 8);
		for (int i = 3; i <= 12; i++) {
			bs.addIntBitsReverse(tripletList.get(i - 1).getVal(), 18);
		}
		// bits 1-4 of triplet 13
		bs.addIntBitsReverse(tripletList.get(12).getVal() & 0xF, 4);


		for (int i = 16; i <= 31; i++) {
			int r = bs.getIntBitsReverse(4); // 0..15
			int g = bs.getIntBitsReverse(4);
			int b = bs.getIntBitsReverse(4);
			if(i==c){

				int r_byte = (r * 255) / 15;
				int g_byte = (g * 255) / 15;
				int b_byte = (b * 255) / 15;
				return (r_byte<<16) | (g_byte<<8) |b_byte;
			}

		}

		return 0;
	}


	/**
	 * @return
	 */
	public boolean isBlackBackGroundColorSubstitution() {
		Triplet tr13 = new Triplet(data_block, offset + 4 + (13 * 3));
		int blackBackgroundColourSubstitution = (tr13.getVal() & 0x4000) >> 14;
				return blackBackgroundColourSubstitution==1;
	}

	public int getDefaultScreenColour() {
		Triplet tr13 = new Triplet(data_block, offset + 4 + (13 * 3));
		return (tr13.getVal() & 0x1F0) >> 4;
	}

	public int getDefaultRowColour() {
		Triplet tr13 = new Triplet(data_block, offset + 4 + (13 * 3));
		return (tr13.getVal() & 0x3E00) >> 9;
	}
}