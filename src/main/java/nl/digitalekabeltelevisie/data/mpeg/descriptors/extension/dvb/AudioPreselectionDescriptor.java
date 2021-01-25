/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.*;

public class AudioPreselectionDescriptor extends DVBExtensionDescriptor {

	
	LookUpList audio_rendering_indication_list = new LookUpList.Builder().
			add(0,"no preference given for the reproduction channel layout").
			add(1,"preferred reproduction channel layout is stereo").
			add(2 ,"preferred reproduction channel layout is two-dimensional (e.g. 5.1 multi-channel)").
			add(3 ,"preferred reproduction channel layout is three-dimensional").
			add(4 ,"content is pre-rendered for consumption with headphones").
			add(5, 7 ,"reserved for future use").
			build();
	
	public class Preselection implements TreeNode{
		
		
		private int preselection_id;
		private int audio_rendering_indication;
		private int audio_description;
		private int spoken_subtitles;
		private int dialogue_enhancement;
		private int interactivity_enabled;
		private int language_code_present;
		private int text_label_present;
		private int multi_stream_info_present;
		private int future_extension;
		
		private String iso639LanguageCode;
		private int message_id;
		private int num_aux_components;
		private int reserved_zero_future_use2;
		private int[]component_tag;
		private int reserved_zero_future_use3;
		private int future_extension_length;
		private byte[] future_extension_byte;

		public Preselection(BitSource bs) {
			preselection_id = bs.readBits(5);
			audio_rendering_indication = bs.readBits(3);
			audio_description = bs.readBits(1);
			spoken_subtitles = bs.readBits(1);
			dialogue_enhancement = bs.readBits(1);
			interactivity_enabled = bs.readBits(1);
			language_code_present = bs.readBits(1);
			text_label_present = bs.readBits(1);
			multi_stream_info_present = bs.readBits(1);
			future_extension = bs.readBits(1);
			if (language_code_present == 1) {
				byte[] iso_639_language_code_bytes = bs.readBytes(3);
				iso639LanguageCode = getISO8859_1String(iso_639_language_code_bytes, 0, 3);
			}
			
			if (text_label_present == 1) {
				message_id = bs.readBits(8);
			}
			
			if (multi_stream_info_present == 1) {
				num_aux_components = bs.readBits(3 );
				reserved_zero_future_use2 = bs.readBits(5 );
				component_tag = new int[num_aux_components];
				for (int j=0;j<num_aux_components;j++) {
					component_tag[j]=  bs.readBits(8);
				}
			}
			
			if (future_extension == 1) {
				reserved_zero_future_use3 = bs.readBits(3);
				future_extension_length = bs.readBits(5);
				future_extension_byte = bs.readBytes(3);
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("preselection"));
			s.add(new DefaultMutableTreeNode(new KVP("preselection_id",preselection_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("audio_rendering_indication",audio_rendering_indication,audio_rendering_indication_list.get(audio_rendering_indication, "??"))));
			s.add(new DefaultMutableTreeNode(new KVP("audio_description",audio_description,null)));
			s.add(new DefaultMutableTreeNode(new KVP("spoken_subtitles",spoken_subtitles,null)));
			s.add(new DefaultMutableTreeNode(new KVP("dialogue_enhancement",dialogue_enhancement,null)));
			s.add(new DefaultMutableTreeNode(new KVP("interactivity_enabled",interactivity_enabled,null)));
			s.add(new DefaultMutableTreeNode(new KVP("language_code_present",language_code_present,null)));
			s.add(new DefaultMutableTreeNode(new KVP("text_label_present",text_label_present,null)));
			s.add(new DefaultMutableTreeNode(new KVP("multi_stream_info_present",multi_stream_info_present,null)));
			s.add(new DefaultMutableTreeNode(new KVP("future_extension",future_extension,null)));
			if (language_code_present == 1) {
				s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			}
			if (text_label_present == 1) {
				s.add(new DefaultMutableTreeNode(new KVP("message_id",message_id,null)));
			}

			if (multi_stream_info_present == 1) {
				s.add(new DefaultMutableTreeNode(new KVP("num_aux_components",num_aux_components,null)));
				s.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use2,null)));
				for (int j=0;j<num_aux_components;j++) {
					s.add(new DefaultMutableTreeNode(new KVP("component_tag["+j+"]",component_tag[j],null)));
				}
			}
			
			if (future_extension == 1) {
				s.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use3,null)));
				s.add(new DefaultMutableTreeNode(new KVP("future_extension_length",future_extension_length,null)));
				s.add(new DefaultMutableTreeNode(new KVP("future_extension_byte",future_extension_byte,null)));
			}

			return s;
		}
	}
		
	
	private int num_preselections;
	private int reserved_zero_future_use;
	private List<Preselection> preselections = new ArrayList<>();
	

	public AudioPreselectionDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		num_preselections = getInt(b, offset+3, 1, 0xF8)>>3;
		reserved_zero_future_use = getInt(b, offset+3, 1, MASK_3BITS);
		BitSource bs = new BitSource(b, offset+4);
		for(int i=0;i<num_preselections;i++) {
			preselections.add(new Preselection(bs));
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("num_preselections",num_preselections,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use",reserved_zero_future_use,null)));

		addListJTree(t,preselections,modus,"preselections");
		return t;
	}
}
