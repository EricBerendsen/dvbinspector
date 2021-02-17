package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;

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

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.LinkageDescriptor.BrandHomeTransponder;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.ONTSection.OperatorBrand;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class M7Fastscan implements TreeNode {
	
	Map<Integer, Map<Integer,OperatorFastscan>> operators = new HashMap<>();
	ONTSection[] ontSections; 

	@SuppressWarnings("unused")
	private PSI parentPSI;

	public static boolean isValidM7Code(int code) {
		return (code>= 0x7701) && (code<= 0x77FF);
	}
	
	public M7Fastscan(PSI psi) {
		this.parentPSI = psi;
	}


	public void update(final ONTSection section){
		if(ontSections == null) {
			ontSections = new ONTSection[section.getSectionLastNumber()+1];
		}
		if(ontSections[section.getSectionNumber()]==null){
			ontSections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = ontSections[section.getSectionNumber()];
			AbstractPSITabel.updateSectionVersion(section, last);
		}	
	}
	
	public void update(final FSTsection section){
		final int pid = section.getParentPID().getPid();
		int operator = section.getOperatorNetworkID();
		
		findOrCreateOperatorFastscan(operator,pid).update(section);
	}

	
	private OperatorFastscan findOrCreateOperatorFastscan(int operator, int pid) {
		Map<Integer, OperatorFastscan> p = operators.computeIfAbsent(operator, k -> new HashMap<>());
		OperatorFastscan o = p.computeIfAbsent(pid, k -> new OperatorFastscan(pid,this));
		return o;
	}


	public void update(final FNTsection section){
		final int pid = section.getParentPID().getPid();
		int operator = section.getOperatorNetworkID();
		
		findOrCreateOperatorFastscan(operator,pid).update(section);
	}
	
	public String getOperatorName(int operator_network_id) {

		if (ontSections != null) {
			for (ONTSection ontSection : ontSections) {
				if (ontSection != null) {
					String r = ontSection.getOperatorName(operator_network_id);
					if (r != null) {
						return r;
					}
				}
			}
		}
		return null;
	}
	
	

	public String getOperatorSubListName(int operatorNetworkId, int fstPid) {
		
		String r = null;

		final NIT nit = parentPSI.getNit();
		final int actualNetworkID = nit.getActualNetworkID();
		final List<Descriptor> descriptors = nit.getNetworkDescriptors(actualNetworkID);
		
		Optional<LinkageDescriptor> optionalHomeTP_location_descriptor = descriptors.stream()
		    .filter(LinkageDescriptor.class::isInstance)
		    .map (LinkageDescriptor.class::cast)
		    .filter(k -> (k.getLinkageType() >= 0x88 &&k.getLinkageType() <=0x8a)) // linkage_type(M7 Fastscan Home TP location descriptor)
		    .findFirst();
	    
		 if(optionalHomeTP_location_descriptor.isPresent()) {
			 LinkageDescriptor homeTP_location_descriptor = optionalHomeTP_location_descriptor.get();
			 Optional<BrandHomeTransponder> optionalBrandHomeTransponder = homeTP_location_descriptor.getM7BrandHomeTransponderList()
			 	.stream()
			 	.filter(k -> k.getOperator_network_id() == operatorNetworkId)
			 	.filter(k -> k.getFst_pid() == fstPid)
			 	.findFirst();
			 

			 
			if (optionalBrandHomeTransponder.isPresent()) {
				BrandHomeTransponder brandHomeTransponder = optionalBrandHomeTransponder.get();
				int sublist_id = brandHomeTransponder.getOperator_sublist_id();
				r = "sublist_id: " + sublist_id;
				if(ontSections!=null) {
					for(ONTSection ontSection:ontSections) {
						if(ontSection!=null) {
							String sublistNamme = ontSection.getOperatorSublistName(operatorNetworkId,sublist_id);
							if(sublistNamme != null) {
								return r+" : "+sublistNamme ;
							}
						}
					}
				}
				return r;
			}
		 }

		return null;
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode( new KVP("M7 Fastscan"));
		
		if(ontSections!=null) {
			KVP kvp = new KVP("ONT");
			DefaultMutableTreeNode ont = new DefaultMutableTreeNode(kvp);
			kvp.setTableSource(()->getTableModelOnt());
			for (final ONTSection ontSection : ontSections) {
				if(ontSection!= null){
					AbstractPSITabel.addSectionVersionsToJTree(ont, ontSection, modus);
				}
			}
			
			t.add(ont);
		}

		for (Integer operatorId : new TreeSet<>(operators.keySet())) {
			Map<Integer, OperatorFastscan> operatorsInPid = operators.get(operatorId);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("operator_network_id",operatorId,getOperatorName(operatorId)));
			t.add(n);
			for (Integer pid : new TreeSet<>(operatorsInPid.keySet())) {
				OperatorFastscan operatorFastscan = operatorsInPid.get(pid);
				n.add(operatorFastscan.getJTreeNode(modus));
			}
			
		}
			
		return t;
	}

	public TableModel getTableModelOnt() {
		FlexTableModel<ONTSection,OperatorBrand> tableModel =  new FlexTableModel<>(ONTSection.buildOntTableHeader());

		for (final ONTSection ontSection : ontSections) {
			if (ontSection != null) {
				tableModel.addData(ontSection, ontSection.getOperatorBrandList());
			}
		}

		tableModel.process();
		return tableModel;
	}

	public Map<Integer, Map<Integer, OperatorFastscan>> getOperators() {
		return operators;
	}

	public ONTSection[] getOntSections() {
		return ontSections;
	}


}
