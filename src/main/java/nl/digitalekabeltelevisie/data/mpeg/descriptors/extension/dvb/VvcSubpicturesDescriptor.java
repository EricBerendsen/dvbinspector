/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Based on DVB BlueBook A038r16 – (April 2023) 6.4.17 VVC subpictures descriptor
 *
 */
public class VvcSubpicturesDescriptor extends DVBExtensionDescriptor {
	
	LookUpList processing_mode_list = new LookUpList.Builder().
			add(0b000,"processing mode undefined").
			add(0b001,"no bitstream processing necessary").
			add(0b010,"merging of VVC subpictures into one bitstream necessary").
			add(0b011,"reserved for future use").
			add(0b100,"extraction of VVC subpictures from a bitstream necessary").
			add(0b101,"reserved for future use").
			add(0b110,"extraction and merging (replacement) of VVC subpictures necessary").
			add(0b111,"reserved for future use").
			build();
			
	
	public record SubPicture(int component_tag, int vvc_subpicture_id) implements TreeNode{
		
		@Override
		public KVP getJTreeNode(int modus){

			KVP t = new KVP("sub_picture");
			t.add(new KVP("component_tag",component_tag));
			t.add(new KVP("vvc_subpicture_id",vvc_subpicture_id));

			return t;
		}

		
	}

	private int default_service_mode;
	private int service_description_present;
	private int number_of_vvc_subpictures;
	
	private List<SubPicture> subPicturesList = new ArrayList<>();
	private int reserved_zero_future_use;
	private int processing_mode;
	private DVBString service_description;

	public VvcSubpicturesDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		
		default_service_mode = getInt(b,  3, 1, 0b1000_0000) >> 7;
		service_description_present = getInt(b, 3, 1, 0b0100_0000) >> 6;
		number_of_vvc_subpictures = getInt(b,  3, 1, Utils.MASK_6BITS);
		
		int localOffset = 4;
		
		for (int i=0;i<number_of_vvc_subpictures;i++) {
			int component_tag = getInt(b, localOffset++, 1, Utils.MASK_6BITS);
			int vvc_subpicture_id = getInt(b, localOffset++, 1, Utils.MASK_6BITS);
			subPicturesList.add(new SubPicture(component_tag, vvc_subpicture_id));
		}
		
		reserved_zero_future_use = getInt(b, localOffset, 1, 0b1111_1000) >> 3;
		processing_mode= getInt(b, localOffset++, 1,Utils.MASK_3BITS);
		if(service_description_present == 0b1) {
			service_description = new DVBString(b, localOffset);
		}
	}

	
	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("default_service_mode",default_service_mode));
		t.add(new KVP("service_description_present",service_description_present));
		t.add(new KVP("number_of_vvc_subpictures",number_of_vvc_subpictures));

		Utils.addListJTree(t, subPicturesList, modus, "subpictures");

		t.add(new KVP("reserved_zero_future_use",reserved_zero_future_use));
		t.add(new KVP("processing_mode",processing_mode).setDescription(processing_mode_list.get(processing_mode)));

		if(service_description_present == 0b1) {
			t.add(new KVP("service_description",service_description));
		}

		return t;
	}

}
