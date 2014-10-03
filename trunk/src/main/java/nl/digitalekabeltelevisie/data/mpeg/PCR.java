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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class PCR implements TreeNode{

	private long program_clock_reference_base;
	private long reserved;
	private long program_clock_reference_extension;
	public PCR(final long pcr, final long reserved2, final long pcr_extension) {
		program_clock_reference_base = pcr;
		reserved = reserved2;
		program_clock_reference_extension = pcr_extension;
	}

	public long getProgram_clock_reference_base() {
		return program_clock_reference_base;
	}
	public void setProgram_clock_reference_base(final long program_clock_reference_base) {
		this.program_clock_reference_base = program_clock_reference_base;
	}
	public long getProgram_clock_reference_extension() {
		return program_clock_reference_extension;
	}
	public void setProgram_clock_reference_extension(
			final long program_clock_reference_extension) {
		this.program_clock_reference_extension = program_clock_reference_extension;
	}
	public long getReserved() {
		return reserved;
	}
	public void setReserved(final long reserved) {
		this.reserved = reserved;
	}

	@Override
	public String toString(){
		return "base:"+program_clock_reference_base+", extension:"+program_clock_reference_extension+", Time:"+printPCRTime(getProgram_clock_reference());
	}

	public long getProgram_clock_reference(){
		return (program_clock_reference_base * 300) + program_clock_reference_extension;
	}



	public DefaultMutableTreeNode getJTreeNode(int modus, String label) {
		DefaultMutableTreeNode t= new DefaultMutableTreeNode(new KVP(label,getProgram_clock_reference(),printPCRTime(getProgram_clock_reference())));

		t.add(new DefaultMutableTreeNode(new KVP("program_clock_reference_base",program_clock_reference_base,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		t.add(new DefaultMutableTreeNode(new KVP("program_clock_reference_extension",program_clock_reference_extension,null)));
		return t;
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		return  getJTreeNode(modus, "PCR") ;
	}

}
