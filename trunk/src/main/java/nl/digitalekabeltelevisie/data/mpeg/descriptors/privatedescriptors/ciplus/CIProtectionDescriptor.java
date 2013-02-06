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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ciplus;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CIProtectionDescriptor extends Descriptor {

	private final int freeCIModeFlag;
	private final int matchBrandFlag;
	private int numberOfEntries;


	private final List<BrandIdentifier> brandList = new ArrayList<BrandIdentifier>();
	private byte[] privateDataByte;

	public static class BrandIdentifier implements TreeNode
	{
		private final int brandId;

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			return new DefaultMutableTreeNode(new KVP("cicam_brand_identifier",brandId,null));
		}

		public BrandIdentifier(final int brandId) {
			super();
			this.brandId = brandId;
		}
	}

	public CIProtectionDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		freeCIModeFlag = getInt(b,offset+2,1,0x80)>>7;
		matchBrandFlag= getInt(b,offset+2,1,0x40)>>6;
		int t=0;
		if(matchBrandFlag==1){
			t=t+1;
			numberOfEntries = getInt(b, offset+3, 1, MASK_8BITS);
			for (int i = 0; i < numberOfEntries; i++) {
				final BrandIdentifier bi = new BrandIdentifier(getInt(b, offset+4+(2*i), 2, MASK_16BITS));
				brandList.add(bi);
				t=t+2;
			}
		}
		privateDataByte = copyOfRange(b, offset+3+t, offset+descriptorLength+2);
	}

	@Override
	public String getDescriptorname(){
		return "CI Protection Descriptor";
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("free_ci_mode_flag",freeCIModeFlag ,freeCIModeFlag==1?"streams do require CI Plus protection":"streams do not require CI Plus protection")));
		t.add(new DefaultMutableTreeNode(new KVP("match_brand_flag",matchBrandFlag ,matchBrandFlag==1?"service has chosen to set CICAM brands.":"service has no chosen CICAM brands")));
		if(matchBrandFlag==1){
			t.add(new DefaultMutableTreeNode(new KVP("number_of_entries", numberOfEntries ,null)));
			addListJTree(t,brandList,modus,"brand_identifier_list");
		}
		if((privateDataByte!=null)&&(privateDataByte.length>0)){
			t.add(new DefaultMutableTreeNode(new KVP("private_data_bytes",privateDataByte ,null)));
		}
		return t;
	}




	public byte[] getPrivateDataByte() {
		return privateDataByte;
	}

	public void setPrivateDataByte(final byte[] privateDataByte) {
		this.privateDataByte = privateDataByte;
	}

}
