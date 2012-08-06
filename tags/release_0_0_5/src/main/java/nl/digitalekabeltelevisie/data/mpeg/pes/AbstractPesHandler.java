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

package nl.digitalekabeltelevisie.data.mpeg.pes;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;

/**
 * @author Eric Berendsen
 *
 */
public abstract class AbstractPesHandler{

	private PesPacketData pesData= null;

	private int pesStreamID =-1;
	private int pesLength =-1;

	/**
	 * by default the contents of a PES PID is not read or analyzed. After explicit user command processTSPacket is called, and PID is initialized
	 */
	private boolean initialized = false;

	protected final List<PesPacketData>	pesPackets	= new ArrayList<PesPacketData>();

	//public abstract void processPesDataBytes(int streamId, byte[] data,int offset,int len, long pts);
	public abstract void processPesDataBytes(PesPacketData pesData);

	public void processTSPacket(final TSPacket packet)
	{
		initialized = true;
		final byte []data = packet.getData();
		if((pesData==null)){ // nothing started
			// sometimes PayloadUnitStartIndicator is 1, and there is no payload, so check AdaptationFieldControl
			if(packet.isPayloadUnitStartIndicator() &&
					((packet.isAdaptationFieldControl()==1)||(packet.isAdaptationFieldControl()==3))){ //start something
				// at least one byte plus pointer available
				if((data[0]!=0)||(data[1]!=0)){ //starting PSI section after ofset
					// type = PSI;
					throw new IllegalStateException("Found PSI data in PESHandler...");

					//	 could be starting PES stream, make sure it really is, Should start with packet_start_code_prefix -'0000 0000 0000 0000 0000 0001' (0x000001)
				}else if((data[0]==0)&&(data[1]==0)&&(data[2]==1)){
					//type = PES;
					pesStreamID = getInt(data, 3, 1, MASK_8BITS);
					pesLength=getInt(data,4,2, 0xFFFF);
					//for PES there can be only one pesPacket per TSpacket, and it always starts on first byte of payload.
					pesData = new PesPacketData(pesStreamID,pesLength);
					pesData.readBytes(data, 0, data.length);

				}
			}
			//	something started
		}else if((packet.isAdaptationFieldControl()==1)||(packet.isAdaptationFieldControl()==3)){ // has payload?
			if(packet.isPayloadUnitStartIndicator()){ // previous pesPAcket Finished, tell it to process its data
				pesData.processPayload();
				//processPesDataBytes(pesData.getPesStreamID(), pesData.getData(), pesData.getPesDataStart(), pesData.getPesDataLen(), pesData.getPts());
				processPesDataBytes(pesData);
				//start a new pesPacket
				pesStreamID = getInt(data, 3, 1, MASK_8BITS);
				pesLength=getInt(data,4,2, 0xFFFF);
				// for PES there can be only one pesPacket per TSpacket, and it always starts on first byte of payload.
				pesData = new PesPacketData(pesStreamID,pesLength);
				pesData.readBytes(data, 0, data.length);
			}else{
				// already in a packet,needs more data
				pesData.readBytes(data, 0, data.length);
			}
		}
	}

	/**
	 * @return
	 */
	public boolean isInitialized() {
		return initialized;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("PES Data"));
		addListJTree(s,pesPackets,modus,"PES Packets");

		return s;
	}

	public List<PesPacketData> getPesPackets() {
		return pesPackets;
	}

}
