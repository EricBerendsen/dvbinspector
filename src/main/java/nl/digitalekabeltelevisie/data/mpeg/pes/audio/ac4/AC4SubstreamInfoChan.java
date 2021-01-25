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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * @author Eric
 *
 */
public class AC4SubstreamInfoChan extends AC4SubstreamInfo implements TreeNode {
	
	LookUpList channel_mode_list = new LookUpList.Builder().
			add(0, "Mono").
			add(1, "Stereo"). 
			add(2, "3.0").
			add(3, "5.0").
			add(4, "5.1").
			add(5, "7.0: 3/4/0 (L, C, R, Ls, Rs, Lrs, Rrs)").
			add(6, "7.1: 3/4/0.1 (L, C, R, Ls, Rs, Lrs, Rrs, LFE)").
			add(7, "7.0: 5/2/0 (L, C, R, Lw, Rw, Ls, Rs)").
			add(8, "7.1: 5/2/0.1 (L, C, R, Lw, Rw, Ls, Rs, LFE)").
			add(9, "7.0: 3/2/2 (L, C, R, Ls, Rs, Vhl, Vhr)").
			add(10, "7.1: 3/2/2.1 (L, C, R, Ls, Rs, Vhl, Vhr, LFE)").
			add(11, "7.0.4").
			add(12, "7.1.4").
			add(13, "9.0.4").
			add(14, "9.1.4").
			add(15, "22.2").
			add(16,255,"Reserved").
			build();
	
		private int channel_mode;
		private int b_4_back_channels_present;
		private int b_centre_present;
		private int top_channels_present;

		private AC4Toc parentAc4Toc;

		private int b_substreams_present;

		private int b_sf_multiplier;

		private int sf_multiplier;

		private int b_bitrate_info;

		private int bitrate_indicator;

		private int add_ch_base;

		private int substream_index;


	/**
	 * @param bs
	 * @param parentAc4Toc 
	 * @param b_substreams_present
	 */
	public AC4SubstreamInfoChan(BitSource bs, AC4Toc parentAc4Toc, int b_substreams_present) {
		
		this.parentAc4Toc = parentAc4Toc;
		this.b_substreams_present = b_substreams_present;
		
		channel_mode = readChannelMode(bs);
		
		
		if (channel_mode == 16) { // if (channel_mode == 0b111111111) {
			channel_mode += bs.variable_bits(2);
		}
		
		if (channel_mode == 11 || channel_mode == 12 ||channel_mode == 13||channel_mode == 14) { // if (channel_mode in [0b11111100, 0b11111101, 0b111111100, 0b111111101]) {
			b_4_back_channels_present = bs.readBits(1);
			b_centre_present= bs.readBits(1);
			top_channels_present= bs.readBits(2);
		}
		
		if (parentAc4Toc.getFs_index() == 1) {
			b_sf_multiplier = bs.readBits(1);
			if (b_sf_multiplier == 1) {
				sf_multiplier = bs.readBits(1);
			}
		}
		
		b_bitrate_info = bs.readBits(1);
		if (b_bitrate_info == 1) {
			bitrate_indicator = readBitrateIndiciator(bs);
		}
		
		if (channel_mode == 7 || channel_mode == 8 ||  channel_mode == 9 || channel_mode == 10) {  // if (channel_mode in [0b1111010, 0b1111011, 0b1111100, 0b1111101]) {
			add_ch_base = bs.readBits(1);
		}
		
		// TODO find ac4_presentation_v1_info that uses this substream 
		// use that frame_rate_factor

		//for (int i = 0; i <  frame_rate_factor; i++) {
		for (int i = 0; i <  1; i++) { // TODO get frame_rate_factor
			int b_audio_ndot = bs.readBits(1);
			b_audio_ndot_list.add(new BAudioNdot(b_audio_ndot));
		}
		
		if (b_substreams_present == 1) {
			substream_index = bs.readBits(2);
			if (substream_index == 3) {
				substream_index += bs.variable_bits(2);
			}
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ac4_substream_info_chan"));
		t.add(new DefaultMutableTreeNode(new KVP("channel_mode",channel_mode,channel_mode_list.get(channel_mode))));

		if (channel_mode == 11 || channel_mode == 12 ||channel_mode == 13||channel_mode == 14) { // if (channel_mode in [0b11111100, 0b11111101, 0b111111100, 0b111111101]) {
			t.add(new DefaultMutableTreeNode(new KVP("b_4_back_channels_present",b_4_back_channels_present,null)));
			t.add(new DefaultMutableTreeNode(new KVP("b_centre_present",b_centre_present,null)));
			t.add(new DefaultMutableTreeNode(new KVP("top_channels_present",top_channels_present,null)));
		}
		
		if (parentAc4Toc.getFs_index() == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("b_sf_multiplier",b_sf_multiplier,null)));
			if (b_sf_multiplier == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("sf_multiplier",sf_multiplier,null)));
			}
		}
		if (parentAc4Toc.getFs_index() == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("b_sf_multiplier",b_sf_multiplier,null)));
			if (b_sf_multiplier == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("sf_multiplier",sf_multiplier,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("b_bitrate_info",b_bitrate_info,null)));
		if (b_bitrate_info == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("bitrate_indicator",bitrate_indicator,bitrate_indicator_list.get(bitrate_indicator))));
		}
		
		if (channel_mode == 7 || channel_mode == 8 ||  channel_mode == 9 || channel_mode == 10) {  // if (channel_mode in [0b1111010, 0b1111011, 0b1111100, 0b1111101]) {
			t.add(new DefaultMutableTreeNode(new KVP("add_ch_base",add_ch_base,null)));
		}

		addListJTree(t, b_audio_ndot_list, modus, "b_audio_ndots");
		
		if (b_substreams_present == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("substream_index",substream_index,null)));
		}

		return t;
	}
	
	
	// based on TS 103 190-2 V1.2.1 (2018-02) 6.3.2.7.2 channel_mode
	private static int readChannelMode(BitSource bs) {
		int bit1 = bs.readBits(1);
		if(bit1 == 0) { // 0b0 Mono 
			return 0;
		}
		// 0b1
		int bit2 = bs.readBits(1);
		if(bit2 == 0) { // 0b10 Stereo 
			return 1;
		}
		//0b11
		int bits34  = bs.readBits(2);
		if(bits34 == 0b00) { // 0b1100 3.0 
			return 2;
		}
		if(bits34 == 0b01) { // 0b1101 5.0 
			return 3;
		}
		if(bits34 == 0b10) { // 0b1110 5.1 
			return 4;
		}
		// 0b1111
		int bits567  = bs.readBits(3);
		if(bits567 == 0b000) { // 0b1111000 7.0: 3/4/0 (L, C, R, Ls, Rs, Lrs, Rrs) 
			return 5;
		}
		
		if(bits567 == 0b001) { // 0b1111001 7.1: 3/4/0.1 (L, C, R, Ls, Rs, Lrs, Rrs, LFE)
			return 6;
		}
		if(bits567 == 0b010) { // 0b1111010 7.0: 5/2/0 (L, C, R, Lw, Rw, Ls, Rs)
			return 7;
		}
		if(bits567 == 0b011) { // 0b1111011 7.1: 5/2/0.1 (L, C, R, Lw, Rw, Ls, Rs, LFE)
			return 8;
		}
		if(bits567 == 0b100) { // 0b1111100 7.0: 3/2/2 (L, C, R, Ls, Rs, Vhl, Vhr)
			return 9;
		}
		if(bits567 == 0b101) { // 0b1111101 7.1: 3/2/2.1 (L, C, R, Ls, Rs, Vhl, Vhr, LFE)
			return 10;
		}
		// 0b111111x 
		
		if(bits567 == 0b110) {
			// 0b1111110
			int bit8 = bs.readBits(1);
			if(bit8== 0b0) { // 0b11111100 7.0.4
				return 11;
			}
			return 12; // 0b11111101 7.1.4
		}
		// 0b1111111
		int bits89 = bs.readBits(2);
		if(bits89 == 0b00) {// 0b111111100 9.0.4
			return 13;
		}
		if(bits89 == 0b01) {// 0b111111101 9.1.4
			return 14;
		}
		if(bits89 == 0b10) {// 0b111111110 22.2
			return 15;
		}
		
		return 16; // 0b111 111 111… Reserved
		

	}

}
