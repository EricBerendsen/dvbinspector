package nl.digitalekabeltelevisie.data.mpeg.descriptors.scte35;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

import static nl.digitalekabeltelevisie.util.Utils.*;

public class TimeDescriptor extends SCTE35Descriptor {

	private long TAI_seconds;
    private long TAI_ns;
    private int UTC_offset;

	public TimeDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		TAI_seconds = getLong(b, 6, 6, 0xFFFFFFFFFFFFL);
		TAI_ns = getLong(b, 12, 4, 0xFFFFFFFFL);
		UTC_offset = getInt(b, 16, 2, 0xFFFF);

	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("TAI_seconds", TAI_seconds));
		t.add(new KVP("TAI_ns", TAI_ns));
		t.add(new KVP("UTC_offset", UTC_offset));
		return t;
	}

}
