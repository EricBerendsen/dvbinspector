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

package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.getHammingReverseByte;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class DRCSLink implements TreeNode{


	/**
	 * 
	 */
	private final byte[] data_block;

	/**
	 * 
	 */
	protected int offset;


	public DRCSLink(final byte[] data,final int offset) {
		data_block = data;
		this.offset = offset;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus, final String name) {
		DefaultMutableTreeNode s;
		if((getPageTens()==0xff)&&(getPageUnits()==0xf)){
			s=new DefaultMutableTreeNode(new KVP("Object Link not used"));
		}else{
			s=new DefaultMutableTreeNode(new KVP(name+TxtDataField.formatPageNo(getMagazine(), getPageNo(), 0x3f7f)));
			s.add(new DefaultMutableTreeNode(new KVP("Magazine ",getMagazine(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("pageTens ",getPageTens(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("pageUnits ",getPageUnits(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("numberOfSubpages",getNumberOfSubpages(),"highest S1 sub-code value transmitted "+(1+getNumberOfSubpages()))));
		}
		return s;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		return getJTreeNode(modus, "DRCS Link");
	}

	public int getMagazine(){
		return (getHammingReverseByte(data_block[offset])&0x7);
	}
	public int getPageTens(){
		return getHammingReverseByte(data_block[offset+1]);
	}

	public int getPageUnits(){
		return getHammingReverseByte(data_block[offset+2]);
	}
	public int getNumberOfSubpages(){
		return getHammingReverseByte(data_block[offset+3]);
	}

	public int getPageNo(){
		return (getPageTens()*16)+getPageUnits();
	}

}