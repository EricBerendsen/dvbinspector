/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import java.util.*;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class NetworkChangeNotifyDescriptor extends DVBExtensionDescriptor {
	
	
	
	private static class Change  implements TreeNode {
		
		private static String receiverCategoryToString(int receiverCategory) {
            return switch (receiverCategory) {
                case 0x0 -> "All receivers";
                case 0x1 -> "DVB-T2 or DVB-S2 or DVB-C2 capable receivers only";
                case 0x2, 0x3, 0x4, 0x5, 0x6, 0x7 -> "Reserved for future use";
                default -> "Invalid - outside range 0x0 to 0x7";
            };
		}
		
		private static String changeTypeToString(int changeType) {
            return switch (changeType) {
                case 0x0 -> "Message only";
                case 0x1 -> "Minor - default";
                case 0x2 -> "Minor - multiplex removed";
                case 0x3 -> "Minor - service changed";
                case 0x4, 0x5, 0x6, 0x7 -> "Reserved - Other minor changes";
                case 0x8 -> "Major - Default";
                case 0x9 -> "Major - multiplex frequency changed";
                case 0xa -> "Major - multiplex coverage changed";
                case 0xb -> "Major - multiplex added";
                case 0xc, 0xd, 0xe, 0xf -> "reserved for future use for other major changes";
                default -> "Invalid Value - greater than 0xF";
            };
		}

        private int changeId;
        private int changeVersion;
        private byte[] startTime;
        private String duration;
        private int receiverCategory;
        private int invariantTsPresent;
        private int changeType;
        private int messageId;
        private int invariantTsTsId;
        private int invariantTsOnId;
		
		@Override
		public KVP getJTreeNode(int modus) {
			KVP tn=new KVP("change");
			tn.add(new KVP("network_change_id", changeId));
			tn.add(new KVP("network_change_version", changeVersion));
			tn.add(new KVP("start_time_of_change", getStartTime(), Utils.getUTCFormattedString(getStartTime())));
			tn.add(new KVP("change_duration", duration, Utils.formatDuration(duration)));
			tn.add(new KVP("receiver_category", receiverCategory, receiverCategoryToString(receiverCategory)));
			tn.add(new KVP("invariant_ts_present", invariantTsPresent));
			tn.add(new KVP("change_type", changeType, changeTypeToString(changeType)));
			tn.add(new KVP("message_id", messageId));
			if(invariantTsPresent == 1) {
				tn.add(new KVP("invariant_ts_tsid ", invariantTsTsId));
				tn.add(new KVP("invariant_ts_onid ", invariantTsOnId));
			}
			return tn;
		}

        public int getChangeId() {
            return changeId;
        }

        public void setChangeId(int changeId) {
            this.changeId = changeId;
        }

        public int getChangeVersion() {
            return changeVersion;
        }

        public void setChangeVersion(int changeVersion) {
            this.changeVersion = changeVersion;
        }

        public byte[] getStartTime() {
            return startTime;
        }

        public void setStartTime(byte[] startTime) {
            this.startTime = startTime;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public int getReceiverCategory() {
            return receiverCategory;
        }

        public void setReceiverCategory(int receiverCategory) {
            this.receiverCategory = receiverCategory;
        }

        public int getInvariantTsPresent() {
            return invariantTsPresent;
        }

        public void setInvariantTsPresent(int invariantTsPresent) {
            this.invariantTsPresent = invariantTsPresent;
        }

        public int getChangeType() {
            return changeType;
        }

        public void setChangeType(int changeType) {
            this.changeType = changeType;
        }

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        public int getInvariantTsTsId() {
            return invariantTsTsId;
        }

        public void setInvariantTsTsId(int invariantTsTsId) {
            this.invariantTsTsId = invariantTsTsId;
        }

        public int getInvariantTsOnId() {
            return invariantTsOnId;
        }

        public void setInvariantTsOnId(int invariantTsOnId) {
            this.invariantTsOnId = invariantTsOnId;
        }
    }
	
	private static class ChangeDescriptor  implements TreeNode {
		public int cellId;
		public int loopLength; 
		List<Change> changes = new ArrayList<>();
		
		@Override
		public KVP getJTreeNode(int modus) {
			KVP tn=new KVP("change_descriptor");
			tn.add(new KVP("cell_id", cellId));
			tn.add(new KVP("loop_length", loopLength));
			Utils.addListJTree(tn, changes, modus, "changes");
			return tn;
		}
	}
	
	private List<ChangeDescriptor> descriptors = new ArrayList<>();

	public NetworkChangeNotifyDescriptor(byte[] b, TableSection parent) {
		super(b,  parent);
		
		int t= 3;
		while(t < (descriptorLength)) {
			ChangeDescriptor d = new ChangeDescriptor();
			d.cellId = Utils.getInt(b, t,2, Utils.MASK_16BITS);
			int count = Utils.getInt(b, t+2, 1, Utils.MASK_8BITS);
			d.loopLength = count;
			t += 3;
			int outerByteCount = t;
			
			while((t-outerByteCount) < count) {
				Change c = new Change();
				c.setChangeId(Utils.getInt(b, t, 1, Utils.MASK_8BITS));
				c.setChangeVersion(Utils.getInt(b, t+1, 1, Utils.MASK_8BITS));
				c.setStartTime(Utils.getBytes(b, t+2, 5));
				c.setDuration(Utils.getBCD(b, (t+7)*2,6));
				c.setReceiverCategory(Utils.getInt(b, t+10, 1, 0xE0) >> 5);
				c.setInvariantTsPresent(Utils.getInt(b, t+10, 1, 0x10) >> 4);
				c.setChangeType(Utils.getInt(b, t+10, 1, 0x0F));
				c.setMessageId(Utils.getInt(b, t+11, 1, Utils.MASK_8BITS));
				t += 12;
				if(c.getInvariantTsPresent() == 1) {
					c.setInvariantTsTsId(Utils.getInt(b, t, 2, Utils.MASK_16BITS));
					c.setInvariantTsOnId(Utils.getInt(b, t+2, 2, Utils.MASK_16BITS));
					t += 4;
				}
				d.changes.add(c);
			}
			descriptors.add(d);
			t = outerByteCount + count;
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		
		Utils.addListJTree(t, descriptors, modus, "networkChanges" );
		
		return t;
	}
	
	

}
