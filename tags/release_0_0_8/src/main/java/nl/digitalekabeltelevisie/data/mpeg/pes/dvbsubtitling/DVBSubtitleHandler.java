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

package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

/**
 * @author Eric Berendsen
 *
 */
public class DVBSubtitleHandler extends GeneralPesHandler{
	
	private Titles titles = null; 


	@Override
	public void processPesDataBytes(final PesPacketData pesData) {
		final DVBSubtitlingPESDataField titlePesPacket = new DVBSubtitlingPESDataField(pesData);
		pesPackets.add(titlePesPacket);
		if((titlePesPacket.getPesStreamID()==0xBD)// "private_stream_1". 
				&&(titlePesPacket.getData_identifier()==0x20) // For DVB subtitle streams the data_identifier field shall be coded with the value 0x20.
				&&(titlePesPacket.getSubtitle_stream_id()==0)){ // A DVB subtitling stream shall be identified by the value 0x00.
			if(titles==null){
				titles = new Titles();
			}
			titles.add(titlePesPacket);
		}
		


	}

	
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=super.getJTreeNode(modus);

		if(titles!=null){
			s.add(titles.getJTreeNode(modus));
		}
		return s;
	}

}
