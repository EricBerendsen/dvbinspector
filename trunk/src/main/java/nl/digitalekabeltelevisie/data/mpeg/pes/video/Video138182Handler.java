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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;



import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.AbstractPesHandler;

/**
 * @author Eric Berendsen
 *
 */
public class Video138182Handler  extends AbstractPesHandler implements TreeNode {

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.AbstractPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	public void processPesDataBytes(final PesPacketData pesData){
		//int streamId, byte[] data, int offset,int len, long pts) {
		//(pesData.getPesStreamID(), pesData.getData(), pesData.getPesDataStart(), pesData.getPesDataLen(), pesData.getPts());
		//videoFields.add(new VideoPESDataField(pesData.getData(),pesData.getPesDataStart(),pesData.getPesDataLen(),pesData.getPts()));
		pesPackets.add(new VideoPESDataField(pesData));

	}

}
