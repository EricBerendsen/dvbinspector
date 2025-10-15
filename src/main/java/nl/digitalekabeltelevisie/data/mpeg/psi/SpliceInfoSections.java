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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

public class SpliceInfoSections extends AbstractPSITabel{

	private final List<SpliceInfoSection> spliceInfoSectionList = new ArrayList<>();
	private int pid = 0;

	public SpliceInfoSections(final PSI parent){
		super(parent);
	}

	public void update(final SpliceInfoSection section){
		spliceInfoSectionList.add(section);
		pid=section.getParentPID().getPid();
	}

	@Override
	public KVP getJTreeNode(int modus) {

		String programName = "";
		List<PMTsection> pmts = getParentPSI().getPmts().findPMTsFromComponentPID(pid);

		if (!pmts.isEmpty()) {
			int programNumber = pmts.get(0).getProgramNumber();
			programName = " for program 	" + programNumber + " (" + getParentPSI().getSdt().getServiceNameForActualTransportStream(programNumber)
					+ ")";
		}
		KVP t = new KVP("SpliceInfoSections PID=" + pid + programName);

		for (SpliceInfoSection toTsection : spliceInfoSectionList) {
			t.add(toTsection.getJTreeNode(modus));
		}
		return t;
	}

	public List<SpliceInfoSection> getSpliceInfoSectionList() {
		return spliceInfoSectionList;
	}




}
