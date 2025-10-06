package nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

import static nl.digitalekabeltelevisie.util.Utils.*;

public class TimeDescriptor extends SCTE35Descriptor {

    private String identifier;
	private long TAI_seconds;
    private long TAI_ns;
    private int UTC_offset;


	public TimeDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);

        identifier = getString(b,offset+2,4);
        TAI_seconds = getLong(b,offset+6,6, 0xFFFFFFFFFFFFL);
        TAI_ns = getLong(b,offset+12,4, 0xFFFFFFFFL);
        UTC_offset = getInt(b,offset+16, 2, 0xFFFF);

	}

	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("identifier",identifier ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("TAI_seconds",TAI_seconds ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("TAI_ns",TAI_ns ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("UTC_offset",UTC_offset ,null)));
		return t;
	}

}
