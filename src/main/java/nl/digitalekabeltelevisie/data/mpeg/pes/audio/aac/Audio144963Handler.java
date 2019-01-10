/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

/**
 * @author Eric Berendsen
 *
 */
public class Audio144963Handler extends GeneralPesHandler{ 
	final static int PIX_PER_SEC = 500;
	final static int LEFT_MARGIN = 60;
	final static int LEGEND_HEIGHT = 20;
	final static int GRAPH_HEIGHT = 400;

	private KVP kvp = null;
	private StreamMuxConfig streamMuxConfig;

	/**
	 * @param ancillaryDataidentifier
	 */
	public Audio144963Handler() {
		super();
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler#processPesDataBytes(int, byte[], int, int)
	 */
	@Override
	protected void processPesDataBytes(final PesPacketData pesData) {
		final Audio144963PESDataField audioPes = new Audio144963PESDataField(pesData, this);
		pesPackets.add(audioPes);


	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		kvp = new KVP("PES Data");
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(kvp);

		addListJTree(s,pesPackets,modus,"PES Packets");
		return s;
	}

	public void setStreamMuxConfig(StreamMuxConfig streamMuxConfig) {
		this.streamMuxConfig= streamMuxConfig;
		
	}

	public StreamMuxConfig getStreamMuxConfig() {
		return streamMuxConfig;
	}


}
