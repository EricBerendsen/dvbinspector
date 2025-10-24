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

package nl.digitalekabeltelevisie.data.mpeg.pes.video.common;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.LookUpList;

public class Construct implements TreeNode, HTMLSource{
	
	private static final Logger logger = Logger.getLogger(Construct.class.getName());

	public static LookUpList cc_type_list = new LookUpList.Builder().
	add(0, "NTSC_CC_FIELD_1").
	add(1, "NTSC_CC_FIELD_2").
	add(2, "DTVCC_PACKET_DATA").
	add(3, "DTVCC_PACKET_START").
	build();

	public static LookUpList control_code_list = new LookUpList.Builder().
	add(0, "resume caption loading").
	add(1, "backspace").
	add(2, "alarm off").
	add(3, "alarm on").
	add(4, "delete to end of row").
	add(5, "roll up 2").
	add(6, "roll up 3").
	add(7, "roll up 4").
	add(8, "flashes captions on").
	add(9, "resume direct captioning").
	add(10, "text restart").
	add(11, "resume text display").
	add(12, "erase display memory").
	add(13, "carriage return").
	add(14, "erase non displayed memory").
	add(15, "end of caption").
	build();
	
	public static LookUpList row_list = new LookUpList.Builder().
			add(0b0000,"Row 11").
			add(0b0001," -").
			add(0b0010,"Row 1").
			add(0b0011,"Row 2").
			add(0b0100,"Row 3").
			add(0b0101,"Row 4").
			add(0b1010,"Row 5").
			add(0b1011,"Row 6").
			add(0b1100,"Row 7").
			add(0b1101,"Row 8").
			add(0b1110,"Row 9").
			add(0b1111,"Row 10").
			add(0b0110,"Row 12").
			add(0b0111,"Row 13").
			add(0b1000,"Row 14").
			add(0b1001,"Row 15").
			build();
	
	public static LookUpList style_list  = new LookUpList.Builder().
			add(0,"White").
			add(1,"Green").
			add(2,"Blue").
			add(3,"Cyan").
			add(4,"Red").
			add(5,"Yellow").
			add(6,"Magenta").
			add(7,"Italics").
			build();
	
	public static LookUpList xds_class_raw = new LookUpList.Builder().
	add(0x01, "Start Current").
	add(0x02, "Continue Current").
	add(0x03, "Start Future").
	add(0x04, "Continue Future").
	add(0x05, "Start Channel").
	add(0x06, "Continue Channel").
	add(0x07, "Start Miscellaneous").
	add(0x08, "Continue Miscellaneous").
	add(0x09, "Start Public Service").
	add(0x0a, "Continue Public Service").
	add(0x0b, "Start Reserved").
	add(0x0c, "Continue Reserved").
	add(0x0d, "Start Private Data").
	add(0x0e, "Continue Private Data").
	add(0x0f, "End ALL").
	build();
	

	public static LookUpList xds_class = new LookUpList.Builder().
	add(0x00, "Current").
	add(0x01, "Future").
	add(0x02, "Channel").
	add(0x03, "Miscellaneous").
	add(0x04, "Public Service").
	add(0x05, "Reserved").
	add(0x06, "Private Data").

	build();
	

	public static LookUpList current_type = new LookUpList.Builder().
	add(0x01, "Program Identification Number").
	add(0x02, "Length/Time-in-Show").
	add(0x03, "Program Name (Title) ").
	add(0x04, "Program Type").
	add(0x05, "Content Advisory").
	add(0x06, "Audio Services").
	add(0x07, "Caption Services").
	add(0x08, "Copy Generation Management System (Analog)").
	add(0x09, "Aspect Ratio Information").
	add(0x0a, "-").
	add(0x0b, "-").
	add(0x0c, "Composite Packet-1").
	add(0x0d, "Composite Packet-2").
	add(0x0e, "-").
	add(0x0f, "-").
	add(0x10, "Program Description Row 1").
	add(0x11, "Program Description Row 2").
	add(0x12, "Program Description Row 3").
	add(0x13, "Program Description Row 4").
	add(0x14, "Program Description Row 5").
	add(0x15, "Program Description Row 6").
	add(0x16, "Program Description Row 7").
	add(0x17, "Program Description Row 8").
	build();
	
	public static LookUpList future_type = new LookUpList.Builder().
	add(0x01, "Program Identification Number").
	add(0x02, "Length/Time-in-Show").
	add(0x03, "Program Name (Title) ").
	add(0x04, "Program Type").
	add(0x05, "Content Advisory").
	add(0x06, "Audio Services").
	add(0x07, "Caption Services").
	add(0x08, "Copy Generation Management System (Analog)").
	add(0x09, "Aspect Ratio Information").
	add(0x0a, "-").
	add(0x0b, "-").
	add(0x0c, "Composite Packet-1").
	add(0x0d, "Composite Packet-2").
	add(0x0e, "-").
	add(0x0f, "-").
	add(0x10, "Program Description Row 1").
	add(0x11, "Program Description Row 2").
	add(0x12, "Program Description Row 3").
	add(0x13, "Program Description Row 4").
	add(0x14, "Program Description Row 5").
	add(0x15, "Program Description Row 6").
	add(0x16, "Program Description Row 7").
	add(0x17, "Program Description Row 8").
	add(0x50, "Minor Channel Number").
	add(0x51, "Event Number").
	add(0x52, "Event Start Time").
	add(0x53, "Event Duration").
	add(0x54, "Program Title").
	add(0x55, "Program Type").
	add(0x56, "Content Advisory").
	add(0x57, "Audio Services").
	add(0x58, "Caption Services").
	add(0x60, "Multiple String Structure Control").
	add(0x61, "EIT Descriptor Information").
	add(0x62,0x6e,"Reserved").
	add(0x6f,"Data Carriage Packet").
	build();
	
	public static LookUpList channel_type = new LookUpList.Builder().
			add(0x01, "Network Name (Affiliation)").
			add(0x02, "Call Letters (Station ID) and Native Channel").
			add(0x03, "Tape Delay").
			add(0x04, "Transmission Signal Identifier (TSID)").
			build();

	public static LookUpList misc_type = new LookUpList.Builder().
			add(0x01, "Time of Day").
			add(0x02, "Impulse Capture ID").
			add(0x03, "Supplemental Data Location").
			add(0x04, "Local Time Zone & DST Use").
			build();

	
	public static LookUpList public_service_type = new LookUpList.Builder().
			add(0x01, "National Weather Service Code (WRSAME)").
			add(0x02, "National Weather Service Message").

			build();

	static Map<Integer,LookUpList> lookupType = new TreeMap<>(); 
	
	static {
		lookupType.put(0, current_type);
		lookupType.put(1, future_type);
		lookupType.put(2, channel_type);
		lookupType.put(3, misc_type);
		lookupType.put(4, public_service_type);
	}
	
	
	
	static public String getTypeDescription(int class1, int type) {
		LookUpList classLookup = lookupType.get(class1);
		if(classLookup==null) {
			return "unknown";
		}
		return classLookup.get(type, "unknown");
		
	}

	private int one_bit;// (set to '1")
	private int reserved;
	private int cc_valid;
	public int getCc_valid() {
		return cc_valid;
	}

	public int getCc_type() {
		return cc_type;
	}

	public int getCc_data_1() {
		return cc_data_1;
	}

	public int getCc_data_2() {
		return cc_data_2;
	}

	private int cc_type;
	private int cc_data_1;
	private int cc_data_2;
	/**
	 * @param data
	 * @param localOffset
	 */
	public Construct(final byte[] data, final int localOffset) {
		if(localOffset<data.length){
			one_bit = getInt(data,localOffset,1,0x80)>>7;
		reserved = getInt(data,localOffset,1,0x71)>>3;
		cc_valid = getInt(data,localOffset,1,0x04)>>2;
		cc_type = getInt(data,localOffset,1,0x03);
		}
		if((localOffset+1)<data.length){
			cc_data_1 = getInt(data,localOffset+1,1,MASK_8BITS);
		}
		if((localOffset+2)<data.length){
			cc_data_2 = getInt(data,localOffset+2,1,MASK_8BITS);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("construct");
		t.addHTMLSource(this, "Construct");
		t.add(new KVP("one_bit",one_bit,"shall be '1' to maintain backwards compatibility with previous versions of CEA-708-C"));
		t.add(new KVP("reserved",reserved));
		t.add(new KVP("cc_valid",cc_valid,cc_valid==1?"the two closed caption data bytes that follow are valid":"the two data bytes are invalid"));
		t.add(new KVP("cc_type",cc_type, Construct.cc_type_list.get(cc_type)));
		t.add(new KVP("cc_data_1",cc_data_1));
		t.add(new KVP("cc_data_2",cc_data_2));

		return t;
	}

	@Override
	public String getHTML() {
		String ccString = interpretCCDataBytes();

		return String.format("Type: %1$d [%2$s], cc_valid: %3$d, cc_data_1: %4$#04x, cc_data_2: %5$#04x, %6$s", cc_type, Construct.cc_type_list.get(cc_type), cc_valid, cc_data_1, cc_data_2, ccString);
	}

	/**
	 * @return
	 */
	public int getCC1WithoutParity() {
		return cc_data_1 &0x7F;
	}

	/**
	 * @return
	 */
	public int getCC2WithoutParity() {
		return cc_data_2 &0x7F;
	}

	private String interpretCCDataBytes() {
		if(cc_type >=2) { // only support cea608
			return "";
		}
		if(isControl()) { // control command
			int field = 1 + (cc_data_1&0b001);
			int controlCode = (cc_data_2&0b1111);
			return String.format("[Control Command, channel: %1$d, field%2$d ], code:  %3$d (%4$s)",getChannel(),field, controlCode,control_code_list.get(controlCode,"???"));
		}else if(isTab()) {
			int tabOffset = (cc_data_2&0b0000_0011);
			return String.format("[Tab Offset, channel: %1$d], offset:  %2$d ",getChannel(),tabOffset);
		}else if(isRowPreambleStyle()) {
			return String.format("RowPreambleStyle: channel: %1$d, row: %2$d, next row down toggle: %3$d [%6$s], style: %4$d [%5$s], underline: %6$d",getChannel(),getRow(),getNextRowDownToggle(), getStyle(), style_list.get(getStyle()), getUnderline(),row_list.get(getRow()*2+getNextRowDownToggle(),"???"));
		}else if(isRowPreambleAddress()) {
			return String.format("RowPreambleAddress: channel: %1$d, row: %2$d, next row down toggle: %3$d [%6$s], cursor:%4$d [%7$s] , underline: %5$d",getChannel(),getRow(),getNextRowDownToggle(), getCursor(), getUnderline(),row_list.get(getRow()*2+getNextRowDownToggle(),"?x?"),"Indent "+(getCursor()*4));
		}else if(cc_data_1 == 0x80 && cc_data_2 == 0x80) {
			return "null pad"; 
		}else if(isXDSControl()) { 
			String res = "XDS metadata, class:"+Construct.xds_class_raw.get(getCC1WithoutParity());
			if(getCC1WithoutParity()==1){
				res =res + ",type:"+Construct.current_type.get(getCC2WithoutParity());
			}
			return res; 
		}else if(isMidRowStyleCode()) {
			return String.format("Mid Row Code: channel: %1$d, style: %2$d [%3$s], underline: %4$d",getChannel(),getStyle(), style_list.get(getStyle(),"??"), getUnderline());
			
		}else if(isSpecialNorthAmericancharacter()) {
			return String.format("Special North American character: channel: %1$d, char: %2$c", getChannel(), Cea608.SPECIAL_CHARACTER_SET[cc_data_2 & 0b0000_1111]);
		}else if(isStandardChar()) {
			return formatStandardChar();
		}else {
			String msg = String.format("Not implemented: cc_data_1: %1$#04x, cc_data_2: %2$#04x", getCC1WithoutParity(), getCC2WithoutParity());
			logger.warning(msg);
			return msg;
		}
	}


	private boolean isStandardChar() {
		return (cc_data_1 & 0b0110_0000) != 0 ;
	}
	
	private String formatStandardChar() {
		final int cc1WithoutParity = getCC1WithoutParity();
		final int cc2WithoutParity = getCC2WithoutParity();
		if((cc2WithoutParity != 0) && (cc2WithoutParity <0x20)) {
			String s = String.format("char less than 0x20, cc_data1: %1$#04x, cc_data1: %2$#04x",cc1WithoutParity, cc2WithoutParity);
			logger.warning( s);
			return s;
		}
		if(cc2WithoutParity != 0){
			return String.format("%1$c %2$c", Cea608.BASIC_CHARACTER_SET[cc1WithoutParity-0x20],Cea608.BASIC_CHARACTER_SET[cc2WithoutParity-0x20]);
		}
		return String.format("%1$c [pad]",  Cea608.BASIC_CHARACTER_SET[cc1WithoutParity-0x20]);
	}

	/**
	 * @return
	 */
	int getUnderline() {
		return cc_data_2 & 0b0000_0001;
	}

	/**
	 * @return
	 */
	int getStyle() {
		return (cc_data_2 & 0b0000_1110) >>> 1;
	}

	/**
	 * @return
	 */
	int getCursor() {
		return (cc_data_2 & 0b0000_0110) >>> 1;
	}
	/**
	 * @return
	 */
	int getNextRowDownToggle() {
		return (cc_data_2 & 0b0010_0000) >>> 5;
	}

	/**
	 * @return
	 */
	int getRow() {
		return cc_data_1&0b0000_0111;
	}

	/**
	 * @return
	 */
	int getChannel() {
		return 1+((cc_data_1&0b1000)>>>3);
	}
	
	
	//+-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
	//|P|0|0|1|C|0|0|1| |P|0|1|1|  CHAR |
	//+-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
	//15             8   7             0

	private boolean isSpecialNorthAmericancharacter() {
		return     ((cc_data_1 & 0b0111_0111) == 0b0001_0001)
				&& ((cc_data_2 & 0b0111_0000) == 0b0011_0000);
	}

	
	
	//             +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
	//midrow style |P|0|0|1|C|0|0|1| |P|0|1|0|STYLE|U|
    //	           +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
    //             15             8   7             0

	public boolean isMidRowStyleCode() {
		return     ((cc_data_1 & 0b0111_0111) == 0b0001_0001)
				&& ((cc_data_2 & 0b0111_0000) == 0b0010_0000);
	}

	/**
	 * @return
	 */
	
	//        cc_data 1                  CC_data_2
	//15 14 13 12    11 10 9 8       7 6 5 4    3 2 1 0
	//p   0  0  1     c  1 0 f       p 0 1 0    d d d d    isControl

	public boolean isControl() {
		return     ((cc_data_1 & 0b0111_0110) == 0b0001_0100)
				&& ((cc_data_2 & 0b0111_0000) == 0b0010_0000);
	}

	//        cc_data 1                  CC_data_2
	//15 14 13 12    11 10 9 8       7 6 5 4    3 2 1 0
	//p   0  0  1     c  1 1 1       p 0 1 0    0 0 d d    isTab
	
	public boolean isTab() {
		return     ((cc_data_1 & 0b0111_0111) == 0b0001_0111)
				&& ((cc_data_2 & 0b0111_1100) == 0b0010_0000); 
	}


	//                 +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+                  +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
	//	preamble style |P|0|0|1|C|0|ROW| |P|1|N|0|STYLE|U| preamble address |P|0|0|1|C|0|ROW| |P|1|N|1|CURSR|U|
	//				   +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+                  +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+

	//
	//
	public boolean isRowPreambleStyle(){
		return     ((cc_data_1 & 0b0111_0000) == 0b0001_0000)
				&& ((cc_data_2 & 0b0101_0000) == 0b0100_0000); 
	}

	
	public boolean isRowPreambleAddress(){
		return     ((cc_data_1 & 0b0111_0000) == 0b0001_0000) 
				&& ((cc_data_2 & 0b0101_0000) == 0b0101_0000); 
	}
	
	
	/**
	 * @return
	 */
	public boolean isXDSControl() { //  When any of the control codes from 01h to 0Fh is used to begin a control-character pair it indicates the beginning of XDS Data
		return (cc_type == 1) && ((cc_data_1 & 0b111_0000) == 0b0000);
	}

}