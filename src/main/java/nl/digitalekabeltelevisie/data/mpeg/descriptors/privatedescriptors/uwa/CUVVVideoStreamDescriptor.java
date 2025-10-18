/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2024 by Paul Higgs (paul_higgs@hotmail.com)
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

 package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.uwa;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_32BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;
 
 public class CUVVVideoStreamDescriptor extends Descriptor {

    private final long cuvv_tag;						// 32 bits
    private final int cuva_version_map;					// 16 bits
    private final int terminal_provide_code;			// 16 bits
    private final int terminal_provide_oriented_code;   // 16 bits

	// T/UWA 005-2.1 table 5
	private static LookUpList vivid_version_strings = new LookUpList.
		Builder().
		add(0x0005, "1.0").
		add(0x0006, "2.0").
		add(0x0007, "3.0").
		add(0x0008, "4.0").
		build();

	public CUVVVideoStreamDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		cuvv_tag = getInt(b, 2, 4, MASK_32BITS);
		cuva_version_map = getInt(b, 6, 2, MASK_16BITS);
		terminal_provide_code = getInt(b, 8, 2, MASK_16BITS);
		terminal_provide_oriented_code = getInt(b, 10, 2, MASK_16BITS);
	}

    private static String FourCC(long v){
		char[] cccc = new char[] {
			Character.forDigit((int)(v & 0xFF000000) >> 24, 10), 
			Character.forDigit((int)(v & 0x00FF0000) >> 16, 10), 
			Character.forDigit((int)(v & 0x0000FF00) >> 8, 10), 
			Character.forDigit((int)(v & 0x000000FF), 10)};
        return new String(cccc);
    }
	private static String toHexString16(int val){
		return "0x" + String.format("%1$04x", val);
	}
	private static String toHexString32(long val){
		return "0x" + String.format("%1$08x", val);
	}
	private static String VersionMap(int val){
		String res = "";
		for (int i=0; i<16; i++)
			if (((val>>i) & 1) != 0)
				res += (i+1) + " ";
		return (res.length() != 0) ? "Versions present: " + res : "No versions specified.";
	}
	private static String HighestVersion(int val){
		String res =vivid_version_strings.get(val);
		if (res != null)
		return res;
		throw new IllegalArgumentException("Invalid value in terminal_provide_oriented_code:"+val);
	}


	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("cuvv_tag", toHexString32(cuvv_tag), FourCC(cuvv_tag)));
		t.add(new KVP("cuva_version_map", cuva_version_map, VersionMap(cuva_version_map)));
		t.add(new KVP("terminal_provide_code", terminal_provide_code, toHexString16(terminal_provide_code)));
		t.add(new KVP("terminal_provide_oriented_code", terminal_provide_oriented_code, HighestVersion(terminal_provide_oriented_code)));
		return t;
	}

    @Override
	public String getDescriptorname() {
		return "CUVV Video Stream Descriptor";
	}
 }