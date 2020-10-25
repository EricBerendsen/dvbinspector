/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video26x.sei;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * 
 * based on Rec. ITU-T H.265 v7 (11/2019) D.2.28 Mastering display colour volume SEI message syntax 
 * and D.3.28 Mastering display colour volume SEI message semantics
 * 
 * @author Eric
 *
 */
public class MasteringDisplayColourVolumeSei_message extends Sei_message {
	
	private int display_primaries_x[] = new int[3];
	private int display_primaries_y[] = new int[3];
	private int white_point_x;
	private int white_point_y;
	private int max_display_mastering_luminance;
	private int min_display_mastering_luminance;

	/**
	 * @param bitSourcePayload
	 */
	public MasteringDisplayColourVolumeSei_message(BitSource bitSource) {
		super(bitSource);

		// bitSource has been read by super(), so now convert payload back into BitSource
		var bitSourcePayload = new BitSource(payload, 0);

		for(int c = 0; c < 3; c++ ) {
			display_primaries_x[ c ] = bitSourcePayload.u(16);
			display_primaries_y[ c ] = bitSourcePayload.u(16);
		}
		white_point_x = bitSourcePayload.u(16);
		white_point_y = bitSourcePayload.u(16);
		max_display_mastering_luminance = bitSourcePayload.u(32);
		min_display_mastering_luminance = bitSourcePayload.u(32);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=super.getJTreeNode(modus);
		for(int c = 0; c < 3; c++ ) {
			s.add(new DefaultMutableTreeNode(new KVP("display_primaries_x["+c+"]",display_primaries_x[c],getCdm2x(display_primaries_x[c]))));
			s.add(new DefaultMutableTreeNode(new KVP("display_primaries_y["+c+"]",display_primaries_x[c],getCdm2y(display_primaries_y[c]))));
		}
		s.add(new DefaultMutableTreeNode(new KVP("white_point_x",white_point_x,getCdm2x(white_point_x))));
		s.add(new DefaultMutableTreeNode(new KVP("white_point_y",white_point_y,getCdm2y(white_point_y))));
		s.add(new DefaultMutableTreeNode(new KVP("max_display_mastering_luminance",max_display_mastering_luminance,getMax_display_mastering_luminanceString(max_display_mastering_luminance))));
		s.add(new DefaultMutableTreeNode(new KVP("min_display_mastering_luminance",min_display_mastering_luminance,getMin_display_mastering_luminance(min_display_mastering_luminance))));

		return s;
	}

	/**
	 * @param min_display_mastering_luminance2
	 * @return
	 */
	private String getMin_display_mastering_luminance(int min_display_mastering_luminance) {
		if(min_display_mastering_luminance>=1 && min_display_mastering_luminance<=50_000) {
			return String.format("%.4f cd/m²", 0.0001 * min_display_mastering_luminance);
		}
		return "unknown";
	}

	/**
	 * @param max_display_mastering_luminance2
	 * @return
	 */
	private String getMax_display_mastering_luminanceString(int max_display_mastering_luminance) {
		if(max_display_mastering_luminance>=50_000 && max_display_mastering_luminance<=100_000_000) {
			return String.format("%.4f cd/m²", 0.0001 * max_display_mastering_luminance);
		}
		return "unknown";
	}

	/**
	 * @param i
	 * @return
	 */
	private static String getCdm2x(int i) {
		if(i>=5 && i<=37000) {
			return String.format("%.4f",i* 0.00002);
		}
		return "unknown";
	}

	private static String getCdm2y(int i) {
		if(i>=5 && i<=42000) {
			return String.format("%.4f",i* 0.00002);
		}
		return "unknown";
	}


}
