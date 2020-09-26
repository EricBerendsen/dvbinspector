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

package nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 *
 * based on TS 103 190-1 V1.3.1 (2018-02) 4.2.3.7 content_type
 */
public class ContentType implements TreeNode{
	
	
	LookUpList content_classifier_list = new LookUpList.Builder().
			add(0, "Main audio service: complete main").
			add(1, "Main audio service: music and effects").
			add(2, "Associated service: visually impaired").
			add(3, "Any associated service: hearing impaired").
			add(4, "Associated service: dialogue").
			add(5, "Any associated service: commentary").
			add(6, "Associated service: emergency").
			add(7, "Associated service: voice over").
			build();

	private int content_classifier;
	private int b_language_indicator;
	private int b_serialized_language_tag;
	private int b_start_tag;
	private int language_tag_chunk;
	private int n_language_tag_bytes;
	private byte[] language_tag_bytes;

	/**
	 * @param bs
	 */
	public ContentType(BitSource bs) {
		
		content_classifier = bs.readBits(3);
		b_language_indicator = bs.readBits(1);
		if (b_language_indicator == 1) { 
			b_serialized_language_tag= bs.readBits(1);
			if (b_serialized_language_tag == 1) { 
				b_start_tag = bs.readBits(1);
				language_tag_chunk = bs.readBits(16);
			} else {
				n_language_tag_bytes= bs.readBits(6);
				//for (i = 0; i < n_language_tag_bytes; i++) {
				language_tag_bytes = bs.readUnalignedBytes(n_language_tag_bytes);
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("content_type"));
		t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("content_classifier",content_classifier,content_classifier_list.get(content_classifier)))));
		t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("b_language_indicator",b_language_indicator,null))));

		if (b_language_indicator == 1) { 
			t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("b_serialized_language_tag",b_serialized_language_tag,null))));
			if (b_serialized_language_tag == 1) { 
				t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("b_start_tag",b_start_tag,null))));
				t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("language_tag_chunk",language_tag_chunk,null))));
			} else {
				t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("n_language_tag_bytes",n_language_tag_bytes,null))));
				t.add(new DefaultMutableTreeNode(new DefaultMutableTreeNode(new KVP("language_tag_bytes",language_tag_bytes,null))));
			}
		}

		return t;
	}

}
