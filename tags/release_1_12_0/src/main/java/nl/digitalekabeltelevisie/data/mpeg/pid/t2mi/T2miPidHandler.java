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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.gui.DVBtree;

public class T2miPidHandler extends GeneralPidHandler {
	
	private static final Logger LOGGER = Logger.getLogger(T2miPidHandler.class.getName());

	private List<T2miPacket> t2miPackets = new ArrayList<>();
	private byte[] currentPacketData;
	private byte[] header = new byte[6];
	private int dataBytesRead = 0;
	private int headerBytesRead = 0;
	private boolean packetStarted = false;
	private Map<Integer, List<T2miPacket>> plps = new HashMap<>();

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("T2MI"));
		addListJTree(t,t2miPackets,modus,"T2MI Packets");
		DefaultMutableTreeNode plpsTree = new DefaultMutableTreeNode(new KVP("PLPs"));
		final Iterator<Integer> plpIter = new TreeSet<Integer>(plps.keySet()).iterator();
		while(plpIter.hasNext()){
			final Integer plpId=plpIter.next();
			List<T2miPacket> itemList = plps.get(plpId);
			if((itemList!=null)&&(itemList.size()!=0)){
					KVP kvp = new KVP("plp:"+plpId +": "+ itemList.size()+" entries");
					final DefaultMutableTreeNode plpListNode = new DefaultMutableTreeNode(kvp);
					final JMenuItem objectMenu = new JMenuItem("Save embedded TS as...");
					objectMenu.setActionCommand(DVBtree.T2MI);
					kvp.setSubMenuAndOwner(objectMenu,new PlpHandler(pid.getPid(), plpId, itemList));

					addToList(plpListNode, itemList, modus);
					plpsTree.add(plpListNode);
			}
		}
		t.add(plpsTree);
		return t;
	}

	@Override
	public void processTSPacket(TSPacket packet) {
		initialized = true;
		byte[] data = packet.getData();
		int packetNo = packet.getPacketNo();
		if(packet.isPayloadUnitStartIndicator()) { //first byte is offset
			if(!packetStarted) {
				packetStarted = true;
				
				readBytes(data,Byte.toUnsignedInt(data[0])+1, packetNo);
			}else { 
				readBytes(data,1, packetNo); // ignore the offset, already something started
			}
		}else { // not payloadStart
			if(packetStarted) {
				readBytes(data,0, packetNo);
				
			}
		}

	}

	private void readBytes(byte[] data, int offsetIn, int packetNo) {
		int offset = offsetIn;
		int bytesLeft = data.length - offset;
		while (bytesLeft > 0) {
			if (headerBytesRead < 6) {
				int toRead = Math.min(6 - headerBytesRead, bytesLeft);
				System.arraycopy(data, offset, header, headerBytesRead, toRead);
				offset += toRead;
				headerBytesRead += toRead;
				bytesLeft -= toRead;
			}
			if (bytesLeft == 0) {
				return;
			}
			int payloadLenInBits = 256 * Byte.toUnsignedInt(header[4]) + Byte.toUnsignedInt(header[5]);
			int payloadLenInBytes = (payloadLenInBits + 7) / 8 + 4; // CRC = 4 bytes
			if (currentPacketData == null) {
				currentPacketData = new byte[payloadLenInBytes];
			}
			if (dataBytesRead < payloadLenInBytes) {
				int toRead = Math.min(payloadLenInBytes - dataBytesRead, bytesLeft);
				System.arraycopy(data, offset, currentPacketData, dataBytesRead, toRead);
				offset += toRead;
				dataBytesRead += toRead;
				bytesLeft -= toRead;
			}
			if (dataBytesRead == payloadLenInBytes) {
				byte[] result = new byte[6+payloadLenInBytes];
				System.arraycopy(header,0,result,0,6);
				System.arraycopy(currentPacketData,0,result,6,payloadLenInBytes);
				T2miPacket packet = new T2miPacket(result,packetNo);
				headerBytesRead = 0;
				dataBytesRead = 0;
				currentPacketData = null;

				if(packet.getCRCresult()!=0) {
					LOGGER.warning("CRC check failed, T2MI packet started at TS Packet:"+packetNo);
					packetStarted = false;
					return;
				}
				handleT2miPacket(packet);
				if (bytesLeft == 0) {
					packetStarted = false;
					return;
				}
			}

		}
		
	}

	/**
	 * @param packet
	 */
	private void handleT2miPacket(T2miPacket packet) {
		t2miPackets.add(packet);
		if(packet.getPacketType()==0) { // Baseband frame
			int plpId = ((BasebandFramePayload)packet.getPayload()).getPlpId();
			List<T2miPacket> plpList = plps.computeIfAbsent(plpId,k ->new ArrayList<>());
			plpList.add(packet);
		}
	}

}
