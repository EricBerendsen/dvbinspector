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
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;


/**
 * @author Eric Berendsen
 *
 * PES_data_field() as defined in 4.3 Syntax for PES data field, ETSI EN 300 472 V1.3.1 (2003-05)
 * Digital Video Broadcasting (DVB); Specification for conveying ITU-R System B Teletext in DVB bitstreams
 *
 * and (extension)
 *
 * ETSI EN 301 775 V1.2.1  Digital Video Broadcasting (DVB);
 * Specification for the carriage of Vertical Blanking
 * Information (VBI) data in DVB bitstreams
 *
 */
public class EBUPESDataField extends PesPacketData implements TreeNode {

	private final int data_identifier;

	private final List<EBUDataField> fieldList = new ArrayList<EBUDataField>();


	/*
	  -- dvbstrPES_EBUDataUnitID  (Teletext, VPS, WSS, etc.)
	  -- EN 300 472  and EN 301 775.
	 */

	public static String getDataUnitIdString(final int dataUnitId){

		if((0x00<=dataUnitId)&&(dataUnitId<=0x01)){
			return "Reserved";
		}
		if((0x04<=dataUnitId)&&(dataUnitId<=0x7f)){
			return "Reserved";
		}
		if((0x80<=dataUnitId)&&(dataUnitId<=0xbe)){
			return "user defined";
		}
		if((0xc1<=dataUnitId)&&(dataUnitId<=0xc2)){
			return "Reserved";
		}
		if((0xc7<=dataUnitId)&&(dataUnitId<=0xfe)){
			return "Reserved";
		}
		switch(dataUnitId){

		case 0x02: return   "EBU Teletext non-subtitle data";
		case 0x03: return    "EBU Teletext subtitle data";
		case 0xC0: return    "inverted teletext";
		case 0xC3: return    "VPS (Video Programming System)";
		case 0xC4: return    "WSS (Wide Screen Signalling)";
		case 0xC5: return    "CC (Closed Caption)";
		case 0xC6: return    "monochrome 4:2:2 samples";
		case 0xFF: return    "data_unit for stuffing" ;
		default:
			return "illegal value";
		}
	}


	public EBUPESDataField(final PesPacketData pesData){
		super(pesData);

		final int offset = pesData.getPesDataStart();
		int dataUnitId =-1;
		int dataUnitLen =-1;
		data_identifier=getInt(data, offset, 1, MASK_8BITS);
		int t=1;
		while((t<pesData.getPesDataLen())&&(dataUnitId!=0)){  // should not happen ?
			dataUnitId = getInt(data, offset+t, 1, MASK_8BITS);
			dataUnitLen = getInt(data, offset+t+1, 1, MASK_8BITS);
			if((offset+t+2+0x2C)<=data.length){  // element always assumed to be 0x2c long.
				if((dataUnitId==0x02)||(dataUnitId==0x03)||(dataUnitId==0xc0)||(dataUnitId==0xc1)){
					final EBUDataField f = new TxtDataField(data,offset+t,dataUnitLen,getPesHeader().getPts());
					fieldList.add(f);
				}else if(dataUnitId==0xC4){
					final EBUDataField f = new WSSDataField(data,offset+t,dataUnitLen,getPesHeader().getPts());
					fieldList.add(f);
				}else if(dataUnitId==0xC3){
					final EBUDataField f = new VPSDataField(data,offset+t,dataUnitLen,getPesHeader().getPts());
					fieldList.add(f);
				}else{
					final EBUDataField f = new EBUDataField(data,offset+t,dataUnitLen,getPesHeader().getPts());
					fieldList.add(f);
				}
			}else{
				dataUnitId = 0; // stop constructor
			}
			t+=dataUnitLen+2;
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=super.getJTreeNode(modus, new KVP("EBU PES Packet"));
		s.add(new DefaultMutableTreeNode(new KVP("data_identifier",data_identifier,getDataIDString(data_identifier))));
		//s.add(new DefaultMutableTreeNode(new KVP("pts",getPesHeader().getPts(), printTimebase90kHz(getPesHeader().getPts()))));

		addListJTree(s,fieldList,modus,"fields");
		return s;
	}


	/**
	 * @return the fieldList
	 */
	public List<EBUDataField> getFieldList() {
		return fieldList;
	}

}
