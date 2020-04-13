package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;

public class AC4Descriptor extends DVBExtensionDescriptor {
	
	LookUpList ac4_channel_mode_list = new LookUpList.Builder().
			add(0 ,"Mono content").
			add(1 ,"Stereo content").
			add(2 ,"Multichannel content").
			add(3 ,"Reserved for future use").
			build();
								private int ac4_config_flag;
	private int ac4_toc_flag;
	private int reserved_zero_future_use;
	private int ac4_dialog_enhancement_enabled;
	private int ac4_channel_mode;
	private int reserved_zero_future_use2;
	private int ac4_toc_len;
	private byte[] ac4_dsi_byte;
	private byte[] additional_info_byte;
	

	public AC4Descriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);

		ac4_config_flag = getInt(b, offset + 3, 1, 0x80) >> 7;
		ac4_toc_flag = getInt(b, offset + 3, 1, 0x40) >> 6;
		reserved_zero_future_use = getInt(b, offset + 3, 1, MASK_6BITS);
		int off = offset + 4;
		if (ac4_config_flag == 1) {
			ac4_dialog_enhancement_enabled = getInt(b, off, 1, 0x80) >> 7;
			ac4_channel_mode = getInt(b, off, 1, 0x60) >> 5;
			reserved_zero_future_use2 = getInt(b, off, 1, MASK_5BITS);
			off++;
		}
		if (ac4_toc_flag == 1) {
			ac4_toc_len = getInt(b, off++, 1, MASK_8BITS);
			ac4_dsi_byte = getBytes(b, off, ac4_toc_len);
			off += ac4_toc_len;
		}
		if (off < (offset + descriptorLength + 2)) {
			additional_info_byte = getBytes(b, off, (offset + descriptorLength + 2) - off);
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("ac4_config_flag", ac4_config_flag, null)));
		t.add(new DefaultMutableTreeNode(new KVP("ac4_toc_flag", ac4_toc_flag, null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use", reserved_zero_future_use, null)));

		if (ac4_config_flag == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("ac4_dialog_enhancement_enabled", ac4_dialog_enhancement_enabled, null)));
			t.add(new DefaultMutableTreeNode(new KVP("ac4_channel_mode", ac4_channel_mode, ac4_channel_mode_list.get(ac4_channel_mode))));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_future_use", reserved_zero_future_use2, null)));
		}
		
		if (ac4_toc_flag == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("ac4_toc_len", ac4_toc_len, null)));
			t.add(new DefaultMutableTreeNode(new KVP("ac4_dsi_byte", ac4_dsi_byte, null)));
		}
		if(additional_info_byte!=null) {
			t.add(new DefaultMutableTreeNode(new KVP("additional_info_byte", additional_info_byte, null)));
		}

		return t;
	}

}
