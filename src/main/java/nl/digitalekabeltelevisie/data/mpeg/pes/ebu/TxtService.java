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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

public class TxtService implements TreeNode{
	
	private TransportStream transportStream;

	TxtService(TransportStream transportStream) {
        this.transportStream = transportStream;
	}

	private final Magazine[] magazineList = new Magazine[8];
	@SuppressWarnings("unchecked")
	private final List<TxtDataField>[] packet8_30 = new ArrayList[16]; // lines , designation code should be
	@SuppressWarnings("unchecked")
	private final List<TxtDataField>[] packet8_31 =new ArrayList[16]; // lines

	void addTxtDataField(TxtDataField txtDataField){
		int mag = txtDataField.getMagazineNo();
		if(txtDataField.getPacketNo()<30){
			if(magazineList[mag]==null){
				magazineList[mag]= new Magazine(this, mag);
			}
			magazineList[mag].addTxtDataField(txtDataField);
		}else{ //30/31 Any packets with Y = 30 or Y = 31 are not page- or magazine-related 7.4 Elements of the service
			if(txtDataField.getPacketNo()==30){
				if(packet8_30[txtDataField.getDesignationCode()]==null){
					packet8_30[txtDataField.getDesignationCode()] = new ArrayList<>();
				}
				EBUTeletextHandler.add(txtDataField, packet8_30[txtDataField.getDesignationCode()]);
			}else if(txtDataField.getPacketNo()==31){
				if(packet8_31[txtDataField.getDesignationCode()]==null){
					packet8_31[txtDataField.getDesignationCode()] = new ArrayList<>();
				}
				EBUTeletextHandler.add(txtDataField, packet8_31[txtDataField.getDesignationCode()]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public KVP getJTreeNode(int modus) {
		KVP s = new KVP("TxtService");

		for (int i = 0; i < packet8_30.length; i++) {
			List<TxtDataField> txtDatafield = packet8_30[i];

			if(txtDatafield!=null){
				addListJTree(s,txtDatafield,modus,"Line Y=30, designation:"+i);
			}
		}
		for (int i = 0; i < packet8_31.length; i++) {
			List<TxtDataField> txtDatafield = packet8_31[i];

			if(txtDatafield!=null){
				addListJTree(s,txtDatafield,modus,"Line Y=31, designation:"+i);
			}
		}
		for (int i = 1; i < 9; i++) {
			Magazine m = magazineList[i%8];
			if(m!=null){
				s.add(m.getJTreeNode(modus));
			}
		}
		return s;
	}

	/**
	 * @param m
	 * @return
	 */
	public Magazine getMagazine(int m) {

		return magazineList[m];
	}

	public TransportStream getTransportStream() {
		return transportStream;
	}

	public List<TxtDataField>[] getPacket8_30() {
		return packet8_30;
	}

	public List<TxtDataField>[] getPacket8_31() {
		return packet8_31;
	}
}