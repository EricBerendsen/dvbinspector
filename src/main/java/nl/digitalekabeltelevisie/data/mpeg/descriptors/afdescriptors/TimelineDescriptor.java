/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.math.BigInteger;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/**
 * Based on ISO/IEC 13818-1:2015/Amd.1/Cor.2:2016 (E) / Rec. ITU-T H.222.0 (2015)/Amd.1/Cor.2 (07/2016) 
 * "Amendment 1: Delivery of timeline for external data
 * Technical Corrigendum 2: Clarifications and
 * corrections on pause flag, URL construction
 * and adaptation field syntax"
 * 
 * @author Eric
 *
 */
public class TimelineDescriptor extends AFDescriptor {

	private static final Logger	logger	= Logger.getLogger(TimelineDescriptor.class.getName());

	
	private int has_timestamp;
	private int has_ntp;
	private int has_ptp;
	private int has_timecode;
	private int force_reload;
	private int paused;
	private int discontinuity;
	private int reserved;
	private int timeline_id;
	private long timescale;
	private BigInteger media_timestamp;

	public TimelineDescriptor(byte[] b, int offset) {
		super(b, offset);

		has_timestamp = getInt(b, offset + 2, 1, 0xC0) >> 6;
		has_ntp = getInt(b, offset + 2, 1, 0x20) >> 5;
		has_ptp = getInt(b, offset + 2, 1, 0x10) >> 4;
		has_timecode = getInt(b, offset + 2, 1, 0x0C) >> 2;
		force_reload = getInt(b, offset + 2, 1, 0x02) >> 1;
		paused = getInt(b, offset + 2, 1, 0x01);
		discontinuity = getInt(b, offset + 3, 1, 0x80) >> 7;
		reserved = getInt(b, offset + 3, 1, MASK_7BITS);
		timeline_id = getInt(b, offset + 4, 1, MASK_8BITS);
		int localOffset = offset + 5;
		
		 if (has_timestamp !=0) {
	         timescale = getLong(b, localOffset, 4, MASK_32BITS);
	         localOffset += 4;
	         if (has_timestamp==1) {
	            media_timestamp = getBigInteger(b, localOffset, 4);
	            localOffset += 4;
	         } else if (has_timestamp==2) {
	            media_timestamp= getBigInteger(b, localOffset, 8);
	            localOffset += 8;
	         }
	      }
		 
	      if (has_ntp!=0) {
	          logger.warning("has_ntp not implemented");
	       }
	       if (has_ptp!=0) {
		          logger.warning("has_ptp not implemented");
	       }
	       if (has_timecode!=0) {
		          logger.warning("has_timecode not implemented");
	       }

		
		
	}
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("has_timestamp", has_timestamp, null)));
		t.add(new DefaultMutableTreeNode(new KVP("has_ntp", has_ntp, null)));
		t.add(new DefaultMutableTreeNode(new KVP("has_ptp", has_ptp, null)));
		t.add(new DefaultMutableTreeNode(new KVP("has_timecode", has_timecode, null)));
		t.add(new DefaultMutableTreeNode(new KVP("force_reload", force_reload, null)));
		t.add(new DefaultMutableTreeNode(new KVP("paused", paused, null)));
		t.add(new DefaultMutableTreeNode(new KVP("discontinuity", discontinuity, null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved, null)));
		t.add(new DefaultMutableTreeNode(new KVP("timeline_id", timeline_id, null)));
		if (has_timestamp != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("timescale", timescale, null)));
			t.add(new DefaultMutableTreeNode(new KVP("media_timestamp", media_timestamp, null)));
		}
		
	      if (has_ntp!=0) {
	    	  t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("has_ntp")));
	       }
	       if (has_ptp!=0) {
		    	  t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("has_ptp")));
	       }
	       if (has_timecode!=0) {
		    	  t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("has_timecode")));
	       }

		return t;
	}

}
