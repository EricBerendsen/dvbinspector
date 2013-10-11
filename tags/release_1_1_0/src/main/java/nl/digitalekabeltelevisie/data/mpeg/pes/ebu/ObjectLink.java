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

public class ObjectLink implements TreeNode{



	/**
	 * 
	 */
	private final byte[] data_block;

	/**
	 * 
	 */
	protected int offset;

	public ObjectLink(final byte[] data,final int offset) {
		data_block = data;
		this.offset = offset;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus, final String name) {
		DefaultMutableTreeNode s;
		if((getPageTens()==0xff)&&(getPageUnits()==0xf)){
			s=new DefaultMutableTreeNode(new KVP("Object Link not used"));
		}else{
			s=new DefaultMutableTreeNode(new KVP(name+" "+TxtDataField.formatPageNo(getMagazine(), (getPageTens()*16)+getPageUnits(), 0x3f7f)));
			s.add(new DefaultMutableTreeNode(new KVP("Magazine ",getMagazine(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("pageTens ",getPageTens(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("pageUnits ",getPageUnits(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("numberOfSubpages",getNumberOfSubpages(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("fallbackProtocol",getFallbackProtocol(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("defaultSidePanel",getDefaultSidePanel(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("defaultBackgroundColour",getDefaultBackgroundColour(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("defaultObject1Type",getDefaultObject1Type(),EBUTeletextHandler.getObjectTypeString(getDefaultObject1Type()))));
			s.add(new DefaultMutableTreeNode(new KVP("defaultObject2Type",getDefaultObject2Type(),EBUTeletextHandler.getObjectTypeString(getDefaultObject2Type()))));
			s.add(new DefaultMutableTreeNode(new KVP("defaultObject1",getDefaultObject1(),"subPage S1:"+getDefaultObject1SubPageS1()+", Pointer location:"+getDefaultObject1PointerLocation())));
			s.add(new DefaultMutableTreeNode(new KVP("defaultObject2",getDefaultObject2(),"subPage S1:"+getDefaultObject2SubPageS1()+", Pointer location:"+getDefaultObject2PointerLocation())));
		}
		return s;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		return getJTreeNode(modus, "Object Link");
	}


	/**
	 * @return the data_block
	 */
	public byte[] getData_block() {
		return data_block;
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

	public int getFallbackProtocol(){
		return (getHammingReverseByte(data_block[offset+4])&0x1);
	}

	public int getDefaultSidePanel(){
		return (getHammingReverseByte(data_block[offset+4])&0x6)>>1;
	}

	public int getDefaultBackgroundColour(){
		return (getHammingReverseByte(data_block[offset+4])&0x8)>>3;
	}

	public int getDefaultObject1Type(){
		return (getHammingReverseByte(data_block[offset+5])&0x3);
	}

	public int getDefaultObject2Type(){
		return (getHammingReverseByte(data_block[offset+5])&0xC)>>2;
	}

	public int getDefaultObject1(){
		return getHammingReverseByte(data_block[offset+6])+(16*getHammingReverseByte(data_block[offset+7]));
	}

	public int getDefaultObject1SubPageS1(){
		return getHammingReverseByte(data_block[offset+6]);
	}

	public int getDefaultObject1TripletNoOffset(){
		return (getHammingReverseByte(data_block[offset+7])&0x6)>>1;
	}

	public int getDefaultObject1PointerLocation(){
		return (getHammingReverseByte(data_block[offset+7])&0x8)>>3;
	}

	public int getDefaultObject1PointerPosition(){
		return (getHammingReverseByte(data_block[offset+7])&0x1);
	}

	public int getDefaultObject2(){
		return getHammingReverseByte(data_block[offset+8])+(16*getHammingReverseByte(data_block[offset+9]));
	}


	public int getDefaultObject2SubPageS1(){
		return getHammingReverseByte(data_block[offset+8]);
	}

	public int getDefaultObject2TripletNoOffset(){
		return (getHammingReverseByte(data_block[offset+9])&0x6)>>1;
	}

	public int getDefaultObject2PointerLocation(){
		return (getHammingReverseByte(data_block[offset+9])&0x8)>>3;
	}

	public int getDefaultObject2PointerPosition(){
		return (getHammingReverseByte(data_block[offset+9])&0x1);
	}

	public int getPageNo(){
		return (getPageTens()*16)+getPageUnits();
	}

	@Override
	public String toString(){
		return "mag:"+getMagazine()+", page:"+getPageNo()+", defaultObject1:"+getDefaultObject1()+", getDefaultObject1Type:"+getDefaultObject1Type()+", defaultObject2"+getDefaultObject2()+", getDefaultObject2Type:"+getDefaultObject2Type();
	}

}

