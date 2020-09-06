package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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


import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.TreeNode;



// based on TS 101 154 V2.2.1 (2015-06) 6.6.7 AC-4 Sync Frame Format
// NOTE in newer versions this is void and refers to ETSI TS 103 190-2 [46], annex C. 
// However annex C of TS 103 190-2 V1.2.1 (2018-02) is Void, so use TS 103 190-2 V1.1.1 (2015-09)

public class AC4SyncFrame implements TreeNode {

	private int sync_word;
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		// TODO Auto-generated method stub
		return null;
	}

}
