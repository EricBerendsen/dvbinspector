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

package nl.digitalekabeltelevisie.data.mpeg.psi.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCCs;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.FNTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.FSTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.M7Fastscan;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.ONTSection;

/**
 * @author Eric
 *
 */
public class GeneralPsiTableHandler extends GeneralPidHandler {
	
	private static final Logger logger = Logger.getLogger(GeneralPsiTableHandler.class.getName());
	
	
	private PAT pat;
	private CAT cat;
	private BAT bat;
	private TSDT tsdt;
	private NIT nit;
	private SDT sdt;
	private PMTs pmts;
	private EIT eit;
	private TDT tdt;
	private TOT tot;
	private SIT sit;
	
	// private INT int_table; // INT and DFIT use same tableID, both not supported here
	private UNTs unt_table;
	private AITs ait_table;
	private RCTs rct_table;
	private DSMCCs dsm_table;
	private SCTE35 scte35_table;
	// private DFITs dfit_table;// INT and DFIT use same tableID, both not supported here
	
	private M7Fastscan m7fastscan;
	


	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		var node = new DefaultMutableTreeNode(new KVP("Interpreted PSI"));
		addToNodeIfNotNull(node, pat, modus);
		addToNodeIfNotNull(node, cat, modus);
		addToNodeIfNotNull(node, bat, modus);
		addToNodeIfNotNull(node, tsdt, modus);
		addToNodeIfNotNull(node, pmts, modus);
		addToNodeIfNotNull(node, nit, modus);
		addToNodeIfNotNull(node, sdt, modus);
		addToNodeIfNotNull(node, eit, modus);
		addToNodeIfNotNull(node, tdt, modus);
		addToNodeIfNotNull(node, tot, modus);
		addToNodeIfNotNull(node, sit, modus);
		addToNodeIfNotNull(node, ait_table, modus);
		addToNodeIfNotNull(node, unt_table, modus);
		addToNodeIfNotNull(node, rct_table, modus);
		addToNodeIfNotNull(node, scte35_table, modus);
		addToNodeIfNotNull(node, dsm_table, modus);
		addToNodeIfNotNull(node, m7fastscan, modus);
		return node;
	}

	@Override
	public String getMenuDescription() {
		return "Interpret as PSI";
	}


	
	@Override
	public void processTSPacket(TSPacket packet) {
		// EMPTY all action in postProcess 
	}

	@Override
	public void postProcess() {
		initialized = true;

		GeneralPSITable psi = pid.getPsi();
		Map<Integer, HashMap<Integer, TableSection[]>> typeSections = psi.getData();
		
		for(HashMap<Integer, TableSection[]> sectionsByTableId: typeSections.values()) {
			for(TableSection[] sectionsByTableIdExt:sectionsByTableId.values()) {
				for (TableSection section : sectionsByTableIdExt) {
					interpretSectionByTableId(section);
				}
			}
		}

		List<TableSection> lst = psi.getSimpleSectionsd();
		for (TableSection section : lst) {
			interpretSectionByTableId(section);
		}
	}
	
	/**
	 * @param section
	 */
	private void interpretSectionByTableId(TableSection section) {
		if (section == null) {
			return;
		}
		try {
			int tableID = section.getTableId();

			if (tableID == 0x00) {
				handlePAT(section);
			} else if (tableID == 0x01) { // conditional_access_section
				handleCAT(section);
			} else if (tableID == 0x02) { // program_map_section
				handlePMT(section);
			} else if (tableID == 0x03) { // TSDT
				handleTSDT(section);
			} else if ((tableID >= 0x37) && (tableID <= 0x3F)) { // DSM-CC
				handleDSMCC(section);
			} else if ((tableID == 0x40) || (tableID == 0x41)) { // network_information_section
				handleNIT(section);
			} else if (tableID == 0x4A) { // bouquet_association_section
				handleBAT(section);
			} else if ((tableID == 0x42) || (tableID == 0x46)) { // service_description_section
				handleSDT(section);
			} else if ((0x4E <= tableID) && (tableID <= 0x6F)) { // event_information_section
				handleEIT(section);
			} else if (tableID == 0x4b) { // update notification table section
				handleUNT(section);
				// TODO FONT/|INT section tableId conflict
			} else if (tableID == 0x70) { // TDT
				handleTDT(section);
			} else if (tableID == 0x73) { // TOT
				handleTOT(section);
			} else if (tableID == 0x74) { // application information section
				handleAIT(section);
			} else if (tableID == 0x76) { // related content section
				handleRCT(section);
			} else if (tableID == 0x7F) { // selection_information_section
				handleSIT(section);

			} else if ((tableID >= 0xBC) && (tableID <= 0xBE)) {
				handleFastScan(section);

			} else if (tableID == 0xFC) { // SCTE-35
				handleSCTE35(section);
			}
		} catch (RuntimeException rte) {
			logger.info(("Ignoring section with tableId:" + section.getTableId() + ", exception:" + rte.getMessage()));
		}

	}

	private void handlePAT(final TableSection section) {
		if (pat == null) {
			pat = new PAT(getTransportStream().getPsi());
		}
		PATsection p = new PATsection(section.getRaw_data(), pid);
		copyMetaData(section, p);
		pat.update(p);
	}

	private void handlePMT(final TableSection section) {
		if (pmts == null) {
			pmts = new PMTs(getTransportStream().getPsi());
		}
		PMTsection pmt = new PMTsection(section.getRaw_data(), pid);
		copyMetaData(section, pmt);
		pmts.update(pmt);
	}
	
	
	private void handleTSDT(final TableSection section) {
		if (tsdt == null) {
			tsdt = new TSDT(getTransportStream().getPsi());
		}
		TSDTsection s = new TSDTsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		tsdt.update(s);
	}
	
	
	

	private void handleNIT(final TableSection section) {
		if (nit == null) {
			nit = new NIT(getTransportStream().getPsi());
		}
		NITsection s = new NITsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		nit.update(s);
	}

	private void handleBAT(final TableSection section) {
		if (bat == null) {
			bat = new BAT(getTransportStream().getPsi());
		}
		BATsection s = new BATsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		bat.update(s);
	}

	private void handleEIT(final TableSection section) {
		if (eit == null) {
			eit = new EIT(getTransportStream().getPsi());
		}
		EITsection s = new EITsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		eit.update(s);
	}

	private void handleCAT(final TableSection section) {
		if (cat == null) {
			cat = new CAT(getTransportStream().getPsi());
		}
		CAsection s = new CAsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		cat.update(s);
	}

	private void handleSDT(final TableSection section) {
		if (sdt == null) {
			sdt = new SDT(getTransportStream().getPsi());
		}
		SDTsection s = new SDTsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		sdt.update(s);
	}

	private void handleAIT(final TableSection section) {
		if (ait_table == null) {
			ait_table = new AITs(getTransportStream().getPsi());
		}
		AITsection s = new AITsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		ait_table.update(s);
	}

	private void handleSIT(final TableSection section) {
		if (sit == null) {
			sit = new SIT(getTransportStream().getPsi());
		}
		SITsection s = new SITsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		sit.update(s);
	}

	private void handleUNT(final TableSection section) {
		if (unt_table == null) {
			unt_table = new UNTs(getTransportStream().getPsi());
		}
		UNTsection s = new UNTsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		unt_table.update(s);
	}

	private void handleRCT(final TableSection section) {
		if (rct_table == null) {
			rct_table = new RCTs(getTransportStream().getPsi());
		}
		RCTsection s = new RCTsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		rct_table.update(s);
	}

	private void handleSCTE35(final TableSection section) {
		if (scte35_table == null) {
			scte35_table = new SCTE35(getTransportStream().getPsi());
		}
		SpliceInfoSection s = new SpliceInfoSection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		scte35_table.update(s);
	}

	private void handleDSMCC(final TableSection section) {
		if (dsm_table == null) {
			dsm_table = new DSMCCs(getTransportStream().getPsi());
		}
		TableSectionExtendedSyntax s = new TableSectionExtendedSyntax(section.getRaw_data(), pid);
		copyMetaData(section, s);
		dsm_table.update(s);
	}

	private void handleTDT(final TableSection section) {
		if (tdt == null) {
			tdt = new TDT(getTransportStream().getPsi());
		}
		TDTsection s = new TDTsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		tdt.update(s);
	}

	private void handleTOT(final TableSection section) {
		if (tot == null) {
			tot = new TOT(getTransportStream().getPsi());
		}
		TOTsection s = new TOTsection(section.getRaw_data(), pid);
		copyMetaData(section, s);
		tot.update(s);
	}

	private void handleFastScan(final TableSection section) {
		if (m7fastscan == null) {
			m7fastscan = new M7Fastscan(getTransportStream().getPsi());
		}
		int tableId = section.getTableId();
		if (tableId == 0xBC) {
			FNTsection s = new FNTsection(section.getRaw_data(), pid);
			copyMetaData(section, s);
			m7fastscan.update(s);
		} else if (tableId == 0xBD) {
			FSTsection s = new FSTsection(section.getRaw_data(), pid);
			copyMetaData(section, s);
			m7fastscan.update(s);
		} else if (tableId == 0xBE) {
			ONTSection s = new ONTSection(section.getRaw_data(), pid);
			copyMetaData(section, s);
			m7fastscan.update(s);
		}
	}	
	
	private static void copyMetaData(TableSection source, TableSection dest) {
		dest.setFirst_packet_no(source.getFirst_packet_no());
		dest.setLast_packet_no(source.getLast_packet_no());
		dest.setMaxPacketDistance(source.getMaxPacketDistance());
		dest.setMinPacketDistance(source.getMinPacketDistance());
		dest.setOccurrence_count(source.getOccurrence_count());
	}


	private static void addToNodeIfNotNull(DefaultMutableTreeNode node,  TreeNode treeNode, int modus) {
		if(treeNode!=null) {
			node.add(treeNode.getJTreeNode(modus));
		}
	}
	

}
