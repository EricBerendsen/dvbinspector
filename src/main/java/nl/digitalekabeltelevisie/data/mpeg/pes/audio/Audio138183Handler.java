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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.rds.UECP;

/**
 * @author Eric Berendsen
 *
 */
public class Audio138183Handler extends GeneralPesHandler{

	private byte[] rdsData=new byte[0];
	private final List<AudioAccessUnit> audioAccessUnits = new ArrayList<AudioAccessUnit>();

	/**
	 * @param ancillaryDataidentifier
	 */
	public Audio138183Handler(final int ancillaryDataidentifier) {
		super();
		this.ancillaryDataIdentifier = ancillaryDataidentifier;
	}

	private final int ancillaryDataIdentifier; // used to identify RDS data

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	public void processPesDataBytes(final PesPacketData pesData) {
		// AudioAccessUnit are not always aligned with PES Packet
		final AudioPESDataField audioPes = new AudioPESDataField(pesData);
		pesPackets.add(audioPes);


		copyIntoBuf(audioPes);

		final List<AudioAccessUnit> accessUnits = new ArrayList<AudioAccessUnit>();
		int i = bufStart;

		while ((i < (bufEnd)) && (i >= 0)) {
			i = indexOfSyncWord(pesDataBuffer,  i);
			if (i >= 0) { // found start,
				if ((i+4) < bufEnd){ // at least 4 bytes, try to create an AudioAccessUnit
					AudioAccessUnit frame = new AudioAccessUnit(pesDataBuffer, i,audioPes.getPts());
					int unitLen = frame.getFrameSize();
					if(unitLen<0) { // not a valid frame. start search again from next pos
						i++;
					}else if((i+unitLen+2)<bufEnd){  // see if at where next frame should start we also have syncword, and
						int nextIndex = indexOfSyncWord(pesDataBuffer,  i+unitLen);
						if(nextIndex==(i+unitLen)){
							accessUnits.add(frame);
							i = nextIndex;
							bufStart = nextIndex;
						}else{// not enough read, continu next time
							bufStart = i;
						}
					}else{// not enough read, continu next time
						bufStart = i;
						break;

					}
				}
			}
		}

		audioAccessUnits.addAll(accessUnits);

		if((ancillaryDataIdentifier & 0x40)!=0) {// RDS via UECP
			for(final AudioAccessUnit accessUnit:accessUnits){
				final AncillaryData ancillaryData = accessUnit.getAncillaryData();
				if((ancillaryData!=null)&&(ancillaryData.getSync()==0xFD)&&(ancillaryData.getDataFieldLength()!=0)){
					// append all data to single byte[]
					final byte[] b =ancillaryData.getDataByte();
					final byte[] newRdsData = new byte[rdsData.length + b.length];
					System.arraycopy(rdsData, 0, newRdsData, 0, rdsData.length);
					System.arraycopy(b, 0, newRdsData, rdsData.length, b.length);
					rdsData = newRdsData;
				}
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s= super.getJTreeNode(modus);

		addListJTree(s, audioAccessUnits, modus, "Audio Access Units");
		if((ancillaryDataIdentifier & 0x40)!=0) {// RDS via UECP
			final DefaultMutableTreeNode rdsNode = new DefaultMutableTreeNode(new KVP("RDS"));
			s.add(rdsNode);
			rdsNode.add(new DefaultMutableTreeNode(new KVP("RDS Data",rdsData,null)));
			final UECP uecp = new UECP(rdsData);
			rdsNode.add(uecp.getJTreeNode(modus));
		}

		return s;
	}


	/**
	 * Look for syncword (0xfff, 12 bits)
	 *
	 * @param source
	 * @param fromIndex
	 * @return
	 */
	private static int indexOfSyncWord(final byte[] source, final int fromIndex){
		if (fromIndex >= source.length) {
			return  -1;
		}

		final int max = source.length -1;

		for (int i = fromIndex; i < max; i++) {
			/* Look for first byte. */
			if (source[i] != -1) {
				while ((++i <= max) && (source[i] != -1)){ //0xFF
					// EMPTY body
				}
			}

			/* Found first byte, now look at second byteof 0xFFF */
			if ((i < max) && ((source[i+1] &0xF0)==0xF0 )) {
					/* Found whole string. */
					return i ;
				}
			}
		return -1;
	}

}
