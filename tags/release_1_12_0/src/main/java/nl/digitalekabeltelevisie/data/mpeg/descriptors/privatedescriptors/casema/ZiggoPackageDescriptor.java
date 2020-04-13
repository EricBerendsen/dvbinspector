/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.casema;

import static nl.digitalekabeltelevisie.util.Utils.copyOfRange;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.BitString;

/**
 * Reverse engineered descriptor, used by Ziggo to indicate which package (subscription) this service belongs to.
 * 4 bytes, each bit indicates the service is part of that package. Services can belong to multiple packages
 * 
 * @author Eric
 *
 */
public class ZiggoPackageDescriptor extends Descriptor {

	private final byte[] packages;

	// names of the packages, starting at the last bit of 4 bytes
	private static String[] packageNames = {"TV Standard", // bit 0
		"package 1",  // bit 1
		"package 2",  // bit 2
		"package 3",  // bit 3
		"Turks",  // bit 4
		"Hindi",  // bit 5
		"package 6",  // bit 6
		"Eredivisie Live",  // bit 7
		"Film 1",  // bit 8
		"Sport 1",  // bit 9
		"Chinees",  // bit 10
		"Uitgebreid Turks",  // bit 11
		"Gay Lifestyle",  // bit 12
		"package 13",  // bit 13
		"package 14",  // bit 14
		"package 15",  // bit 15
		"Erotiek",  // bit 16
		"TV Plus",  // bit 17
		"TV Extra",  // bit 18
		"package 19",  // bit 19
		"package 20",  // bit 20
		"package 21",  // bit 21
		"package 22",  // bit 22
		"package 23",  // bit 23
		"package 24",  // bit 24
		"package 25",  // bit 25
		"package 26",  // bit 26
		"package 27",  // bit 27
		"package 28",  // bit 28
		"package 29",  // bit 29
		"package 30",  // bit 30
		"package 31",  // bit 31
	};


	public ZiggoPackageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		// packages seems to be 4 bytes, this should always work
		packages = copyOfRange(b, offset+2, offset+2+getDescriptorLength());
	}


	@Override
	public String getDescriptorname(){
		return "Ziggo Package Descriptor";
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);

		t.add(new DefaultMutableTreeNode(new KVP("packages",packages ,getPackagesList(packages))));
		return t;
	}

	/**
	 * @param packages
	 * @return a comma separated list of names of packages
	 */
	private static String getPackagesList(final byte[] packages){
		final StringBuilder t = new StringBuilder();
		final BitString bs = new BitString(packages);
		for (int i = 0; i < packageNames.length; i++) {
			final int bit = bs.getIntBitsEnd(1);
			if(bit==1){
				t.append(packageNames[i]).append(',');
			}
		}
		// remove last comma ','
		if(t.length()>0){
			t.delete(t.length()-1, t.length());
		}
		return t.toString();
	}


	public byte[] getPackages() {
		return packages;
	}

}
