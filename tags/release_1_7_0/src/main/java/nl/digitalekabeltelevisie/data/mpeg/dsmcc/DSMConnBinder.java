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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.Utils;

public class DSMConnBinder extends LiteComponent {
	protected int taps_count;
	protected List<Tap> taps = new ArrayList<Tap>();

	public DSMConnBinder(final byte[] data, final int offset) {
		super(data, offset);
		taps_count= Utils.getInt(data, offset+5, 1, Utils.MASK_8BITS);
		// TODO does not work for different length Taps
		for (int i = 0; i < taps_count; i++) {
			final Tap tap =new Tap(data,offset+6+(i*17));
			taps.add(tap);

		}

	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(
				"DSM::ConnBinder"));
		t.add(new DefaultMutableTreeNode(new KVP("component_tag",component_tag ,getComponentTagString((int)component_tag))));
		t.add(new DefaultMutableTreeNode(new KVP("component_data_length",component_data_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("taps_count",taps_count ,null)));
		addListJTree(t,taps,modus,"BIOP::Taps");
		return t;
	}


	public int getTaps_count() {
		return taps_count;
	}


	public List<Tap> getTaps() {
		return taps;
	}


}
