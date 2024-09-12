/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ExtendedEventDescriptor extends LanguageDependentEitDescriptor{

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
		public DVBString getItemDescription() {
			return itemDescription;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP s;
			if (simpleModus(modus)) {
				s = new KVP("item", itemDescription + ": " + item, null);
			} else {
				s = new KVP("item");
				s.add(new KVP("item_description_encoding", itemDescription.getEncodingString()));
				s.add(new KVP("item_description_length", itemDescription.getLength()));
				s.add(new KVP("item_description", itemDescription));
				s.add(new KVP("item_encoding", item.getEncodingString()));
				s.add(new KVP("item_length", item.getLength()));
				s.add(new KVP("item", item));
			}

			return s;
		}

	}

	private final int descriptorNumber;
	private final int lastDescriptorNumber;
	private String  iso639LanguageCode;
	private final int lengthOfItems;
	private final List<Item> itemList = new ArrayList<>();

	private final DVBString text;

	public ExtendedEventDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		descriptorNumber = getInt(b, 2, 1, 0xF0)>>4;
		lastDescriptorNumber = getInt(b, 2, 1, MASK_4BITS);
		iso639LanguageCode = getISO8859_1String(b, 3,3);
		lengthOfItems= getInt(b, 6, 1, MASK_8BITS);

		int t=7;
		while (t<(lengthOfItems + 7)) {
			final int item_description_length = getInt(b, t, 1, MASK_8BITS);
			final DVBString item_descripton=new DVBString(b, t);
			final int item_length = getInt(b, t+1+item_description_length, 1, MASK_8BITS);
			final DVBString item=new DVBString(b, t+1+item_description_length);

			final Item i = new Item(item_descripton,item);
			itemList.add(i);
			t+=2+item_description_length+item_length;
		}

		text = new DVBString(b, 7 +lengthOfItems);

	}

	@Override
	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	@Override
	public String toString() {
		return super.toString() + " text="+text;
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		final KVP t = super.getJTreeNode(modus);
		if (simpleModus(modus)) {
			t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			addListJTree(t, itemList, modus, "items");
			if (text.getLength() > 0) {
				t.add(new KVP("text", text));
			}
		} else {
			t.add(new KVP("descriptor_number", descriptorNumber));
			t.add(new KVP("last_descriptor_number", lastDescriptorNumber));
			t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			t.add(new KVP("length_of_items", lengthOfItems));
			addListJTree(t, itemList, modus, "items");
			t.add(new KVP("text_encoding", text.getEncodingString()));
			t.add(new KVP("text_length", text.getLength()));
			t.add(new KVP("text", text));
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
