package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;

import static nl.digitalekabeltelevisie.util.Utils.MASK_1BIT;
import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_7BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class HEVCTimingAndHRDDescriptor extends MPEGExtensionDescriptor {

	private final int hrd_management_valid_flag;
	private final int reserved1;
	private final int picture_and_timing_info_present;

	private int _90kHz_flag;
	private int reserved2;
	private long n;
	private long k;
	private long num_units_in_tick;
	
	public HEVCTimingAndHRDDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		hrd_management_valid_flag = getInt(b, offset+2, 1, 0x80)>>>7;
		reserved1 = getInt(b, offset+2, 1, 0x7e)>>>1;
		picture_and_timing_info_present = getInt(b, offset+2, 1, MASK_1BIT);
		int t=0;
		if(picture_and_timing_info_present==1){
			_90kHz_flag = getInt(b, offset+3, 1, 0x80)>>>7;
			reserved2 = getInt(b, offset+3, 1, MASK_7BITS);
			t+=1;
			if(_90kHz_flag==0){
				n = getLong(b, offset+4, 4, MASK_32BITS);
				k = getLong(b, offset+8, 4, MASK_32BITS);
				t+=8;
			}
			num_units_in_tick = getLong(b, offset+3+t, 4, MASK_32BITS);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("hrd_management_valid_flag",hrd_management_valid_flag,
				hrd_management_valid_flag == 1
				? "Buffering Period SEI and Picture Timing SEI messages shall be present in the associated HEVC video stream"
				: "leak method shall be used for the transfer from MBn to EBn")));
		t.add(new DefaultMutableTreeNode(new KVP("reserved1",reserved1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("picture_and_timing_info_present",picture_and_timing_info_present,null)));
		if(picture_and_timing_info_present==1){
			t.add(new DefaultMutableTreeNode(new KVP("90kHz_flag",_90kHz_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved2",reserved2,null)));
			if(_90kHz_flag==0){
				t.add(new DefaultMutableTreeNode(new KVP("n",n,null)));
				t.add(new DefaultMutableTreeNode(new KVP("k",k,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("num_units_in_tick",num_units_in_tick,null)));

		}
		return t;
	}
}
