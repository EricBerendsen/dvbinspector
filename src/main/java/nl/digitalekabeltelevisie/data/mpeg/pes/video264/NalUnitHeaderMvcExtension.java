package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public class NalUnitHeaderMvcExtension implements TreeNode{

	private int non_idr_flag;
	private int priority_id;
	private int view_id;
	private int temporal_id;
	private int anchor_pic_flag;
	private int inter_view_flag;
	private int reserved_one_bit;

	public NalUnitHeaderMvcExtension(BitSource bs) {
		non_idr_flag = bs.u(1);
		priority_id = bs.u(6);
		view_id = bs.u(10);
		temporal_id = bs.u(3);
		anchor_pic_flag = bs.u(1);
		inter_view_flag = bs.u(1);
		reserved_one_bit = bs.u(1);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("nal_unit_header_mvc_extension");
		t.add(new KVP("non_idr_flag", non_idr_flag));
		t.add(new KVP("priority_id", priority_id));
		t.add(new KVP("view_id", view_id));
		t.add(new KVP("temporal_id", temporal_id));
		t.add(new KVP("anchor_pic_flag", anchor_pic_flag));
		t.add(new KVP("inter_view_flag", inter_view_flag));
		t.add(new KVP("reserved_one_bit", reserved_one_bit));

		return t;
	}

}
