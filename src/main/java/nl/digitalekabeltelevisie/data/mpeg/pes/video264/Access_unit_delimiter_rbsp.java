package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

public class Access_unit_delimiter_rbsp extends RBSP {
	
	private final int primary_pic_type;

	protected Access_unit_delimiter_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		primary_pic_type = bitSource.u(3);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("access_unit_delimiter_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("primary_pic_type",primary_pic_type,getPrimaryPicTypeString(primary_pic_type))));
		return t;
	}

	
	public static String getPrimaryPicTypeString(final int primary_pic_type) {

		StringBuilder r = new StringBuilder("slice_type values that may be present in the primary coded picture: ");
		switch (primary_pic_type) {
		case 0 : r.append("I");
			break;
		case 1 : r.append( "I, P");
		break;
		case 2 : r.append( "I, P, B");
		break;
		case 3 : r.append( "SI");
		break;
		case 4 : r.append( "SI, SP");
		break;
		case 5 : r.append( "I, SI");
		break;
		case 6 : r.append( "I, SI, P, SP");
		break;
		case 7 : r.append( "I, SI, P, SP, B");
		break;
		default:
			return "unknown";
		}
		return r.toString();
	}

}
