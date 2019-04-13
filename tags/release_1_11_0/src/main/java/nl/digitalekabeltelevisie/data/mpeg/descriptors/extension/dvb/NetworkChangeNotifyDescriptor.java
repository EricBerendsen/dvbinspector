package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class NetworkChangeNotifyDescriptor extends DVBExtensionDescriptor {
	
	
	
	private class Change  implements TreeNode {
		
		private String receiverCategoryToString(int receiverCategory) {
			switch(receiverCategory) {
			case 0x0:
				return "All receivers";
			case 0x1:
				return "DVB-T2 or DVB-S2 or DVB-C2 capable receivers only";
			case 0x2:
			case 0x3:
			case 0x4:
			case 0x5:
			case 0x6:
			case 0x7:
				return "Reserved for future use";
			default:
				return  "Invalid - outside range 0x0 to 0x7";
			}
		}
		
		private String changeTypeToString(int changeType) {
			switch(changeType) {
			case 0x0:
				return "Message only";
			case 0x1:
				return "Minor - default";
			case 0x2:
				return "Minor - multiplex removed";
			case 0x3:
				return "Minor - service changed";
			case 0x4:
			case 0x5:
			case 0x6:
			case 0x7:
				return "Reserved - Other minor changes";
			case 0x8:
				return "Major - Default";
			case 0x9:
				return "Major - multiplex frequency changed";
			case 0xa:
				return "Major - multiplex coverage changed";
			case 0xb:
				return "Major - multiplex added";
			case 0xc:
			case 0xd:
			case 0xe:
			case 0xf:
				return "reserved for future use for other major changes";
			default:
				return "Invalid Value - greater than 0xF";
			}
		}
		
		public int changeId;
		public int changeVersion;
		public byte[] startTime;
		public String duration;
		public int receiverCategory;
		public int invariantTsPresent;
		public int changeType;
		public int messageId;
		public int invariantTsTsId;
		public int invariantTsOnId;
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode tn=new DefaultMutableTreeNode(new KVP("change"));
			tn.add(new DefaultMutableTreeNode(new KVP("network_change_id", changeId, null)));
			tn.add(new DefaultMutableTreeNode(new KVP("network_change_version", changeVersion, null)));
			tn.add(new DefaultMutableTreeNode(new KVP("start_time_of_change", startTime, Utils.getUTCFormattedString(startTime))));
			tn.add(new DefaultMutableTreeNode(new KVP("change_duration", duration, Utils.formatDuration(duration))));
			tn.add(new DefaultMutableTreeNode(new KVP("receiver_category", receiverCategory, receiverCategoryToString(receiverCategory))));
			tn.add(new DefaultMutableTreeNode(new KVP("invariant_ts_present", invariantTsPresent, null)));
			tn.add(new DefaultMutableTreeNode(new KVP("change_type", changeType, changeTypeToString(changeType))));
			tn.add(new DefaultMutableTreeNode(new KVP("message_id", messageId, null)));
			if(invariantTsPresent == 1) {
				tn.add(new DefaultMutableTreeNode(new KVP("invariant_ts_tsid ", invariantTsTsId, null)));
				tn.add(new DefaultMutableTreeNode(new KVP("invariant_ts_onid ", invariantTsOnId, null)));
			}
			return tn;
		}
	}
	
	private class ChangeDescriptor  implements TreeNode {
		public int cellId;
		public int loopLength; 
		List<Change> changes = new ArrayList<Change>();
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode tn=new DefaultMutableTreeNode(new KVP("change_descriptor"));
			tn.add(new DefaultMutableTreeNode(new KVP("cell_id", cellId, null)));
			tn.add(new DefaultMutableTreeNode(new KVP("loop_length", loopLength, null)));
			Utils.addListJTree(tn, changes, modus, "changes");
			return tn;
		}
	}
	
	private List<ChangeDescriptor> descriptors = new ArrayList<ChangeDescriptor>();

	public NetworkChangeNotifyDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		
		int t= offset + 3;
		while(t < (offset + descriptorLength)) {
			ChangeDescriptor d = new ChangeDescriptor();
			d.cellId = Utils.getInt(b, t,2, Utils.MASK_16BITS);
			final int count = Utils.getInt(b, t+2, 1, Utils.MASK_8BITS);
			d.loopLength = count;
			t += 3;
			int outerByteCount = t;
			
			while((t-outerByteCount) < count) {
				Change c = new Change();
				c.changeId = Utils.getInt(b, t, 1, Utils.MASK_8BITS);
				c.changeVersion = Utils.getInt(b, t+1, 1, Utils.MASK_8BITS);
				c.startTime = Utils.getBytes(b, t+2, 5);
				c.duration = Utils.getBCD(b, (t+7)*2,6);
				c.receiverCategory = Utils.getInt(b, t+10, 1, 0xE0) >> 5;
				c.invariantTsPresent = Utils.getInt(b, t+10, 1, 0x10) >> 4;
				c.changeType = Utils.getInt(b, t+10, 1, 0x0F);
				c.messageId = Utils.getInt(b, t+11, 1, Utils.MASK_8BITS);
				t += 12;
				if(c.invariantTsPresent == 1) {
					c.invariantTsTsId = Utils.getInt(b, t, 2, Utils.MASK_16BITS);
					c.invariantTsOnId = Utils.getInt(b, t+2, 2, Utils.MASK_16BITS);
					t += 4;
				}
				d.changes.add(c);
			}
			descriptors.add(d);
			t = outerByteCount + count;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		
		Utils.addListJTree(t, descriptors, modus, "networkChanges" );
		
		return t;
	}
	
	

}
