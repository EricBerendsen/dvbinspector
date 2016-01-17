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
package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitString;
import nl.digitalekabeltelevisie.util.Utils;

public class TxtDataField extends EBUDataField implements TreeNode{

	public static final String BLACK_HTML_LINE="<code><b><span style=\"background-color: black; color: white;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></b></code>";

	/**
	 *
	 */
	public TxtDataField(final byte[] data,final int offset,final int len, final long pts) {
		super(data,offset,len,pts);
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP(EBUPESDataField.getDataUnitIdString(dataUnitId)));
		super.addDetailsToJTree(s,modus);
		addDetailsToJTree(s,modus);
		if((getPacketNo()>=0)&&(getPacketNo()<=25)){
			s.add(new DefaultMutableTreeNode(new KVP(getTeletextHTML(),getTeletextPlain())));
		}
		return s;
	}


	/**
	 * @param s
	 */
	@Override
	protected void addDetailsToJTree(final DefaultMutableTreeNode s,final int modus) {
		s.add(new DefaultMutableTreeNode(new KVP("framing_code",getInt(data_block, offset+3, 1, MASK_8BITS),null)));
		s.add(new DefaultMutableTreeNode(new KVP("magazine_and_packet_address",getInt(data_block, 4+offset, 2,MASK_16BITS),"Magazine:"+getMagazineNo()+" Packet:"+getPacketNo())));
		s.add(new DefaultMutableTreeNode(new KVP("raw data",data_block,offset,46,null)));
		if(getPacketNo()==0){ // header
			s.add(new DefaultMutableTreeNode(new KVP("page number",toHexString((getPageNumberTens()*16)+getPageNumberUnits(),2), null))); // Tens is really hexadecimal
			s.add(new DefaultMutableTreeNode(new KVP("sub page",toHexString(getSubPage(),4), null)));
			s.add(new DefaultMutableTreeNode(new KVP("data bytes",getHeaderDataBytes(),null)));
			final StringBuilder flags =new StringBuilder();
			if(isErasePage()){ buildStringList(flags,"Erase Page");	}
			if(isNewsFlash()){ buildStringList(flags,"Newsflash");	}
			if(isSubtitle()){ buildStringList(flags,"Subtitle");	}
			if(isSuppresHeader()){ buildStringList(flags,"Suppress Header");	}
			if(isUpdateIndicator()){ buildStringList(flags,"Update Indicator");	}
			if(isInterruptedSequence()){ buildStringList(flags,"Interrupted Sequence");	}
			if(isInhibitDisplay()){ buildStringList(flags,"Inhibit Display");	}
			if(isMagazineSerial()){ buildStringList(flags,"Magazine Serial");	}
			s.add(new DefaultMutableTreeNode(new KVP("flags",flags.toString(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("National Option Character Subset",getNationalOptionCharacterSubset(),null)));

		}else if((getPacketNo()>0)&&(getPacketNo()<=25)){ // Packets X/1 to X/25, direct display
			s.add(new DefaultMutableTreeNode(new KVP("data bytes",getPageDataBytes(),null)));

		}
		if((getPacketNo()>=26)&&(getPacketNo()<=31)){ // Page enhancement data packets
			s.add(new DefaultMutableTreeNode(new KVP("Designation Code",getDesignationCode(),null)));

		}
		if((getPacketNo()==26)){ // 13 triplets, Hamming 24/18 coded 9.4.1 Packet X/26 addressing a character location and overwriting the existing character defined on the Level 1 page; VCR programming, see EN 300 231

			final List<TxtTriplet> tripletList = getTxtTripletList();
			addListJTree(s,tripletList,modus,"triplet_list");

		}
		if((getPacketNo()==28)||  // page specific
				(getPacketNo()==29)){ addPacketX28Format1Details(s, modus);

		}
		if((getPacketNo()==27)&&(getDesignationCode()>=0)&&(getDesignationCode()<=3)){
			// 9.6.1 Packets X/27/0 to X/27/3 for Editorial Linking
			final DefaultMutableTreeNode linkControl=new DefaultMutableTreeNode(new KVP("Editorial Linking"));
			s.add(linkControl);
			for(int t=0;t<6;t++){
				final int pageNumber=getPageNumber(7+(t*6));
				final int subPage = getSubPage(9+(t*6));
				final int m=getMagazineComplement(10+(6*t))^getMagazineNo();
				final String formattedPageNo = formatPageNo(m,pageNumber,subPage);
				final DefaultMutableTreeNode link=new DefaultMutableTreeNode(new KVP("Link "+t+": "+formattedPageNo));
				linkControl.add(link);

				link.add(new DefaultMutableTreeNode(new KVP("pageNumber",pageNumber,null)));
				link.add(new DefaultMutableTreeNode(new KVP("sub page",subPage,null)));
				link.add(new DefaultMutableTreeNode(new KVP("magazine modified",m,null)));
			}
			if(getPacketNo()==27){
				final int linkControlByte = getHammingByte(data_block[43+offset]);
				s.add(new DefaultMutableTreeNode(new KVP("Link Control Byte",linkControlByte,null)));
			}
		}

		if((getPacketNo()>=30)&&(getPacketNo()<=31)){ // Independent Data Lines (IDL) ETSI EN 300 708 V1.2.1,
			s.add(new DefaultMutableTreeNode(new KVP("Data Channel",getDataChannel(),getDataChannelString(getDataChannel()))));
		}
		if((getMagazineNo()==0)&&(getPacketNo()==30)&&(getDesignationCode()>=0)&&(getDesignationCode()<=1)){ // 9.8.1 Packet 8/30 Format 1
			addInititalPagePacket8_30ToJTree(s,modus);
			addChannelDataLine30(s,modus);

			s.add(new DefaultMutableTreeNode(new KVP("Status Display",getDataBytes(26, 46),null)));

		}
		if((getMagazineNo()==0)&&(getPacketNo()==30)&&(getDesignationCode()>=2)&&(getDesignationCode()<=3)){ // 9.8.2 Packet 8/30 Format 2
			addInititalPagePacket8_30ToJTree(s,modus);
			addPDCDetails(s, 0, 13);
			s.add(new DefaultMutableTreeNode(new KVP("Status Display",getDataBytes(26, 46),null)));
		}

	}


	/**
	 * @return
	 */
	public List<TxtTriplet> getTxtTripletList() {
		final ArrayList<TxtTriplet> tripletList = new ArrayList<TxtTriplet>();
		for (int i = 1; i <= 13; i++) {
			final TxtTriplet tr = new TxtTriplet(data_block,offset+4+(i*3));
			tripletList.add(tr);
		}
		return tripletList;
	}


	/**
	 * @param s
	 * @param modus
	 */
	private void addPacketX28Format1Details(final DefaultMutableTreeNode s, final int modus) {
		// magazine specific
		// 13 triplets, Hamming 24/18 coded

		// § ETSI EN 300 706 V1.2.1 9.4.2.1 Page Function and Page Coding

		final List<Triplet> tripletList = getTripletList();
		addListJTree(s, tripletList, modus, "triplet_list");

		final Triplet tr1 = tripletList.get(0); // 1th triplet

		final int pageFunction = tr1.getPageFunction();
		final int pageCoding = (tr1.getVal() & 0x70) >> 4;
		s.add(new DefaultMutableTreeNode(new KVP("first triplet", tr1.getVal(), null)));
		s.add(new DefaultMutableTreeNode(new KVP("pageFunction", pageFunction, getPageFunctionString(pageFunction))));
		s.add(new DefaultMutableTreeNode(new KVP("pageCoding", pageCoding, getPageCodingString(pageCoding))));

		if (pageFunction == 0) { // 9.4.2.2 Coding for basic Level 1 Teletext pages
			final int defaultCharset = (tr1.getVal() & 0x3F80) >> 7;
		s
		.add(new DefaultMutableTreeNode(new KVP(
				"Default G0 and G2 Character Set Designation and National Option Selection",
				defaultCharset, null)));
		final Triplet tr2 = tripletList.get(1);
		final int leftSidePanel = (tr2.getVal() & 0x08) >> 3;
		final int rightSidePanel = (tr2.getVal() & 0x10) >> 4;
		final int sidePanelStatusFlag = (tr2.getVal() & 0x20) >> 5;
		final int numberOfColumnsInSidePanels = (tr2.getVal() & 0x3C0) >> 6;
		s.add(new DefaultMutableTreeNode(
				new KVP("Left Side Panel", leftSidePanel, leftSidePanel == 0 ? "No left side panel is to be displayed" : "Left side panel is to be displayed")));
		s.add(new DefaultMutableTreeNode(new KVP("Right Side Panel", rightSidePanel, rightSidePanel == 0 ? "No right side panel is to be displayed": "Right side panel is to be displayed")));
		s.add(new DefaultMutableTreeNode(new KVP("Side Panel Status Flag", sidePanelStatusFlag,	sidePanelStatusFlag == 0 ? "Side panel(s) required at Level 3.5 only" : "Side panel(s) required at Levels 2.5 & 3.5")));
		s.add(new DefaultMutableTreeNode(new KVP("Number of Columns in Side Panels", numberOfColumnsInSidePanels,null)));

		// The bits for Colour Map Entry Coding for CLUTs 2 and 3 are transmitted LSB first (as standard for
		// teletext)
		// in the tripletList these bits are already reversed into MSB first (standard and convenient for java)
		// For retrieving the colours we need to reverse back to LSB first, concatenate all together, and split.
		// After splitting the
		// separate colours reverse again to get java ints.

		final BitString bs = new BitString();
		// bits 11-18 of triplet 2
		bs.addIntBitsReverse(tr2.getVal() & (0x3FC00 >> 10), 8);
		for (int i = 3; i <= 12; i++) {
			bs.addIntBitsReverse(tripletList.get(i - 1).getVal(), 18);
		}
		// bits 1-4 of triplet 13
		bs.addIntBitsReverse(tripletList.get(12).getVal() & 0xF, 4);

		final DefaultMutableTreeNode clut23 = new DefaultMutableTreeNode(new KVP(
				"Colour Map Entry Coding for CLUTs 2 and 3"));

		for (int i = 16; i <= 31; i++) {
			final int r = bs.getIntBitsReverse(4); // 0..15
			final int g = bs.getIntBitsReverse(4);
			final int b = bs.getIntBitsReverse(4);

			final int r_byte = (r * 255) / 15;
			final int g_byte = (g * 255) / 15;
			final int b_byte = (b * 255) / 15;

			final String bgColor = "#" + Utils.toHexStringUnformatted(r_byte, 2)
					+ Utils.toHexStringUnformatted(g_byte, 2) + Utils.toHexStringUnformatted(b_byte, 2);
			final DefaultMutableTreeNode cmapEntry = new DefaultMutableTreeNode(new KVP("Colour Map entry " + i
					+ " <code><span style=\"background-color: " + bgColor
					+ "; color: white;\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></code>","Colour Map entry " + i));
			cmapEntry.add(new DefaultMutableTreeNode(new KVP("R", r, null)));
			cmapEntry.add(new DefaultMutableTreeNode(new KVP("G", g, null)));
			cmapEntry.add(new DefaultMutableTreeNode(new KVP("B", b, null)));
			clut23.add(cmapEntry);
		}
		s.add(clut23);

		final Triplet tr13 = tripletList.get(12);
		final int defaultScreenColour = (tr13.getVal() & 0x1F0) >> 4;
		final int defaultRowColour = (tr13.getVal() & 0x3E00) >> 9;
		final int blackBackgroundColourSubstitution = (tr13.getVal() & 0x4000) >> 14;
		final int colourTableRemapping = (tr13.getVal() & 0x38000) >> 15;

		s.add(new DefaultMutableTreeNode(new KVP("Default Screen Colour", defaultScreenColour, null)));
		s.add(new DefaultMutableTreeNode(new KVP("Default Row Colour", defaultRowColour, null)));
		s.add(new DefaultMutableTreeNode(new KVP("Black Background Colour Substitution", blackBackgroundColourSubstitution, blackBackgroundColourSubstitution==1?"black background is replaced by the full row colour applying to that row":"No substitution of black background by the pertaining row colour")));
		s.add(new DefaultMutableTreeNode(new KVP("Colour Table Re-mapping for use with Spacing Attributes", colourTableRemapping, null)));

		}
	}



	/**
	 * @return
	 */
	protected List<Triplet> getTripletList() {
		final ArrayList<Triplet> tripletList = new ArrayList<Triplet>();
		for (int i = 1; i <= 13; i++) {
			final Triplet tr = new Triplet(data_block, offset + 4 + (i * 3));
			tripletList.add(tr);
		}
		return tripletList;
	}


	public static String getPageFunctionString(final int pageFunction) {

		switch (pageFunction) {
		case 0x00: return "Basic Level 1 Teletext page (LOP)";
		case 0x01: return "Data broadcasting page coded according to EN 300 708";
		case 0x02: return "Global Object definition page (GPOP)";
		case 0x03: return "Normal Object definition page (POP)";
		case 0x04: return "Global DRCS downloading page (GDRCS)";
		case 0x05: return "Normal DRCS downloading page (DRCS)";
		case 0x06: return "Magazine Organization table (MOT)";
		case 0x07: return "Magazine Inventory page (MIP)";
		case 0x08: return "Basic TOP table (BTT)";
		case 0x09: return "Additional Information Table (AIT)";
		case 0x0a: return "Multi-page table (MPT)";
		case 0x0b: return "Multi-page extension table (MPT-EX)";
		case 0x0c: return "Page contain trigger messages defined according to IEC/PAS 62297 Edition 1.0 (2002-01): Proposal for introducing a trigger mechanism into TV transmissions";
		default: return "reserved for future use";
		}
	}


	public static String getPageCodingString(final int pageCoding) {

		switch (pageCoding) {
		case 0x00: return "All 8-bit bytes, each comprising 7 data bits and 1 odd parity bit";
		case 0x01: return "All 8-bit bytes, each comprising 8 data bits";
		case 0x02: return "Per packet: One 8-bit byte coded Hamming 8/4, followed by thirteen groups of three 8-bit bytes coded Hamming 24/18. All packets coded in this way";
		case 0x03: return "All 8-bit bytes, each code Hamming 8/4";
		case 0x04: return "Per packet: Eight 8-bit bytes coded Hamming 8/4, followed by twelve 8-bit bytes coded 7 data bits and 1 odd parity bit. This sequence is then repeated for the remaining 20 bytes. All packets coded in this way.";
		case 0x05: return "Per packet: First 8-bit byte coded Hamming 8/4. The data bits from this byte define the coding of the remaining 39 bytes of this packet only, according to the first five entries in this table.";
		default: return "reserved for future use";
		}
	}

	/**
	 * @param s
	 */
	private void addChannelDataLine30(final DefaultMutableTreeNode node, final int modus) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("TV channel related broadcast service data"));
		node.add(s);
		s.add(new DefaultMutableTreeNode(new KVP("Multiplexing",getDesignationCode(),(getDesignationCode()==0)?"Multiplexed with video":"Non-multiplexed, all lines may be used to carry Teletext")));
		final int netWorkIdent = getInt(data_block, offset+13, 2, MASK_16BITS);
		s.add(new DefaultMutableTreeNode(new KVP("Network Identification Code",netWorkIdent,getNIString(netWorkIdent))));
		final int timeOffsetCode=getInt(data_block, offset+15, 1, 0x7E)>>1;
		s.add(new DefaultMutableTreeNode(new KVP("Time Offset Code",timeOffsetCode,getTimeOffsetCodeString(timeOffsetCode))));

		//Modified Julian Date
		final int tenthousands = invtab[getInt(data_block, offset+16, 1, 0xF0)]-1;
		final int hundreds = invtab[getInt(data_block, offset+17, 1, 0xF0)]-1;
		final int thousands = invtab[getInt(data_block, offset+17, 1, 0x0F)<<4]-1;
		final int units = invtab[getInt(data_block, offset+18, 1, 0xF0)]-1;
		final int tens = invtab[getInt(data_block, offset+18, 1, 0x0F)<<4]-1;

		final long mjd= (((((((tenthousands*10)+thousands)*10)+hundreds)*10)+tens)*10)+units;
		long y =  (long) ((mjd  - 15078.2) / 365.25);
		long m =  (long) ((mjd - 14956.1 - (long)(y * 365.25) ) / 30.6001);
		final long d =  (mjd - 14956 - (long)(y * 365.25) - (long)(m * 30.6001));
		final long k =  ((m == 14) || (m == 15)) ? 1 : 0;
		y = y + k + 1900;
		m = m - 1 - (k*12);

		s.add(new DefaultMutableTreeNode(new KVP("Modified Julian Date",mjd,y+"/"+m+"/"+d)));
		final int hoursUnits = invtab[getInt(data_block, offset+19, 1, 0xF0)]-1;
		final int hoursTens = invtab[getInt(data_block, offset+19, 1, 0x0F)<<4]-1;
		final int minutesUnits = invtab[getInt(data_block, offset+20, 1, 0xF0)]-1;
		final int minutesTens = invtab[getInt(data_block, offset+20, 1, 0x0F)<<4]-1;
		final int secondsUnits = invtab[getInt(data_block, offset+21, 1, 0xF0)]-1;
		final int secondsTens = invtab[getInt(data_block, offset+21, 1, 0x0F)<<4]-1;
		final StringBuilder utc = new StringBuilder().append(hoursTens).append(hoursUnits).append(':').append(minutesTens).append(minutesUnits).append(':').append(secondsTens).append(secondsUnits);
		s.add(new DefaultMutableTreeNode(new KVP("Universal Time Co-ordinated",utc.toString(),null)));
	}


	/**
	 * @param s
	 */
	private void addInititalPagePacket8_30ToJTree(final DefaultMutableTreeNode s, final int modus) {
		final int pageNumber=getPageNumber(7);
		final int subPage = getSubPage(9);
		final int m=getMagazineComplement(10)^getMagazineNo();
		final String formattedPageNo = formatPageNo(m,pageNumber,subPage);
		final DefaultMutableTreeNode initialTxtPage=new DefaultMutableTreeNode(new KVP("Initial Teletext Page",formattedPageNo,null));
		s.add(initialTxtPage);

		initialTxtPage.add(new DefaultMutableTreeNode(new KVP("pageNumber",pageNumber,null)));
		initialTxtPage.add(new DefaultMutableTreeNode(new KVP("sub page",subPage,null)));
		initialTxtPage.add(new DefaultMutableTreeNode(new KVP("magazine modified",m,null)));
	}

	/**
	 * @param m magazine number (0..7)
	 * @param pageNumber pagenumber (hex)
	 * @param subPage
	 * @return String whith pagenumber in form "mpp:ssss"
	 */
	public static String formatPageNo(final int m, final int pageNumber, final int subPage) {
		final StringBuilder b=new StringBuilder();

		if(pageNumber!=0xff){
			if(m==0){
				b.append("8");
			}else{
				b.append(m);
			}
			b.append(toHexStringUnformatted(pageNumber, 2));
			if(subPage!=0x3f7f){
				b.append(':').append(toHexStringUnformatted(subPage, 4));
			}
		}else{
			b.append(" - ");
		}

		return b.toString();
	}


	/**
	 * @param flags
	 * @param string
	 */
	private static void buildStringList(final StringBuilder flags, final String string) {
		if(flags.length()!=0){
			flags.append(",");
		}
		flags.append(string);
	}


	public DefaultMutableTreeNode getHTMLJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP(getTeletextHTML(),getTeletextPlain()));
		super.addDetailsToJTree(s, modus);
		addDetailsToJTree(s,modus);
		return s;
	}

	public byte[] getHeaderDataBytes(){
		final byte[] r = new byte[32];
		for (int i = 14; i < 46; i++) {
			r[i-14] = (byte)(invtab[getUnsignedByte(data_block[i+offset])]&0x7f);
		}
		return r;
	}

	public byte[] getDataBytes(final int start, final int end){
		final byte[] r = new byte[end-start];
		for (int i = start; i < end; i++) {
			r[i-start] = (byte)(invtab[getUnsignedByte(data_block[i+offset])]&0x7f);
		}
		return r;
	}

	public byte getRawByte(final int i){
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



	private static String getTeletextPlain(final byte[] b){
		final StringBuilder buf = new StringBuilder();
		for (final byte ch : b) {
			if((ch>=32)&&(ch<127)){ // For text version of single line  national option charset subset is ignored because it can not be derived from only the line itself. Need pageheader for that.
				buf.append((char)ch);
			}else{  // empty space
				buf.append(' ');
			}
		}

		return buf.toString();
	}


	protected String getTeletextHTML(final byte[] b){
		String bg="black";
		String fg="white";
		final StringBuilder buf = new StringBuilder("<code><b><span style=\"background-color: black; color: white; \">");
		for (final byte ch : b) {
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

	public static int getColorInt(final int i){
		return Integer.decode("0x"+getRawColorString(i));

	}

	public static String getHTMLColorString(final int i){
		return "#"+getRawColorString(i);

	}
	public static String getRawColorString(final int i){
		switch (i) {
		case 0: return "000000";
		case 1: return "ff0000";
		case 2: return "00ff00";
		case 3: return "ffff00";
		case 4: return "0000ff";
		case 5: return "ff00ff";
		case 6: return "00ffff";
		case 7: return "ffffff";

		case 8: return "000000";
		case 9: return "770000";
		case 10: return "007700";
		case 11: return "777700";
		case 12: return "000077";
		case 13: return "770077";
		case 14: return "007777";
		case 15: return "777777";

		case 16: return "ff0055";
		case 17: return "ff7700";
		case 18: return "00ff77";
		case 19: return "ffffbb";
		case 20: return "00ccaa";
		case 21: return "550000";
		case 22: return "665522";
		case 23: return "cc7777";

		case 24: return "333333";
		case 25: return "ff7777";
		case 26: return "77ff77";
		case 27: return "ffff77";
		case 28: return "7777ff";
		case 29: return "ff77ff";
		case 30: return "77ffff";
		case 31: return "dddddd";

		default: return "ffffff";
		}
	}


	private static String getDataChannelString(final int i){
		switch (i) {
		case 0: return "Packet 8/30";
		case 1: return "Packet 1/30";
		case 2: return "Packet 2/30";
		case 3: return "Packet 3/30";
		case 4: return "Low bit rate audio";
		case 5: return "Datavideo";
		case 6: return "Datavideo";
		case 7: return "Packet 7/30";
		case 8: return "IDL Format A or B";
		case 9: return "IDL Format A or B";
		case 10: return "IDL Format A or B";
		case 11: return "IDL Format A or B";
		case 12: return "Low bit rate audio";
		case 13: return "Datavideo";
		case 14: return "Datavideo";
		case 15: return "IDL Format B";
		default: return "Illegal value";
		}
	}

	public byte[] getPageDataBytes(){
		final byte[] r = new byte[40];
		for (int i = 6; i < 46; i++) {
			r[i-6] = (byte)(invtab[getUnsignedByte(data_block[i+offset])]&0x7f);
		}
		return r;
	}

	public byte getPageDataByte(final int i){
		return (byte)(invtab[getUnsignedByte(data_block[i+offset+6])]&0x7f);
	}

	public int getPageNumberUnits(){
		return getPageNumberUnits(6);
	}

	public int getPageNumberUnits(final int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset]);
	}

	public int getPageNumberTens(){
		return getPageNumberTens(7);
	}

	public int getPageNumberTens(final int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset]);
	}

	public int getPageNumber(){
		return getPageNumber(6);
	}

	public int getPageNumber(final int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset])+(16*getHammingReverseByte(data_block[localOffset+1+offset]));
	}

	public int getSubPage(){
		return getSubPage(8);
	}

	public int getSubPage(final int localOffset){
		return getHammingReverseByte(data_block[localOffset+offset]) +(16*(getHammingReverseByte(data_block[localOffset+1+offset])&0x7)) +(256*getHammingReverseByte(data_block[localOffset+2+offset]))+ (4096*(getHammingReverseByte(data_block[localOffset+3+offset])&0x3));
	}

	public int getPageFunction(){ // assumses packet 28/0
		final Triplet tr = new Triplet(data_block, offset + 7); // triplet 1, at 4 + 1*3
		return tr.getPageFunction();
	}


	private int getMagazineComplement(final int localOffset){
		final int m1 = (getUnsignedByte(data_block[offset+localOffset]) &0x01);
		final int m2 = (getUnsignedByte(data_block[offset+localOffset+2]) &0x04)>>1;
		final int m3 = (getUnsignedByte(data_block[offset+localOffset+2]) &0x01)<<2;
		return m1|m2|m3;
	}

	public int getDesignationCode(){
		return getHammingReverseByte(data_block[6+offset]);
	}

	public int getMagazineNo(){
		final int magazine_and_packet_address = getInt(data_block, 4+offset, 2,MASK_16BITS);
		int r = (magazine_and_packet_address & 0x4000) >>14 ;
		r |= (magazine_and_packet_address & 0x1000) >>11 ;
			r |= (magazine_and_packet_address & 0x0400) >>8 ;
		return r;

	}
	public int getPacketNo(){
		final int magazine_and_packet_address = getInt(data_block, 4+offset, 2,MASK_16BITS);
		int r = (magazine_and_packet_address & 0x0100) >> 8;
		r |= (magazine_and_packet_address & 0x0040) >> 5;
		r |= (magazine_and_packet_address & 0x0010) >>2 ;
		r |= (magazine_and_packet_address & 0x0004)<<1 ;
		r |= (magazine_and_packet_address & 0x0001)<<4 ;
		return r;

	}

	//ETSI EN 300 708 V1.2.1 § 6.4
	public int getDataChannel(){
		final int magazine_and_packet_address = getInt(data_block, 4+offset, 2,MASK_16BITS);
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
		final StringBuilder b= new StringBuilder("TxtDataField ,Magazine:").append(getMagazineNo()).append("getPacketNo():").append(getPacketNo());
		if(getPacketNo()==0){
			b.append(", page=").append(getPageNumber()).append(", subPage=").append(getSubPage());
		}
		return b.toString();
	}


	protected void addPDCDetails(final DefaultMutableTreeNode node, final int modus, final int localOffset) {
		final DefaultMutableTreeNode s = new DefaultMutableTreeNode(new KVP("TV programme identification data for VCR control"));
		node.add(s);

		s.add(new DefaultMutableTreeNode(new KVP("Label channel identifier",getLabelChannelIdentifier(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("Label Update Flag",getLabelUpdateFlag(localOffset),(getLabelUpdateFlag(localOffset)==1)?"label does not relate to the current television programme, but is intended to update the label memories in video recorders":"label does relate to the current television programme")));
		s.add(new DefaultMutableTreeNode(new KVP("Prepare to Record Flag",getPrepareToRecordFlag(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("Status of analogue sound",getStatusOfAnalogueSound(localOffset),VPSDataField.getPCSAudioString(getStatusOfAnalogueSound(localOffset)))));
		s.add(new DefaultMutableTreeNode(new KVP("Mode identifier",getModeIdentifier(localOffset),(getModeIdentifier(localOffset)==1)?"service code takes immediate effect":"effect of service codes is delayed by 30 s")));
		s.add(new DefaultMutableTreeNode(new KVP("CNI Country",getCNICountry(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("CNI Network",getCNINetwork(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("day",getDay(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("month",getMonth(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("hour",getHour(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("minute",getMinute(localOffset),null)));
		s.add(new DefaultMutableTreeNode(new KVP("PTY",getPTY(localOffset),null)));
	}


	protected int getCNICountry(final int localOffset) {
		return (16*getHammingByte(data_block[localOffset+offset+2]))
				+ (4*(getHammingByte(data_block[localOffset+offset+8])&0x03))
				+ ((getHammingByte(data_block[localOffset+offset+9])&0x0C)>>2);
	}


	protected int getCNINetwork(final int localOffset) {
		return ((64*(getHammingByte(data_block[localOffset+offset+3])&0xC))>>2)
				+ (16*((getHammingByte(data_block[localOffset+offset+9])&0x03)))
				+ getHammingByte(data_block[localOffset+offset+10]);
	}


	protected int getLabelChannelIdentifier(final int localOffset) {
		return (getHammingByte(data_block[localOffset+offset])&0xC)>>2;
	}

	protected int getLabelUpdateFlag(final int localOffset) {
		return (getHammingByte(data_block[localOffset+offset])&0x2)>>1;
	}

	protected int getPrepareToRecordFlag(final int localOffset) {
		return (getHammingByte(data_block[localOffset+offset])&0x1);
	}

	protected int getStatusOfAnalogueSound(final int localOffset) {
		return (getHammingByte(data_block[localOffset+offset+1])&0xC)>>2;
	}

	protected int getModeIdentifier(final int localOffset) {
		return (getHammingByte(data_block[localOffset+offset+1])&0x2)>>1;
	}

	protected int getDay(final int localOffset) {
		return (8*(getHammingByte(data_block[localOffset+offset+3])&0x3))+((getHammingByte(data_block[localOffset+offset+4])&0xe)>>1);
	}
	protected int getMonth(final int localOffset) {
		return (2*(getHammingByte(data_block[localOffset+offset+4])&0x1))+((getHammingByte(data_block[localOffset+offset+5])&0xe)>>1);
	}
	protected int getHour(final int localOffset) {
		return (16*(getHammingByte(data_block[localOffset+offset+5])&0x1))+getHammingByte(data_block[localOffset+offset+6]);
	}
	protected int getMinute(final int localOffset) {
		return (4*getHammingByte(data_block[localOffset+offset+7]))+((getHammingByte(data_block[localOffset+offset+8])&0xC)>>2);
	}
	protected int getPTY(final int localOffset) {
		return (16*getHammingByte(data_block[localOffset+offset+11]))+getHammingByte(data_block[localOffset+offset+12]);
	}

	private static String getTimeOffsetCodeString(final int timeOffset){
		final int uren = invtab[(timeOffset&0x1E)<<3];
		final StringBuilder b = new StringBuilder().append(uren);
		if((timeOffset&0x20)!=0){
			b.append('½');
		}
		if((timeOffset&0x01)!=0){
			b.append(" hour(s), negative offset (west of Greenwich)");
		}else{
			b.append(" hour(s), positive offset (east of Greenwich)");
		}

		return b.toString();
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
		final TxtDataField other = (TxtDataField) obj;
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
		if (reserved_future_use != other.reserved_future_use) {
			return false;
		}
		return true;
	}



	public int getColor(final int c){
		final List<Triplet> tripletList= getTripletList();
		final BitString bs = new BitString();
		// bits 11-18 of triplet 2
		bs.addIntBitsReverse(tripletList.get(1).getVal() & (0x3FC00 >> 10), 8);
		for (int i = 3; i <= 12; i++) {
			bs.addIntBitsReverse(tripletList.get(i - 1).getVal(), 18);
		}
		// bits 1-4 of triplet 13
		bs.addIntBitsReverse(tripletList.get(12).getVal() & 0xF, 4);


		for (int i = 16; i <= 31; i++) {
			final int r = bs.getIntBitsReverse(4); // 0..15
			final int g = bs.getIntBitsReverse(4);
			final int b = bs.getIntBitsReverse(4);
			if(i==c){

				final int r_byte = (r * 255) / 15;
				final int g_byte = (g * 255) / 15;
				final int b_byte = (b * 255) / 15;
				return (r_byte<<16) | (g_byte<<8) |b_byte;
			}

		}

		return 0;
	}


	/**
	 * @return
	 */
	public boolean isBlackBackGroundColorSubstitution() {
		final Triplet tr13 = new Triplet(data_block, offset + 4 + (13 * 3));
		final int blackBackgroundColourSubstitution = (tr13.getVal() & 0x4000) >> 14;
				return blackBackgroundColourSubstitution==1;
	}

	public int getDefaultScreenColour() {
		final Triplet tr13 = new Triplet(data_block, offset + 4 + (13 * 3));
		return (tr13.getVal() & 0x1F0) >> 4;
	}

	public int getDefaultRowColour() {
		final Triplet tr13 = new Triplet(data_block, offset + 4 + (13 * 3));
		return (tr13.getVal() & 0x3E00) >> 9;
	}
}