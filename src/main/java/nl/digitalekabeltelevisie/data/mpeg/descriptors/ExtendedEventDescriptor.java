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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ExtendedEventDescriptor extends Descriptor {

	public static class Item implements TreeNode{
		private DVBString itemDescription;
		private DVBString item;


		public Item(final DVBString itemDescription, final DVBString item) {
			super();
			this.itemDescription = itemDescription;
			this.item = item;
		}
		public DVBString getItem() {
			return item;
		}
		public void setItem(final DVBString item) {
			this.item = item;
		}
		public DVBString getItemDescription() {
			return itemDescription;
		}
		public void setItemDescription(final DVBString itemDescription) {
			this.itemDescription = itemDescription;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			DefaultMutableTreeNode s;
			if(simpleModus(modus)){
				s=new DefaultMutableTreeNode(new KVP("item",itemDescription+": "+item,null));
			}else{
				s=new DefaultMutableTreeNode(new KVP("item"));
				s.add(new DefaultMutableTreeNode(new KVP("item_description_encoding",itemDescription.getEncodingString(),null)));
				s.add(new DefaultMutableTreeNode(new KVP("item_description_length",itemDescription.getLength(),null)));
				s.add(new DefaultMutableTreeNode(new KVP("item_description",itemDescription,null)));
				s.add(new DefaultMutableTreeNode(new KVP("item_encoding",item.getEncodingString(),null)));
				s.add(new DefaultMutableTreeNode(new KVP("item_length",item.getLength(),null)));
				s.add(new DefaultMutableTreeNode(new KVP("item",item,null)));
			}

			return s;
		}


	}

	private final int descriptorNumber;
	private final int lastDescriptorNumber;
	private String  iso639LanguageCode;
	private final int lengthOfItems;
	private final List<Item> itemList = new ArrayList<Item>();

	private final DVBString text;

	public ExtendedEventDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		descriptorNumber = getInt(b, offset+2, 1, 0xF0)>>4;
		lastDescriptorNumber = getInt(b, offset+2, 1, MASK_4BITS);
		iso639LanguageCode = getISO8859_1String(b,offset+3,3);
		lengthOfItems= getInt(b, offset+6, 1, MASK_8BITS);

		int t=offset+7;
		while (t<(lengthOfItems+offset+7)) {
			final int item_description_length = getInt(b, t, 1, MASK_8BITS);
			final DVBString item_descripton=new DVBString(b, t);
			final int item_length = getInt(b, t+1+item_description_length, 1, MASK_8BITS);
			final DVBString item=new DVBString(b, t+1+item_description_length);

			final Item i = new Item(item_descripton,item);
			itemList.add(i);
			t+=2+item_description_length+item_length;
		}

		text = new DVBString(b,offset+7 +lengthOfItems);

	}

	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	public void setIso639LanguageCode(final String networkName) {
		this.iso639LanguageCode = networkName;
	}

	@Override
	public String toString() {
		return super.toString() + " text="+text;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		if(simpleModus(modus)){
			t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode ,null)));
			addListJTree(t,itemList, modus, "items");
			if(text.getLength()>0){
				t.add(new DefaultMutableTreeNode(new KVP("text",text,null)));
			}
		}else{

			t.add(new DefaultMutableTreeNode(new KVP("descriptor_number",descriptorNumber ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("last_descriptor_number",lastDescriptorNumber ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode ,null)));
			t.add(new DefaultMutableTreeNode(new KVP("length_of_items",lengthOfItems,null)));
			addListJTree(t,itemList, modus, "items");
			t.add(new DefaultMutableTreeNode(new KVP("text_encoding",text.getEncodingString(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("text_length",text.getLength(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("text",text,null)));
		}

		return t;
	}

	public int getDescriptorNumber() {
		return descriptorNumber;
	}

	public int getLastDescriptorNumber() {
		return lastDescriptorNumber;
	}

	public int getLengthOfItems() {
		return lengthOfItems;
	}

	public List<Item> getItemList() {
		return itemList;
	}

	public DVBString getText() {
		return text;
	}

}
