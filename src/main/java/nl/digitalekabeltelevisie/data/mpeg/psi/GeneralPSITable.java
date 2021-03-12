package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.PreferencesManager;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * This class represents a PSI table, without interpreting it.
 *
 */
public class GeneralPSITable extends AbstractPSITabel{

	private final Map<Integer, HashMap<Integer,TableSection []>> data = new HashMap<>();
	private List<TableSection> simpleSectionsd = new ArrayList<>();


	public GeneralPSITable(final PSI parent){
		super(parent);
	}

	public void update(final TableSection section){

		if(section.sectionSyntaxIndicator==0x01){

			final int tableId = section.getTableId();
			HashMap<Integer, TableSection[]> table = data.computeIfAbsent(tableId, k -> new HashMap<>());

			TableSection[] sections = table.computeIfAbsent(section.getTableIdExtension(), k -> new TableSection[section.getSectionLastNumber() + 1]);
			if(sections.length<=section.getSectionNumber()){ //resize if needed
				sections = Arrays.copyOf(sections, section.getSectionNumber()+1);
				table.put(section.getTableIdExtension(),sections);
			}
			if(sections[section.getSectionNumber()]==null){
				sections[section.getSectionNumber()] = section;
			}else{
				final TableSection last = sections[section.getSectionNumber()];
				updateSectionVersion(section, last);
			}
		}else{
			// look for duplicates, if so update counters on existing on

			for (final TableSection existingSection : simpleSectionsd) {
				if(existingSection.equals(section)){
					int previousPacketNo = existingSection.getLast_packet_no();
					int distance = section.getPacket_no() - previousPacketNo;
					if(distance>existingSection.getMaxPacketDistance()){
						existingSection.setMaxPacketDistance(distance);
					}
					if(distance<existingSection.getMinPacketDistance()){
						existingSection.setMinPacketDistance(distance);
					}

					existingSection.setLast_packet_no(section.getPacket_no());
					existingSection.setOccurrence_count(existingSection.getOccurrence_count()+1);
					return;
				}
			}
			simpleSectionsd.add(section);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PSI Data"));
		if(!PreferencesManager.isEnableGenericPSI()) {
		    t.add(new DefaultMutableTreeNode(GuiUtils.getErrorKVP ("Generic PSI not enabled, select 'Settings -> Enable Generic PSI' to enable ")));
		    return t;
		}
		final TreeSet<Integer> tableIDs = new TreeSet<>(data.keySet());

		for (Integer tableID : tableIDs) {
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("table_id", tableID, TableSection.getTableType(tableID)));
			final HashMap<Integer, TableSection[]> table = data.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<>(table.keySet());
			for (Integer tableIdExt : serviceSet) {
				final DefaultMutableTreeNode o = new DefaultMutableTreeNode(new KVP("table_id_extension", tableIdExt, null));
				final TableSection[] sections = table.get(tableIdExt);
				for (TableSection section : sections) {
					if (section != null) {
						if (!Utils.simpleModus(modus)) { // show all versions
							addSectionVersionsToJTree(o, section, modus);
						} else { // keep it simple
							o.add(section.getJTreeNode(modus));
						}
					}
				}
				n.add(o);
			}
			t.add(n);
		}
		if(!simpleSectionsd.isEmpty()){
			Utils.addListJTree(t, simpleSectionsd, modus, "syntax0");
		}
		return t;
	}


	public boolean exists(final int tableId, final int tableIdExtension, final int section){
		return ((data.get(tableId)!=null) &&
				(data.get(tableId).get(tableIdExtension)!=null) &&
				(data.get(tableId).get(tableIdExtension).length >section) &&
				(data.get(tableId).get(tableIdExtension)[section]!=null));
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = (PRIME * result) + ((simpleSectionsd == null) ? 0 : simpleSectionsd.hashCode());
		result = PRIME * result + data.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		final GeneralPSITable other = (GeneralPSITable) obj;
		if (simpleSectionsd == null) {
			if (other.simpleSectionsd != null){
				return false;
			}
		} else if (!simpleSectionsd.equals(other.simpleSectionsd)){
			return false;
		}
		return data.equals(other.data);
	}

	public Map<Integer, HashMap<Integer, TableSection[]>> getData() {
		return data;
	}

	public List<TableSection> getSimpleSectionsd() {
		return simpleSectionsd;
	}

}
