/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class TerrestrialDeliverySystemDescriptor extends Descriptor{

	private final long frequency; // 32-bit uimsbf field giving the binary coded frequency value in multiples of 10 Hz.
	private final int bandwidth;
	private final int priority;
	private final int time_Slicing_indicator;
	
	private final int mpe_FEC_indicator;
	private final int reserved_future_use;
	private final int constellation;
	private final int hierarchy_information;
	private final int code_rate_HP_stream;
	private final int code_rate_LP_stream ;
	private final int guard_interval;
	private final int transmission_mode;
	private final int other_frequency_flag;
	private final long reserved_future_use2;

	public TerrestrialDeliverySystemDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		frequency = getLong(b, 2, 4, MASK_32BITS);
		bandwidth = getInt(b, 6, 1, 0b1110_0000) >>> 5;
		priority = getInt(b, 6, 1, 0b0001_0000) >>> 4;
		time_Slicing_indicator = getInt(b, 6, 1, 0b0000_1000) >>> 3;
		mpe_FEC_indicator = getInt(b, 6, 1, 0b0000_0100) >>> 2;
		reserved_future_use = getInt(b, 6, 1, 0b0000_0011);

		constellation = getInt(b, 7, 1, 0b1100_0000) >>> 6;
		hierarchy_information = getInt(b, 7, 1, 0b0011_1000) >>> 3;
		code_rate_HP_stream = getInt(b, 7, 1, 0b0000_0111);

		code_rate_LP_stream = getInt(b, 8, 1, 0b1110_0000) >>> 5;
		guard_interval = getInt(b, 8, 1, 0b0001_1000) >>> 3;
		transmission_mode = getInt(b, 8, 1, 0b0000_0110) >>> 1;
		other_frequency_flag = getInt(b, 8, 1, 0b0000_0001);
		reserved_future_use2 = getLong(b, 9, 4, MASK_32BITS);

	}

	public static String getBandwidtString(final int b) {
		return switch (b) {
		case 0 -> "8 MHz";
		case 1 -> "7 MHz";
		case 2 -> "6 MHz)";
		case 3 -> "5 MHz)";
		default -> "reserved for future use";
		};
	}

	public static String getPriorityString(final int p) {
		return switch (p) {
		case 0x00 -> "LP (low priority)";
		case 0x01 -> "HP (high priority)";
		default -> "error";
		};
	}

	public long getFrequency() {
		return frequency;
	}

	@Override
	public String toString() {
		return super.toString() + "Frequency=" + getFrequency() + ", priority=" + getPriorityString(priority);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);

		t.add(new KVP("frequency", frequency, Descriptor.formatTerrestrialFrequency(frequency)));
		t.add(new KVP("bandwidth", bandwidth, getBandwidtString(bandwidth)));
		t.add(new KVP("priority", priority, getPriorityString(priority)));
		t.add(new KVP("Time_Slicing_indicator", time_Slicing_indicator, getTimeSlicingString()));

		t.add(new KVP("MPE-FEC_indicator", mpe_FEC_indicator, getMpeFecString()));
		t.add(new KVP("reserved_future", reserved_future_use));
		t.add(new KVP("constellation", constellation, getConstellationString(constellation)));
		t.add(new KVP("hierarchy_information", hierarchy_information, getHierarchyInformationString(hierarchy_information)));

		t.add(new KVP("code_rate-HP_stream", code_rate_HP_stream, getCodeRateString(code_rate_HP_stream)));
		t.add(new KVP("code_rate-LP_stream", code_rate_LP_stream, getCodeRateString(code_rate_LP_stream)));
		t.add(new KVP("guard_interval", guard_interval, getGuardIntervalString(guard_interval)));

		t.add(new KVP("transmission_mode", transmission_mode, getTransmissionModeString(transmission_mode)));
		t.add(new KVP("other_frequency_flag", other_frequency_flag,
				other_frequency_flag == 1 ? "one or more other frequencies are in use." : "no other frequency is in use"));
		t.add(new KVP("reserved_future_use2", reserved_future_use2));

		return t;
	}

	public String getMpeFecString() {
		return mpe_FEC_indicator == 1 ? "MPE-FEC is not used" : "at least one elementary stream uses MPE-FEC.";
	}

	public static String getTransmissionModeString(int transmission_mode) {
		return switch (transmission_mode) {
		case 0 -> "2k mode";
		case 1 -> "8k mode";
		case 2 -> "4k mode";
		default -> "reserved for future use";
		};
	}

	public static String getGuardIntervalString(int guard_interval) {
		return switch (guard_interval) {
		case 0 -> "1/32";
		case 1 -> "1/16";
		case 2 -> "1/8";
		case 3 -> "1/4";
		default -> "Illegal value";
		};
	}

	public static String getCodeRateString(int code_rate) {
		return switch (code_rate) {
		case 0 -> "1/2";
		case 1 -> "2/3";
		case 2 -> "3/4";
		case 3 -> "5/6";
		case 4 -> "7/8";
		default -> "reserved for future use";
		};
	}

	public static String getHierarchyInformationString(int hierarchy_information) {
		return switch (hierarchy_information) {
		case 0 -> "non-hierarchical, native interleaver";
		case 1 -> "α = 1, native interleaver";
		case 2 -> "α = 2, native interleaver";
		case 3 -> "α = 4, native interleaver";
		case 4 -> "non-hierarchical, in-depth interleaver";
		case 5 -> "α = 1, in-depth interleaver";
		case 6 -> "α = 2, in-depth interleaver";
		case 7 -> "α = 4, in-depth interleaver";
		default -> "Illegal value";
		};
	}

	public static String getConstellationString(int constellation) {
		return switch (constellation) {
		case 0 -> "QPSK";
		case 1 -> "16-QAM";
		case 2 -> "64-QAM";
		default -> "reserved for future use";
		};
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public int getPriority() {
		return priority;
	}

	public int getTime_Slicing_indicator() {
		return time_Slicing_indicator;
	}

	public String getTimeSlicingString() {
		return time_Slicing_indicator == 1 ? "not used" : "at least one elementary stream uses Time Slicing.";
	}

	public int getMpe_FEC_indicator() {
		return mpe_FEC_indicator;
	}

	public int getReserved_future_use() {
		return reserved_future_use;
	}

	public int getConstellation() {
		return constellation;
	}

	public int getHierarchy_information() {
		return hierarchy_information;
	}

	public int getCode_rate_HP_stream() {
		return code_rate_HP_stream;
	}

	public int getCode_rate_LP_stream() {
		return code_rate_LP_stream;
	}

	public int getGuard_interval() {
		return guard_interval;
	}

	public int getTransmission_mode() {
		return transmission_mode;
	}

	public int getOther_frequency_flag() {
		return other_frequency_flag;
	}

	public long getReserved_future_use2() {
		return reserved_future_use2;
	}
}
