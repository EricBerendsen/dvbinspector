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



import java.awt.image.BufferedImage;

import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

/**
 * @author Eric Berendsen
 *
 */
public class Video138182Handler  extends GeneralPesHandler{


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	public void processPesDataBytes(final PesPacketData pesData){
		pesPackets.add(new VideoPESDataField(pesData));

	}

	/**
	 * find IFrame closest to the supplied pts, and return it's image in the requested size (height * width)
	 * @param height
	 * @param width
	 * @param pts
	 * @return
	 */
	public BufferedImage getImage(int height, int width, long pts) {
		VideoPESDataField resultPES = null;

		long diff = Long.MAX_VALUE;
		for (PesPacketData pesPacket : pesPackets) { // iterate over all video frames, in case pts wraps around
			VideoPESDataField video = (VideoPESDataField)pesPacket;
			if(video.isIFrame() && (Math.abs(video.getPts() - pts) <diff)){
				resultPES = video;
				diff = Math.abs(video.getPts() - pts);
			}
		}
		if(resultPES!=null){
			return resultPES.getImage(width,height);
		}else{
			return null;
		}
	}

}
