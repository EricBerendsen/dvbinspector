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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.H26xPESDataField;

/**
 * @author Eric Berendsen
 *
 */

public class Video265PESDataField extends H26xPESDataField<H265NALUnit> implements TreeNode {

	private static final Logger	logger	= Logger.getLogger(Video265PESDataField.class.getName());


	public Video265PESDataField(final PesPacketData pesPacket) {
		super(pesPacket);

	}



	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s = super.getJTreeNode(modus,new KVP("Video 14496 PES Packet"));
		addListJTree(s,nalUnits,modus,"NAL Units");
		return s;

	}



	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.data.mpeg.pes.video264.H26xPESDataField#createInstance(byte[], int, int)
	 */
	@Override
	protected H265NALUnit createNALUnitInstance(final byte[] data, final int i, final int j) {
		return new H265NALUnit(data, i, j);
	}





}
