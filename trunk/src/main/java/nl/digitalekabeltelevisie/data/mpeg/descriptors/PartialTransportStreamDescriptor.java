/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class PartialTransportStreamDescriptor extends Descriptor {

	private final int reserved1;
	private final int peakRate;
	private final int reserved2;
	private final int minimumOveralSmoothingRate;
	private final int reserved3;
	private final int maximumOveralSmoothingBuffer;

	public PartialTransportStreamDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		reserved1 = Utils.getInt(b, offset+2, 1, 0xC0)>>6;
		peakRate = Utils.getInt(b, offset+2, 3, Utils.MASK_22BITS);
		reserved2 = Utils.getInt(b, offset+5, 1, 0xC0)>>6;
		minimumOveralSmoothingRate = Utils.getInt(b, offset+5, 3, Utils.MASK_22BITS);
		reserved3 = Utils.getInt(b, offset+8, 1, 0xC0)>>6;
		maximumOveralSmoothingBuffer = Utils.getInt(b, offset+8, 2, Utils.MASK_14BITS);
	}

	@Override
	public String toString() {

		return super.toString() + "peakRate="+peakRate+",minimum_overal_smoothing_rate="+minimumOveralSmoothingRate+",maximum_overal_smoothing_buffer="+maximumOveralSmoothingBuffer;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		t.add(new DefaultMutableTreeNode(new KVP("DVB_reserved_future_use",reserved1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("peak_rate",peakRate,getPeakRateString(peakRate))));
		t.add(new DefaultMutableTreeNode(new KVP("DVB_reserved_future_use",reserved2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("minimum_overal_smoothing_rate",minimumOveralSmoothingRate,getMinimumOveralSmoothingRateString(minimumOveralSmoothingRate))));
		t.add(new DefaultMutableTreeNode(new KVP("DVB_reserved_future_use",reserved3,null)));
		t.add(new DefaultMutableTreeNode(new KVP("maximum_overal_smoothing_buffer",maximumOveralSmoothingBuffer,getMaximumOveralSmoothingBufferString(maximumOveralSmoothingBuffer))));

		return t;
	}

	/**
	 * @param peakRate2
	 * @return
	 */
	private static String getPeakRateString(final int peakRate2) {
		return "" + (peakRate2 * 400) +" bit/s";
	}

	/**
	 * @param minimumOveralSmoothingRate2
	 * @return
	 */
	private String getMinimumOveralSmoothingRateString(final int minimumOveralSmoothingRate2) {
		if(minimumOveralSmoothingRate2==0x3FFFFF){
			return "undefined";
		}else{
			return "" + (minimumOveralSmoothingRate * 400) +" bit/s";
		}
	}

	/**
	 * @param minimumOveralSmoothingRate2
	 * @return
	 */
	private static String getMaximumOveralSmoothingBufferString(final int maximumOveralSmoothingBuffer2) {
		if(maximumOveralSmoothingBuffer2==0x3FFF){
			return "undefined";
		}else{
			return "" + maximumOveralSmoothingBuffer2  +" bytes";
		}
	}

}
