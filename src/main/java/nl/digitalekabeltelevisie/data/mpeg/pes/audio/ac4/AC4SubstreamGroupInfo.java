/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * 
 * based on TS 103 190-2 V1.2.1 (2018-02) 6.2.1.6 ac4_substream_group_info
 * @author Eric
 *
 */
public class AC4SubstreamGroupInfo implements TreeNode {
	
	private static final Logger	logger	= Logger.getLogger(AC4SubstreamGroupInfo.class.getName());
	
	public class ChannelCodedSubstream  implements TreeNode {
		
		private int sus_ver;
		private AC4SubstreamInfoChan ac4_substream_info_chan;
		private Ac4HsfExtSubstreamInfo ac4HsfExtSubstreamInfo;
		/**
		 * @param sus_ver
		 */
		public void setSusVer(int sus_ver) {
			this.sus_ver = sus_ver;
		}
		
		public int getSusVer() {
			return sus_ver;
		}

		/**
		 * @param ac4_substream_info_chan
		 */
		public void setAC4SubstreamInfoChan(AC4SubstreamInfoChan ac4_substream_info_chan) {
			this.ac4_substream_info_chan = ac4_substream_info_chan;
		}

		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("channel__coded_substream"));
			t.add(new DefaultMutableTreeNode(new KVP("sus_ver",sus_ver,null)));
			t.add(ac4_substream_info_chan.getJTreeNode(modus));
			if (b_hsf_ext == 1) {
				t.add(ac4HsfExtSubstreamInfo.getJTreeNode(modus));				
			}

			return t;
		}

		/**
		 * @param ac4HsfExtSubstreamInfo
		 */
		public void setAc4HsfExtSubstreamInfo(Ac4HsfExtSubstreamInfo ac4HsfExtSubstreamInfo) {
			this.ac4HsfExtSubstreamInfo = ac4HsfExtSubstreamInfo;
		}
	}
		
	public class Substream  implements TreeNode {
		
		private int b_ajoc;
		private AC4SubstreamInfoObj ac4_substream_info_obj;
		private Ac4HsfExtSubstreamInfo ac4HsfExtSubstreamInfo;

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("substream"));
			t.add(new DefaultMutableTreeNode(new KVP("b_ajoc",b_ajoc,null)));
			if (b_ajoc == 1) {
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("ac4_substream_info_ajoc")));
			}else {
				t.add(ac4_substream_info_obj.getJTreeNode(modus));
			}
			if (b_hsf_ext == 1) {
				t.add(ac4HsfExtSubstreamInfo.getJTreeNode(modus));				
			}

			return t;
		}

		public int getB_ajoc() {
			return b_ajoc;
		}

		public void setB_ajoc(int b_ajoc) {
			this.b_ajoc = b_ajoc;
		}

		public AC4SubstreamInfo getAc4_substream_info_obj() {
			return ac4_substream_info_obj;
		}

		public void setAc4_substream_info_obj(AC4SubstreamInfoObj ac4_substream_info_obj) {
			this.ac4_substream_info_obj = ac4_substream_info_obj;
		}

		/**
		 * @param ac4HsfExtSubstreamInfo
		 */
		public void setAc4HsfExtSubstreamInfo(Ac4HsfExtSubstreamInfo ac4HsfExtSubstreamInfo) {
			this.ac4HsfExtSubstreamInfo = ac4HsfExtSubstreamInfo;
		}

	}

	private int b_substreams_present;
	private int b_hsf_ext;
	private int b_single_substream;
	private int n_lf_substreams;
	private int n_lf_substreams_minus2;
	private int b_channel_coded;
	//private int sus_ver;
	private int b_oamd_substream;
	
	private OamdSubstreamInfo oamd_substream_info;
	
	private List<ChannelCodedSubstream> channel_coded_substreams =  new ArrayList<>();
	private List<Substream> substreams =  new ArrayList<>();
	
	
	private int b_content_type;
	private ContentType content_type;

	/**
	 * @param bs
	 */
	public AC4SubstreamGroupInfo(BitSource bs, AC4Toc parentAc4Toc) {
		b_substreams_present = bs.readBits(1);
		b_hsf_ext = bs.readBits(1);
		b_single_substream = bs.readBits(1);
		
		if (b_single_substream == 1) {
			n_lf_substreams = 1;
		} else {
			n_lf_substreams_minus2 = bs.readBits(2);
			n_lf_substreams = n_lf_substreams_minus2 + 2;
			if (n_lf_substreams == 5) {
				n_lf_substreams += bs.variable_bits(2);
			}
		}
		
		
		b_channel_coded = bs.readBits(1); // b_channel_coded substreams contain channel-based audio
		
		if (b_channel_coded == 1) {
			for (int sus = 0; sus < n_lf_substreams; sus++) {
				ChannelCodedSubstream channel_coded_substream = new ChannelCodedSubstream();
				channel_coded_substreams.add(channel_coded_substream);
				
				int sus_ver;
				if (parentAc4Toc.getBitstream_version() == 1) {
					sus_ver = bs.readBits(1);			
				}else {
					sus_ver = 1;
				}
				
				channel_coded_substream.setSusVer(sus_ver);
				AC4SubstreamInfoChan ac4_substream_info_chan = new AC4SubstreamInfoChan(bs, parentAc4Toc, b_substreams_present);
				channel_coded_substream.setAC4SubstreamInfoChan(ac4_substream_info_chan);
				
				if (b_hsf_ext == 1) {
					Ac4HsfExtSubstreamInfo ac4_hsf_ext_substream_info = new  Ac4HsfExtSubstreamInfo(bs, b_substreams_present); // 6.2.1.14
					channel_coded_substream.setAc4HsfExtSubstreamInfo((ac4_hsf_ext_substream_info));
				}
			}
		}else{
			b_oamd_substream = bs.readBits(1);	
			if (b_oamd_substream ==1 ) {
				oamd_substream_info = new OamdSubstreamInfo(bs, b_substreams_present);
			}
			

			for (int sus = 0; sus < n_lf_substreams; sus++) {
				Substream substream = new Substream();
				substreams.add(substream);
				int b_ajoc = bs.readBits(1);	
				substream.setB_ajoc(b_ajoc);

				if (b_ajoc == 1) {
					logger.warning("if (b_ajoc == 1) { not implmented");
		//			ac4_substream_info_ajoc(b_substreams_present);
		//			if (b_hsf_ext) {
		//			ac4_hsf_ext_substream_info(b_substreams_present);
				} else {
					AC4SubstreamInfoObj ac4_substream_info_obj = new AC4SubstreamInfoObj(bs, parentAc4Toc, b_substreams_present);
					substream.setAc4_substream_info_obj(ac4_substream_info_obj);

					if (b_hsf_ext == 1) {
						Ac4HsfExtSubstreamInfo ac4_hsf_ext_substream_info = new  Ac4HsfExtSubstreamInfo(bs, b_substreams_present); // 6.2.1.14
						substream.setAc4HsfExtSubstreamInfo(ac4_hsf_ext_substream_info);
					}

				}
			}
		}
		b_content_type = bs.readBits(1);	
		if(b_content_type == 1) {
			content_type = new ContentType(bs);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ac4_substream_group_info"));
		t.add(new DefaultMutableTreeNode(new KVP("b_substreams_present",b_substreams_present,null)));
		t.add(new DefaultMutableTreeNode(new KVP("b_hsf_ext",b_hsf_ext,null)));
		t.add(new DefaultMutableTreeNode(new KVP("b_single_substream",b_single_substream,b_single_substream == 1?"n_lf_substreams = 1":null)));

		if (b_single_substream != 1) {
			t.add(new DefaultMutableTreeNode(new KVP("n_lf_substreams_minus2",n_lf_substreams_minus2,"n_lf_substreams = "+n_lf_substreams)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("b_channel_coded",b_channel_coded,b_channel_coded==1?"substreams contain channel-based audio":null)));
		if (b_channel_coded == 1) {
			addListJTree(t, channel_coded_substreams, modus, "channel coded substreams");
			// TODO if (b_hsf_ext) {
			// ac4_hsf_ext_substream_info(b_substreams_present); not implemented
			// }
		}else {
			t.add(new DefaultMutableTreeNode(new KVP("b_oamd_substream",b_oamd_substream,null)));
			if (b_oamd_substream ==1 ) {
				t.add(oamd_substream_info.getJTreeNode(modus));
			}
			addListJTree(t, substreams, modus, "A-JOC coded/direct-coded substreams");
		}
		t.add(new DefaultMutableTreeNode(new KVP("b_content_type",b_content_type,null)));
		
		if(b_content_type == 1) {
			t.add(content_type.getJTreeNode(modus));
		}

		return t;
	}

}
