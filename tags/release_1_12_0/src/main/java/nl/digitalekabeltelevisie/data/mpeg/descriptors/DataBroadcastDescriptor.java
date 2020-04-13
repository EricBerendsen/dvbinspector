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

import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class DataBroadcastDescriptor extends Descriptor {

	//
	//	The important specs are:
	//
	//	    * ISO/IEC 13818-6 (MPEG Extensions for DSM-CC)
	//	    * ETSI EN 301 192 (DVB Specification for Data Broadcasting)
	//	    * ETSI TR 101 202 (DVB Implementation Guidelines for Data Broadcasting)
	//
	//	The most useful of these is probably TR 101 202.
	//	While the title claims that it's only a set of implementation guidelines, it's actually the closest thing to a clear description of DSM-CC that you'll find anywhere. At 60 pages long, it's well worth the download.
	//
	//	Do not try to read the DSM-CC specification first -
	//  you'll only confuse yourself and make it harder for you to understand what the hell is going on.
	//  Reading TR101 202 while referring to EN 301 192 is the approach that seems to work best.
	//  At first, only read the DSM-CC specification to check details that are mentioned in either
	//  of the two other documents. It's far better to learn what you need to know first, and then extend that knowledge into other areas, than to try and learn it all at once.

	private final int dataBroadcastId;
	private final int componentTag;
	private final int selectorLength;
	private final byte[] selectorByte;
	private final String iso639LanguageCode;

	private int MAC_address_range;
	private int MAC_IP_mapping_flag;
	private int alignment_indicator;
	private int max_sections_per_datagram ;

	private final int textLength;
	private final byte[] text;

	public DataBroadcastDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		dataBroadcastId = Utils.getInt(b, offset+2, 2, Utils.MASK_16BITS);
		componentTag = Utils.getInt(b, offset+4, 1, Utils.MASK_8BITS);
		selectorLength= Utils.getInt(b, offset+5, 1, Utils.MASK_8BITS);
		selectorByte = Utils.copyOfRange(b, offset+6, offset+6+selectorLength);
		if(dataBroadcastId==0x0005){ //en 301192 7.2.1
			MAC_address_range = Utils.getInt(b, offset+6, 1, 0xE0)>>>5;
			MAC_IP_mapping_flag = Utils.getInt(b, offset+6, 1, 0x10)>>>4;
			alignment_indicator = Utils.getInt(b, offset+6, 1, 0x08)>>>3;
			max_sections_per_datagram =Utils.getInt(b, offset+7, 1, Utils.MASK_8BITS);
		}
		iso639LanguageCode=getISO8859_1String(b, offset+6+selectorLength, 3);
		textLength= Utils.getInt(b, offset+9+selectorLength, 1, Utils.MASK_8BITS);
		text = Utils.copyOfRange(b, offset+10+selectorLength, offset+10+selectorLength+textLength);
	}




	@Override
	public String toString() {

		return super.toString() + "dataBroadcastId="+dataBroadcastId;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		t.add(new DefaultMutableTreeNode(new KVP("data_broadcast_id",dataBroadcastId ,Utils.getDataBroadCastIDString(dataBroadcastId))));
		t.add(new DefaultMutableTreeNode(new KVP("component_tag",componentTag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("selector_length",selectorLength,null)));
		if(dataBroadcastId==0x0005){ //en 301192 7.2.1
			t.add(new DefaultMutableTreeNode(new KVP("MAC_address_range",MAC_address_range,getvalidMACaddressBytesString(MAC_address_range))));
			t.add(new DefaultMutableTreeNode(new KVP("MAC_IP_mapping_flag",MAC_IP_mapping_flag,MAC_IP_mapping_flag==1?"uses IP to MAC mapping as described in RFC 1112 and RFC 2464":"mapping not defined")));
			t.add(new DefaultMutableTreeNode(new KVP("alignment_indicator",alignment_indicator,alignment_indicator==1?"alignment in bits: 32":"alignment in bits: 8 (default)")));
			t.add(new DefaultMutableTreeNode(new KVP("max_sections_per_datagram",max_sections_per_datagram,null)));
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("selector_bytes",selectorByte,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
		t.add(new DefaultMutableTreeNode(new KVP("text_length",textLength,null)));
		t.add(new DefaultMutableTreeNode(new KVP("text_char",text,null)));

		return t;
	}

	public static String getvalidMACaddressBytesString(final int adresReange) {
		switch (adresReange) {
		case 0x00 : return "reserved";
		case 0x01 : return "valid MAC_address bytes: 6";
		case 0x02 : return "valid MAC_address bytes: 6,5";
		case 0x03 : return "valid MAC_address bytes: 6,5,4";
		case 0x04 : return "valid MAC_address bytes: 6,5,4,3";
		case 0x05 : return "valid MAC_address bytes: 6,5,4,3,2";
		case 0x06 : return "valid MAC_address bytes: 6,5,4,3,2,1";
		default:
			return "reserved";
		}
	}

}
