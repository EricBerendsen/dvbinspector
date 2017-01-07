package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class HEVCVideoDescriptor extends Descriptor {
	
	private final int profile_space;
	private final int tier_flag;
	private final int profile_idc;
	private final long profile_compatibility_indication;
	private final int progressive_source_flag;
	private final int interlaced_source_flag;
	private final int non_packed_constraint_flag;
	private final int frame_only_constraint_flag;
	private final long reserved_zero_44bits;
	private final int level_idc;
	private final int temporal_layer_subset_flag;
	private final int HEVC_still_present_flag;
	private final int HEVC_24hr_picture_present_flag;
	private final int sub_pic_hrd_params_not_present_flag;
	private final int reserved1;
	
	private int temporal_id_min;
	private int reserved2;
	private int temporal_id_max;
	private int reserved3;

	public HEVCVideoDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		profile_space = Utils.getInt(b, offset + 2, 1, 0xc0) >>> 6;
		tier_flag = Utils.getInt(b, offset + 2, 1, 0x20) >>> 5;
		profile_idc = Utils.getInt(b, offset + 2, 1, Utils.MASK_5BITS);
		profile_compatibility_indication = Utils.getLong(b, offset + 3, 4, Utils.MASK_32BITS);
		progressive_source_flag = Utils.getInt(b, offset + 7, 1, 0x80) >>> 7;
		interlaced_source_flag = Utils.getInt(b, offset + 7, 1, 0x40) >>> 6;
		non_packed_constraint_flag = Utils.getInt(b, offset + 7, 1, 0x20) >>> 5;
		frame_only_constraint_flag = Utils.getInt(b, offset + 7, 1, 0x10) >>> 4;
		reserved_zero_44bits = Utils.getLong(b, offset + 7, 6, 0xfffffffffffL);
		level_idc = Utils.getInt(b, offset + 13, 1, Utils.MASK_8BITS);
		temporal_layer_subset_flag = Utils.getInt(b, offset + 14, 1, 0x80) >>> 7;
		HEVC_still_present_flag = Utils.getInt(b, offset + 14, 1, 0x40) >>> 6;
		HEVC_24hr_picture_present_flag = Utils.getInt(b, offset + 14, 1, 0x20) >>> 5;
		sub_pic_hrd_params_not_present_flag = Utils.getInt(b, offset + 14, 1, 0x10) >>> 4;
		reserved1 = Utils.getInt(b, offset + 14, 1, Utils.MASK_4BITS);
		if(temporal_layer_subset_flag == 1) {
			temporal_id_min = Utils.getInt(b, offset + 15, 1, 0xe0) >>> 5;
			reserved2 = Utils.getInt(b, offset + 15, 1, Utils.MASK_5BITS);
			temporal_id_max = Utils.getInt(b, offset + 16, 1, 0xe0) >>> 5;
			reserved3 = Utils.getInt(b, offset + 16, 1, Utils.MASK_5BITS);
		}
	}
	
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("profile_space",profile_space,null)));
		t.add(new DefaultMutableTreeNode(new KVP("tier_flag",tier_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("profile_idc",profile_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("profile_compatibility_indication",profile_compatibility_indication,null)));
		t.add(new DefaultMutableTreeNode(new KVP("progressive_source_flag",progressive_source_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("interlaced_source_flag",interlaced_source_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("non_packed_constraint_flag",non_packed_constraint_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_only_constraint_flag",frame_only_constraint_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_44bits",reserved_zero_44bits,null)));
		t.add(new DefaultMutableTreeNode(new KVP("level_idc",level_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("temporal_layer_subset_flag",temporal_layer_subset_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("HEVC_still_present_flag",HEVC_still_present_flag,
			HEVC_still_present_flag == 1 ?
			"HEVC video stream or the HEVC highest temporal sub-layer representation may include HEVC still pictures" :
			"the associated HEVC video stream shall not contain HEVC still pictures")));
		t.add(new DefaultMutableTreeNode(new KVP("HEVC_24hr_picture_present_flag",HEVC_24hr_picture_present_flag,
			HEVC_24hr_picture_present_flag == 1 ?
			"HEVC video stream or the HEVC highest temporal sub-layer representation may contain HEVC 24-hour pictures" :
			"the associated HEVC video stream shall not contain HEVC 24-hour pictures")));
		t.add(new DefaultMutableTreeNode(new KVP("sub_pic_hrd_params_not_present_flag",sub_pic_hrd_params_not_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved1",reserved1,null)));
		if(temporal_layer_subset_flag == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("temporal_id_min",temporal_id_min,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved2",reserved2,null)));
			t.add(new DefaultMutableTreeNode(new KVP("temporal_id_max",temporal_id_max,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved3",reserved3,null)));
		}
		return t;
	}
}
