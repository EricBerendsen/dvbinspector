/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;

import java.util.Optional;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ExtendedChannelNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;

public class ATSCTables extends AbstractPSITabel {

	public static final int BASE_PID = 0x1FFB;

	private final STT stt;
	private final MGT mgt;
	private final VCT<TVCTsection> tvct;
	private final VCT<CVCTsection> cvct;

	public ATSCTables(final PSI parentPSI) {
		super(parentPSI);
		stt = new STT(parentPSI);
		mgt = new MGT(parentPSI);
		tvct = new VCT<>(parentPSI, "TVCT");
		cvct = new VCT<>(parentPSI, "CVCT");
	}

	public void update(final STTsection section) {
		stt.update(section);
	}

	public void update(final MGTsection section) {
		mgt.update(section);
	}

	public void update(final TVCTsection section) {
		tvct.update(section);
	}

	public void update(final CVCTsection section) {
		cvct.update(section);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP kvp = new KVP("ATSC PSIP");
		kvp.add(stt.getJTreeNode(modus));
		kvp.add(mgt.getJTreeNode(modus));
		kvp.add(tvct.getJTreeNode(modus));
		kvp.add(cvct.getJTreeNode(modus));
		return kvp;
	}

	public STT getStt() {
		return stt;
	}

	public MGT getMgt() {
		return mgt;
	}

	public VCT<TVCTsection> getTvct() {
		return tvct;
	}

	public VCT<CVCTsection> getCvct() {
		return cvct;
	}

	public Optional<String> getServiceNameOptional(final int programNumber) {
		return findServiceName(tvct, programNumber).or(() -> findServiceName(cvct, programNumber));
	}

	private static Optional<String> findServiceName(final VCT<? extends VCTsection> vct, final int programNumber) {
		VCTsection[] sections = vct.getSections();
		if (sections == null) {
			return Optional.empty();
		}
		for (VCTsection section : sections) {
			if (section == null) {
				continue;
			}
			for (VCTsection.VirtualChannel channel : section.getVirtualChannels()) {
				if (channel.getProgramNumber() == programNumber) {
					return Optional.of(getChannelLabel(channel));
				}
			}
		}
		return Optional.empty();
	}

	private static String getChannelLabel(final VCTsection.VirtualChannel channel) {
		String name = findGenericDescriptorsInList(channel.getDescriptorList(), ExtendedChannelNameDescriptor.class)
				.stream()
				.findFirst()
				.map(ExtendedChannelNameDescriptor::getLongChannelName)
				.filter(s -> !s.isBlank())
				.orElse(channel.getShortName());
		if (name == null || name.isBlank()) {
			return channel.getChannelNumberString();
		}
		return channel.getChannelNumberString() + " " + name;
	}
}
