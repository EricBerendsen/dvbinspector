/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.DVBSubtitlingPESDataField;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.Video138182Handler;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class GeneralPesHandler extends GeneralPidHandler{

	private static final Logger logger = Logger.getLogger(GeneralPesHandler.class.getName());

	private PesPacketData pesData= null;

	protected final List<PesPacketData>	pesPackets	= new ArrayList<>();
	protected static final int DEFAULT_BUF_LEN = 10;
	protected byte[] pesDataBuffer = new byte[DEFAULT_BUF_LEN];
	protected int bufStart = 0;
	protected int bufEnd = 0;


	/**
	 * Each specialist PesHandler has its own implementation of this method, responsible for decoding pictures, teletext or whatever.
	 * This generic implementation just adds the pesData packet to the pesPackets List
	 * @param pesData
	 */
	protected void processPesDataBytes(final PesPacketData pesData){
		pesPackets.add(pesData);
	}


	@Override
	public void processTSPacket(final TSPacket packet)
	{
		initialized = true;
		final byte []data = packet.getData();
		int pesLength = -1;
		int pesStreamID = -1;
		if((pesData==null)){ // nothing started
			// sometimes PayloadUnitStartIndicator is 1, and there is no payload, so check AdaptationFieldControl
			if(packet.isPayloadUnitStartIndicator() &&
					((packet.getAdaptationFieldControl()==1)||(packet.getAdaptationFieldControl()==3))){ //start something
				// at least one byte plus pointer available
				if((data[0]!=0)||(data[1]!=0)){ //starting PSI section after ofset
					// type = PSI;
					throw new IllegalStateException("Found PSI data in PESHandler, PID type changed over time (not illegal, however not supported...)");

					//	 could be starting PES stream, make sure it really is, Should start with packet_start_code_prefix -'0000 0000 0000 0000 0000 0001' (0x000001)
				}else if((data[0]==0)&&(data[1]==0)&&(data[2]==1)){
					//type = PES;
					pesStreamID = getInt(data, 3, 1, MASK_8BITS);
					pesLength =getInt(data,4,2, 0xFFFF);
					//for PES there can be only one pesPacket per TSpacket, and it always starts on first byte of payload.
					pesData = new PesPacketData(pesStreamID, pesLength,this,packet.getPacketNo());
					pesData.readBytes(data, 0, data.length);
					handlePesPacketIfComplete();

				}
			}
			//	something started
		}else if((packet.getAdaptationFieldControl()==1)||(packet.getAdaptationFieldControl()==3)){ // has payload?
			if(packet.isPayloadUnitStartIndicator()){ // previous pesPacket Finished, tell it to process its data
				if(pesData!=null){
					processPesDataBytes(pesData);
				}
				//start a new pesPacket
				// sometime a TSPacket has payload_unit_start_indicator set, and adaptation_field has 184 bytes, leaving nothing left for payload.
				// i think it is illegal (2.4.3.3 Semantic definition of fields in Transport Stream packet layer), but we should handle it.
				// TODO handle start of a PES packet when there are 1-5 bytes in the TS packet.
				// This is legal, but we can not handle it
				if(data.length>=6){
					pesStreamID = getInt(data, 3, 1, MASK_8BITS);
					pesLength =getInt(data,4,2, 0xFFFF);
					// for PES there can be only one pesPacket per TSpacket, and it always starts on first byte of payload.
					pesData = new PesPacketData(pesStreamID, pesLength,this,packet.getPacketNo());
					pesData.readBytes(data, 0, data.length);
					handlePesPacketIfComplete();
				}else{
					logger.warning("Not supported: Found PES packet start with data.length<6 at TSPacket: "+packet.getPacketNo());
				}
			}else if (pesData!=null){
				// already in a packet,needs more data
				pesData.readBytes(data, 0, data.length);
				handlePesPacketIfComplete();

			}
		}
	}

	private void handlePesPacketIfComplete() {
		if(pesData.isComplete()){
			pesData.processPayload();
			processPesDataBytes(pesData);
			pesData = null;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("PES Data"));
		addListJTree(s,pesPackets,modus,"PES Packets");

		return s;
	}

	public List<PesPacketData> getPesPackets() {
		return pesPackets;
	}

	public BufferedImage getBGImage(int height, int width, long pts) {

		BufferedImage bgImage = null;
		// try to get video BG
		// first get PMT to which this PES belongs
		// this ASS U MEs this PES is used by only one service.
		// Also ignores Page ID.
		PMTsection pmt = getTransportStream().getPMTforPID(getPID().getPid());
		// then get PID with ITU-T Rec. H.262 | ISO/IEC 13818-2 Video or ISO/IEC 11172-2 constrained parameter video stream (0x02)
		if(pmt!=null){
			int videoPID = Utils.findMPEG2VideoPid(pmt);
			if(videoPID>0){
				// see if it has a PESHandler (i.e. not scrambled) and if it is already parsed
				PID pid = getTransportStream().getPids()[videoPID];
				if(pid!=null){ // in partial stream the video PID may be missing
					GeneralPidHandler pesHandler = pid.getPidHandler();
					if((pesHandler!=null)&&
						pesHandler.isInitialized() &&
						(pesHandler instanceof Video138182Handler)){
						Video138182Handler videoHandler = (Video138182Handler)pesHandler;
						bgImage=videoHandler.getImage(height, width, pts);

					}
				}
			}
		}

		if(bgImage==null){ // no life image, use default
			bgImage = DVBSubtitlingPESDataField.bgImage576;
			// display_definition_segment for other size
			if(height==1080){
				bgImage = DVBSubtitlingPESDataField.bgImage1080;
			}else if(height==720){
				bgImage = DVBSubtitlingPESDataField.bgImage720;
			}
		}
		return bgImage;
	}

	/**
	 * @param pesDataField
	 */
	protected void copyIntoBuf(final PesPacketData pesDataField) {
		final int len = pesDataField.getPesDataLen();
		// clean if needed, remove used bytes from start en append new space at end
		if ((len + bufEnd) > pesDataBuffer.length) {
			final byte[] newBuf = new byte[Math.max((len + bufEnd) - bufStart,DEFAULT_BUF_LEN)];
			System.arraycopy(pesDataBuffer, bufStart, newBuf, 0, bufEnd - bufStart);
			pesDataBuffer = newBuf;
			bufEnd = bufEnd - bufStart;
			bufStart = 0;
		}

		// now copy new data into buf
		System.arraycopy(pesDataField.getData(),
				pesDataField.getPesDataStart(),
				pesDataBuffer,
				bufEnd,
				pesDataField.getPesDataLen());
		bufEnd+= pesDataField.getPesDataLen();
	}


}
