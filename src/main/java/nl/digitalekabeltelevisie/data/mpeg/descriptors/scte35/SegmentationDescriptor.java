/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class SegmentationDescriptor extends SCTE35Descriptor {
	
	public class ComponentOffset implements TreeNode {

		private int component_tag;
		private int reserved;
		private long pts_offset;

		public ComponentOffset(int component_tag, int reserved, long pts_offset) {
			super();
			this.component_tag = component_tag;
			this.reserved = reserved;
			this.pts_offset = pts_offset;
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("component_offset"));
			t.add(new DefaultMutableTreeNode(new KVP("component_tag", component_tag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved, null)));
			t.add(new DefaultMutableTreeNode(new KVP("pts_offset", pts_offset, null)));
			return t;
		}

	}

	private byte[] segmentation_event_id;
	private int segmentation_event_cancel_indicator;
	private int reserved;
	private int program_segmentation_flag;
	private int segmentation_duration_flag;
	private int delivery_not_restricted_flag;
	private int web_delivery_allowed_flag;
	private int no_regional_blackout_flag;
	private int archive_allowed_flag;
	private int device_restrictions;
	private int reserved2;
	private int component_count;
	private List<ComponentOffset> componentOffsetList = new ArrayList<>();
	private long segmentation_duration;
	private int segmentation_upid_type;
	private int segmentation_upid_length;
	private byte[] segmentation_upid;
	private int segmentation_type_id;
	private int segment_num;
	private int segments_expected;
	private int sub_segment_num;
	private int sub_segments_expected;


	public SegmentationDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		segmentation_event_id = getBytes(b,offset+6,4);
		segmentation_event_cancel_indicator = getInt(b,offset+10,1,0b1000_0000)>>7; 
		reserved = getInt(b,offset+10,1,0b0111_1111); 
		if(segmentation_event_cancel_indicator == 0) {
			
			int localOffset = offset+11;
			program_segmentation_flag = getInt(b,localOffset,1,0b1000_0000)>>7; 
			segmentation_duration_flag = getInt(b,localOffset,1,0b0100_0000)>>6;
			delivery_not_restricted_flag = getInt(b,localOffset,1,0b0010_0000)>>5;
			if(delivery_not_restricted_flag == 0) {
				web_delivery_allowed_flag  = getInt(b,localOffset,1,0b0001_0000)>>4;
				no_regional_blackout_flag  = getInt(b,localOffset,1,0b0000_1000)>>3;
				archive_allowed_flag = getInt(b,localOffset,1,0b0000_0100)>>2; 
				device_restrictions  = getInt(b,localOffset,1,0b1000_0011); 
			} else {
				reserved2  = getInt(b,localOffset,1,0b0001_1111); 
			}
			localOffset++;
			if(program_segmentation_flag == 0) {
				component_count = getInt(b,localOffset++,1,MASK_8BITS);
				for (int i = 0; i < component_count; i++) {
					int component_tag = getInt(b,localOffset++,1,MASK_8BITS);
					int reserved = getInt(b,localOffset,1,0b1111_1110)>>1;
					long pts_offset = getLong(b, localOffset, 5, MASK_33BITS);
					ComponentOffset componentOffset = new ComponentOffset(component_tag, reserved, pts_offset);
					componentOffsetList.add(componentOffset);
					localOffset+= 5;
				}
			}
			if(segmentation_duration_flag == 1){
				segmentation_duration = getLong(b, localOffset, 5, MASK_40BITS);
				localOffset+=5;
			}
			segmentation_upid_type  = getInt(b,localOffset++,1,MASK_8BITS);
			segmentation_upid_length = getInt(b,localOffset++,1,MASK_8BITS);
			
			if(segmentation_upid_length>0){
				segmentation_upid = getBytes(b, localOffset, segmentation_upid_length);
				localOffset += segmentation_upid_length;
			}
			segmentation_type_id  = getInt(b,localOffset++,1,MASK_8BITS);
			segment_num = getInt(b,localOffset++,1,MASK_8BITS);
			segments_expected = getInt(b,localOffset++,1,MASK_8BITS);
			if(segmentation_type_id == 0x34 ||segmentation_type_id == 0x36) {
				sub_segment_num = getInt(b,localOffset++,1,MASK_8BITS);
				sub_segments_expected = getInt(b,localOffset++,1,MASK_8BITS);
			}
		}
	}

	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("segmentation_event_id", segmentation_event_id, null)));
		t.add(new DefaultMutableTreeNode(
				new KVP("segmentation_event_cancel_indicator", segmentation_event_cancel_indicator,
						segmentation_event_cancel_indicator == 1
								? "a previously sent segmentation event has been cancelled"
								: "no previously sent segmentation event has been cancelled")));
		t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved, null)));
		if (segmentation_event_cancel_indicator == 0) {
			t.add(new DefaultMutableTreeNode(new KVP("program_segmentation_flag", program_segmentation_flag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("segmentation_duration_flag", segmentation_duration_flag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("delivery_not_restricted_flag", delivery_not_restricted_flag, null)));

			if (delivery_not_restricted_flag == 0) {
				t.add(new DefaultMutableTreeNode(
						new KVP("web_delivery_allowed_flag", web_delivery_allowed_flag, null)));
				t.add(new DefaultMutableTreeNode(
						new KVP("no_regional_blackout_flag", no_regional_blackout_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("archive_allowed_flag", archive_allowed_flag, null)));
				t.add(new DefaultMutableTreeNode(new KVP("device_restrictions", device_restrictions, null)));

			} else {
				t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved2, null)));

			}
			if (program_segmentation_flag == 0) {
				t.add(new DefaultMutableTreeNode(new KVP("component_count", component_count, null)));
				Utils.addListJTree(t, componentOffsetList, modus, "Component Offsets");
			}
			if (segmentation_duration_flag == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("segmentation_duration", segmentation_duration, null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("segmentation_upid_type", segmentation_upid_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("segmentation_upid_length", segmentation_upid_length, null)));

			if (segmentation_upid_length > 0) {
				t.add(new DefaultMutableTreeNode(new KVP("segmentation_upid", segmentation_upid, null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("segmentation_type_id", segmentation_type_id, null)));
			t.add(new DefaultMutableTreeNode(new KVP("segment_num", segment_num, null)));
			t.add(new DefaultMutableTreeNode(new KVP("segments_expected", segments_expected, null)));
			if (segmentation_type_id == 0x34 || segmentation_type_id == 0x36) {
				t.add(new DefaultMutableTreeNode(new KVP("sub_segment_num", sub_segment_num, null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_segments_expected", sub_segments_expected, null)));
			}

		}

		return t;
	}

}
