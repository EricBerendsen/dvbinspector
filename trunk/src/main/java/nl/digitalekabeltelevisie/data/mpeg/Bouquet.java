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

package nl.digitalekabeltelevisie.data.mpeg;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class Bouquet implements TreeNode {

	private static final Logger logger = Logger.getLogger(Bouquet.class.getName());
	private Map<Integer, TransportStream> streams = new TreeMap<Integer, TransportStream>();

	public Bouquet(final File dir){
		super();
		if(dir.isDirectory()){
			final File[] streamFiles = dir.listFiles();
			for(final File streamFile : streamFiles){
				final TransportStream ts = new TransportStream(streamFile);
				try {
					ts.parseStream();
				} catch (final IOException e) {
					logger.throwing("Bouquet", "Bouquet()", e);
				}
				ts.setBouquet(this);
				streams.put(ts.getStreamID(), ts);
			}
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Bouquet"));
		for(final TransportStream ts: streams.values()){
			t.add(ts.getJTreeNode(modus));
		}

		return t;
	}

	public Map<Integer, TransportStream> getStreams() {
		return streams;
	}

	public void setStreams(final Map<Integer, TransportStream> streams) {
		this.streams = streams;
	}

}
