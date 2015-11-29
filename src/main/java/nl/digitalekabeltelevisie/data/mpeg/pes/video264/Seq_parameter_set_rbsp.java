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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;

public class Seq_parameter_set_rbsp extends RBSP {
	
	Seq_parameter_set_data seqParameterSetData;

	public Seq_parameter_set_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		seqParameterSetData = new Seq_parameter_set_data(bitSource);


	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("seq_parameter_set_rbsp"));
		seqParameterSetData.addToJTree(t,modus);
		return t;
	}


	public Seq_parameter_set_data getSeqParameterSetData() {
		return seqParameterSetData;
	}

	public void setSeqParameterSetData(Seq_parameter_set_data seqParameterSetData) {
		this.seqParameterSetData = seqParameterSetData;
	}


}
