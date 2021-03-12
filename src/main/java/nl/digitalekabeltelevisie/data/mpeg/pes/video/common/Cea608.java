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

import static nl.digitalekabeltelevisie.util.Utils.printTimebase90kHz;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.*;

public class Cea608 {

	protected Map<Integer,Map<Long, List<Construct>>> allCcData = new TreeMap<>();
	protected Map<Integer,Map<Integer, List<byte[]>>> xdsData = new TreeMap<>();


	
	// Basic North American 608 CC char set, mostly ASCII. Indexed by (char-0x20).
	// https://github.com/shaheenkdr/ExoDemo/blob/master/library/src/main/java/com/google/android/exoplayer2/text/cea/Cea608Decoder.java

	static final int[] BASIC_CHARACTER_SET = new int[] {
	    0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27,     //   ! " # $ % & '
	    0x28, 0x29,                                         // ( )
	    0xE1,       // 2A: 225 'á' "Latin small letter A with acute"
	    0x2B, 0x2C, 0x2D, 0x2E, 0x2F,                       //       + , - . /
	    0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,     // 0 1 2 3 4 5 6 7
	    0x38, 0x39, 0x3A, 0x3B, 0x3C, 0x3D, 0x3E, 0x3F,     // 8 9 : ; < = > ?
	    0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,     // @ A B C D E F G
	    0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0x4E, 0x4F,     // H I J K L M N O
	    0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57,     // P Q R S T U V W
	    0x58, 0x59, 0x5A, 0x5B,                             // X Y Z [
	    0xE9,       // 5C: 233 'é' "Latin small letter E with acute"
	    0x5D,                                               //           ]
	    0xED,       // 5E: 237 'í' "Latin small letter I with acute"
	    0xF3,       // 5F: 243 'ó' "Latin small letter O with acute"
	    0xFA,       // 60: 250 'ú' "Latin small letter U with acute"
	    0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67,           //   a b c d e f g
	    0x68, 0x69, 0x6A, 0x6B, 0x6C, 0x6D, 0x6E, 0x6F,     // h i j k l m n o
	    0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77,     // p q r s t u v w
	    0x78, 0x79, 0x7A,                                   // x y z
	    0xE7,       // 7B: 231 'ç' "Latin small letter C with cedilla"
	    0xF7,       // 7C: 247 '÷' "Division sign"
	    0xD1,       // 7D: 209 'Ñ' "Latin capital letter N with tilde"
	    0xF1,       // 7E: 241 'ñ' "Latin small letter N with tilde"
	    0x25A0      // 7F:         "Black Square" (NB: 2588 = Full Block)
	  };
	// Special North American 608 CC char set.
	  static final int[] SPECIAL_CHARACTER_SET = new int[] {
	    0xAE,    // 30: 174 '®' "Registered Sign" - registered trademark symbol
	    0xB0,    // 31: 176 '°' "Degree Sign"
	    0xBD,    // 32: 189 '½' "Vulgar Fraction One Half" (1/2 symbol)
	    0xBF,    // 33: 191 '¿' "Inverted Question Mark"
	    0x2122,  // 34:         "Trade Mark Sign" (tm superscript)
	    0xA2,    // 35: 162 '¢' "Cent Sign"
	    0xA3,    // 36: 163 '£' "Pound Sign" - pounds sterling
	    0x266A,  // 37:         "Eighth Note" - music note
	    0xE0,    // 38: 224 'à' "Latin small letter A with grave"
	    0x20,    // 39:         TRANSPARENT SPACE - for now use ordinary space
	    0xE8,    // 3A: 232 'è' "Latin small letter E with grave"
	    0xE2,    // 3B: 226 'â' "Latin small letter A with circumflex"
	    0xEA,    // 3C: 234 'ê' "Latin small letter E with circumflex"
	    0xEE,    // 3D: 238 'î' "Latin small letter I with circumflex"
	    0xF4,    // 3E: 244 'ô' "Latin small letter O with circumflex"
	    0xFB     // 3F: 251 'û' "Latin small letter U with circumflex"
	  };
	  
	LookUpList mpa_rating_lookup = new LookUpList.Builder().
			add(0,"N/A").
			add(1,"G").
			add(2,"PG").
			add(3,"PG-13").
			add(4,"R").
			add(5,"NC-17").
			add(6,"X").
			add(7,"Not Rated").
			build();
	
	LookUpList ustv_rating_lookup = new LookUpList.Builder().
			add(0,"None").
			add(1,"TV-Y (All Children)").
			add(2,"TV-Y7 (Directed to Older Children)").
			add(3,"TV-G (General Audience)").
			add(4,"TV-PG (Parental Guidance Suggested)").
			add(5,"TV-14 (Parents Strongly Cautioned)").
			add(6,"TV-MA (Mature Audience Only)").
			add(7,"None").
			build();

	LookUpList cgms_lookup = new LookUpList.Builder().
			add(0,"Copying is permitted without restriction").
			add(1,"Condition not to be used").
			add(2,"One generation of copies may be made").
			add(3,"No copying is permitted").
			build();
	
	LookUpList aps_lookup = new LookUpList.Builder().
			add(0,"No APS").
			add(1,"PSP On; Split Burst Off").
			add(2,"PSP On; 2 line Split Burst On").
			add(3,"PSP On; 4 line Split Burst On").
			build();
	
	LookUpList program_type_lookup = new LookUpList.Builder().
			add(0x20,"Education").
			add(0x21,"Entertainment").
			add(0x22,"Movie").
			add(0x23,"News").
			add(0x24,"Religious").
			add(0x25,"Sports").
			add(0x26,"OTHER").
			add(0x27,"Action").
			add(0x28,"Advertisement").
			add(0x29,"Animated").
			add(0x2A,"Anthology").
			add(0x2B,"Automobile").
			add(0x2C,"Awards").
			add(0x2D,"Baseball").
			add(0x2E,"Basketball").
			add(0x2F,"Bulletin").
			add(0x30,"Business").
			add(0x31,"Classical").
			add(0x32,"College").
			add(0x33,"Combat").
			add(0x34,"Comedy").
			add(0x35,"Commentary").
			add(0x36,"Concert").
			add(0x37,"Consumer").
			add(0x38,"Contemporary").
			add(0x39,"Crime").
			add(0x3A,"Dance").
			add(0x3B,"Documentary").
			add(0x3C,"Drama").
			add(0x3D,"Elementary").
			add(0x3E,"Erotica").
			add(0x3F,"Exercise").
			add(0x40,"Fantasy").
			add(0x41,"Farm").
			add(0x42,"Fashion").
			add(0x43,"Fiction").
			add(0x44,"Food").
			add(0x45,"Football").
			add(0x46,"Foreign").
			add(0x47,"Fund Raiser").
			add(0x48,"Game/Quiz").
			add(0x49,"Garden").
			add(0x4A,"Golf").
			add(0x4B,"Government").
			add(0x4C,"Health").
			add(0x4D,"High School").
			add(0x4E,"History").
			add(0x4F,"Hobby").
			add(0x50,"Hockey").
			add(0x51,"Home").
			add(0x52,"Horror").
			add(0x53,"Information").
			add(0x54,"Instruction").
			add(0x55,"International").
			add(0x56,"Interview").
			add(0x57,"Language").
			add(0x58,"Legal").
			add(0x59,"Live").
			add(0x5A,"Local").
			add(0x5B,"Math").
			add(0x5C,"Medical").
			add(0x5D,"Meeting").
			add(0x5E,"Military").
			add(0x5F,"Miniseries").
			add(0x60,"Music").
			add(0x61,"Mystery").
			add(0x62,"National").
			add(0x63,"Nature").
			add(0x64,"Police").
			add(0x65,"Politics").
			add(0x66,"Premier").
			add(0x67,"Prerecorded").
			add(0x68,"Product").
			add(0x69,"Professional").
			add(0x6A,"Public").
			add(0x6B,"Racing").
			add(0x6C,"Reading").
			add(0x6D,"Repair").
			add(0x6E,"Repeat").
			add(0x6F,"Review").
			add(0x70,"Romance").
			add(0x71,"Science").
			add(0x72,"Series").
			add(0x73,"Service").
			add(0x74,"Shopping").
			add(0x75,"Soap Opera").
			add(0x76,"Special").
			add(0x77,"Suspense").
			add(0x78,"Talk").
			add(0x79,"Technical").
			add(0x7A,"Tennis").
			add(0x7B,"Travel").
			add(0x7C,"Variety").
			add(0x7D,"Video").
			add(0x7E,"Weather").
			add(0x7F,"Western").			
			build();

	Function<byte[], String> cea608StringFormat = (byte[] input) -> {
		StringBuilder builder = new StringBuilder();
		for (byte b : input) {
			if (b >= 0x20) {
				builder.append((char) Cea608.BASIC_CHARACTER_SET[b - 0x20]);
			}
		}
		return builder.toString();
	};

	
	Function<byte[], String> cea608ProgramTypeFormat = (byte[] input) -> {
		StringBuilder builder = new StringBuilder();
		for (byte b : input) {	
			builder.append(program_type_lookup.get(b, "??")).append(' ');
		}
		return builder.toString().trim(); // remove last space
	};

	
	Function<byte[], String> cea608ProgramIdentificationNumberFormat = (byte[] input) -> {

		if (input.length != 4) {
			return "invalid length!";
		}
		int minutes = input[0] & 0b0011_1111;
		int hours = input[1] & 0b0001_1111;
		int date = input[2] & 0b0001_1111;
		int month = input[3] & 0b0000_1111;
		int tapeDelay = (input[3] & 0b0001_0000) >>> 4;
		return String.format("Month: %1$d, Day: %2$d, Hours: %3$d, Minutes: %4$d, Tape delay: %5$d (UTC)", month, date,
				hours, minutes, tapeDelay);
	};
	
	
	Function<byte[], String> cea608LengthTimeInShowFormat = (byte[] input) -> {
		if ((input.length != 2) && (input.length != 4) && (input.length != 6)) {
			return "invalid length!";
		}
		int minutesLength = input[0] & 0b0011_1111;
		int hoursLength = input[1] & 0b0011_1111;
		String res = String.format("Length Hours: %1$d, Minutes: %2$d", hoursLength, minutesLength);
		if (input.length > 2) {
			int minutesElapsed = input[2] & 0b0011_1111;
			int hoursElapsed = input[3] & 0b0011_1111;
			res += String.format(", Elapsed Hours: %1$d, Elapsed Minutes: %2$d", hoursElapsed, minutesElapsed);
			if (input.length > 4) {
				int secondsElapsed = input[4] & 0b0011_1111;
				res += String.format(", Elapsed Seconds: %1$d", secondsElapsed);
			}
		}
		return res;
	};
	
	Function<byte[], String> cea608ContentAdvisoryFormat = (byte[] input) -> {
		if (input.length != 2) {
			return "invalid length!";
		}
		String res = "System: ";
		int a1 = (input[0] & 0b0001_0000) >>>4 ;
		int a0 = (input[0] & 0b0000_1000) >>>3 ;
		
		if(a1 ==0 && a0 ==0) {
			res += "MPA, ";
			int ratingR = (input[0] & 0b0000_0111) ;
			res += mpa_rating_lookup.get(ratingR,"unknown value");
			return res;
		}
		if(a1 ==0 && a0 ==1) {
			res += "U.S. TV Parental Guidelines, ";
			int ratingG = (input[1] & 0b0000_0111) ;
			res += ustv_rating_lookup.get(ratingG,"unknown value");
			if((input[1] & 0b0010_0000) != 0)
				if(ratingG == 2) {  // TV-Y7
					res += ", FV (Fantasy Violence)";
				}else {
					res += ", V (Violence) ";
				}
			if((input[1] & 0b0001_0000) != 0) {
				res += ", S (Sexual Situations)"; 
			}
			if((input[1] & 0b0000_1000) != 0) {
				res += ", L (Adult Language)"; 
			}
			if((input[0] & 0b0010_0000) != 0) {
				res += ", D (Sexually Suggestive Dialog)"; 
			}
			return res;
		}
		
		return "? not implemented";
	};

	
	Function<byte[], String> cea608CopyGenerationManagementSystemFormat = (byte[] input) -> {
		if (input.length != 2) {
			return "invalid length!";
		}
		int cgmsa = (input[0] & 0b0001_1000) >>>3;
		String res = cgms_lookup.get(cgmsa, "unknown value");
		if(cgmsa == 3) {
			int aps =  (input[0] & 0b0000_0110) >>>1;
			res += ", "+ aps_lookup.get(aps,"unknown apsvalue");
			int asb =  (input[0] & 0b0000_0001);
			res += ", Analog Source Bit: "+asb;
		}
		return res;
	};

	Function<byte[], String> getFormatter(int class1, int type){
		if(class1 == 0){
			if(type == 1) {
				return cea608ProgramIdentificationNumberFormat;
			}else if(type == 2) {
				return cea608LengthTimeInShowFormat;
			}else if(type == 4) {
				return cea608ProgramTypeFormat;
			}else if(type == 5) {
				return cea608ContentAdvisoryFormat;
			}else if(type == 8) {
				return cea608CopyGenerationManagementSystemFormat;
			}
		}
		return cea608StringFormat;
	}

	
	protected void addCCDataToTree(final int modus, final DefaultMutableTreeNode s) {
		if(!allCcData.isEmpty()) {
			addRawCCDataToTree(modus, s);
		}
		if(!xdsData.isEmpty()) {
			addXDSToTree(s);
		}
	}


	/**
	 * @param s
	 */
	void addXDSToTree(final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode xdsTree = new DefaultMutableTreeNode(new KVP("XDS"));
		s.add(xdsTree);
		for(Entry<Integer, Map<Integer, List<byte[]>>> xdsEntry: xdsData.entrySet()) {
			final Integer classValue = xdsEntry.getKey();
			DefaultMutableTreeNode classNode = new DefaultMutableTreeNode(new KVP("class",classValue,Construct.xds_class.get(classValue, "unknown")));
			xdsTree.add(classNode);
			for(Entry<Integer, List<byte[]>> typeList :xdsEntry.getValue().entrySet()) {
				
				final Integer typeValue = typeList.getKey();
				DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(new KVP("type",typeValue,Construct.getTypeDescription(classValue,typeValue)));
				classNode.add(typeNode);
				for(byte[] value :typeList.getValue()) {
					DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(new KVP("value",value,getFormatter(classValue, typeValue) .apply(value)));
					typeNode.add(valueNode);
				}
			}
		}
	}


	/**
	 * @param modus
	 * @param s
	 */
	void addRawCCDataToTree(final int modus, final DefaultMutableTreeNode s) {
		final DefaultMutableTreeNode ccDataTree = new DefaultMutableTreeNode(new KVP("cc_data"));
		s.add(ccDataTree);
		for( Entry<Integer, Map<Long, List<Construct>>> typeEntry:allCcData.entrySet()) {
			final Map<Long, List<Construct>> typeEntryValue = typeEntry.getValue();
			final KVP typeNodeKvp = new KVP("type",typeEntry.getKey(),Construct.cc_type_list.get(typeEntry.getKey()));
			typeNodeKvp.setHtmlSource(() -> typeEntryValue.values().
					stream().
					flatMap(Collection::stream).
					map(HTMLSource::getHTML).
					collect(Collectors.joining("<br>"))
					);
			DefaultMutableTreeNode typeNode = new DefaultMutableTreeNode(typeNodeKvp);
			ccDataTree.add(typeNode);
			
			for(Entry<Long, List<Construct>> ptsEntry: typeEntryValue.entrySet()) {
				final List<Construct> constructList = ptsEntry.getValue();
				final KVP kvp = new KVP("pts",ptsEntry.getKey(),printTimebase90kHz(ptsEntry.getKey()));
				kvp.setHtmlSource(() -> constructList.stream().
								map(HTMLSource::getHTML).
								collect(Collectors.joining("<br>"))
								);
				
				
				DefaultMutableTreeNode ptsNode = new DefaultMutableTreeNode(kvp);
				typeNode.add(ptsNode);
				for(Construct construct:constructList) {
					ptsNode.add(construct.getJTreeNode(modus));
				}
			}
		}
	}

	
	void handleXDSData() {
		Map<Long, List<Construct>> field2Data = allCcData.get(1);

		List<Byte> payLoad = null;
		int currentClass = -1;
		int currentType = -1;
		if(field2Data==null || field2Data.isEmpty()) {
			return;
		}
		String state = "initial";
		for (List<Construct> fieldList : field2Data.values()) {
			for (Construct field : fieldList) {
				final int cc1WithoutParity = field.getCC1WithoutParity();
				final int cc2WithoutParity = field.getCC2WithoutParity();
				if (field.isXDSControl()) {
					if (cc1WithoutParity == 0xf) { // End ALL
						if (currentClass >= 0 && currentType >= 0) {
							Map<Integer, List<byte[]>> classXDS = xdsData.computeIfAbsent(currentClass,
									k -> new TreeMap<>());
							List<byte[]> typeXDSList = classXDS.computeIfAbsent(currentType, k -> new ArrayList<>());
							byte[] pay = Utils.bytesListToArray(payLoad);
							if (typeXDSList.isEmpty()) {
								typeXDSList.add(pay);
							} else {
								if (!Utils.listContainsByteArray(typeXDSList, pay)) {
									typeXDSList.add(pay);
								}
							}
							currentClass = -1;
							currentType = -1;
							payLoad = null;

						} // currentClass>=0 && currentType >=0

					} else { // XDS Control, not end all, so assume start // TODO
						int xdsClassRaw = cc1WithoutParity - 1;
						
						int startContinu = xdsClassRaw % 2; // 0 == start, 1 == continue
						
						
						if(startContinu==0) {
							currentClass = xdsClassRaw / 2;
							payLoad = new ArrayList<>();
							currentType = cc2WithoutParity;
							state = "started";
						}else if("interrupted".equals(state))  {
							state = "started";
						}
					}
				} else { // not an XDS control code // TODO check if not subtitle
					if(!field.isControl()) {
						if ("started".equals(state) && currentClass >= 0 && currentType >= 0 && payLoad !=null) { // something started
							if(cc1WithoutParity!=0) {
								payLoad.add((byte)cc1WithoutParity);
							}
							if(cc2WithoutParity!=0) {
								payLoad.add((byte)cc2WithoutParity);
							}
						}
					}else {
						state = "interrupted";
					}
				}
			}
		}
	}

	
	public void find708AuxData(long pts, AuxiliaryData auxData) {
		if(auxData != null && auxData.isDVB1data() && auxData.getUser_data_type_code() ==3) { // GA94 cc_data
			CCData ccData = auxData.getCcData();
			if(ccData.getProcess_cc_data_flag()==1) {
				List<Construct> constructs = ccData.getConstructs();
				for(Construct c:constructs) {
					if(c.getCc_valid() == 1) {
						if(c.getCc_data_1() !=128 || c.getCc_data_2() !=128) {
							final Map<Long, List<Construct>> typeMap = allCcData.computeIfAbsent(c.getCc_type(), k -> new TreeMap<Long,List<Construct>>());
							List<Construct> constructList = typeMap.computeIfAbsent(pts, k -> new ArrayList<>());
							constructList.add(c);
						}
					}
				}
			}
		}
	}

}
