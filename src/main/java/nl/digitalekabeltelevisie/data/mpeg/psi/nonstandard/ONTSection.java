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

package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyFunc;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyListFunc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7OperatorDiSEqCTDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7OperatorNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7OperatorOptionsDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7OperatorPreferencesDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7OperatorSublistNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class ONTSection extends TableSectionExtendedSyntax {
	
	private static final Logger	logger	= Logger.getLogger(ONTSection.class.getName());

	public static class OperatorBrand implements TreeNode{

		
		private int operator_network_id;
		private int operator_sublist_id;
		private int reserved_future_use;
		private int operator_descriptors_length;
		
		public List<Descriptor> descriptorList;
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("operator_brand",operator_network_id + "-"+ operator_sublist_id,null));

			t.add(new DefaultMutableTreeNode(new KVP("operator_network_id",operator_network_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("operator_sublist_id",operator_sublist_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_future_use",reserved_future_use,null)));
			t.add(new DefaultMutableTreeNode(new KVP("operator_descriptors_length",operator_descriptors_length,null)));

			Utils.addListJTree(t,descriptorList,modus,"operator_descriptors");

			return t;
		}

		public int getOperator_network_id() {
			return operator_network_id;
		}

		public void setOperator_network_id(int operator_network_id) {
			this.operator_network_id = operator_network_id;
		}

		public int getOperator_sublist_id() {
			return operator_sublist_id;
		}

		public void setOperator_sublist_id(int operator_sublist_id) {
			this.operator_sublist_id = operator_sublist_id;
		}

		public int getReserved_future_use() {
			return reserved_future_use;
		}

		public void setReserved_future_use(int reserved_future_use) {
			this.reserved_future_use = reserved_future_use;
		}

		public int getOperator_descriptors_length() {
			return operator_descriptors_length;
		}

		public void setOperator_descriptors_length(int operator_descriptors_length) {
			this.operator_descriptors_length = operator_descriptors_length;
		}

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}
		
	}

	private final int bouquet_descriptors_loop_length;
	
	private final List<Descriptor> bouquetDescriptorList;
	private final int operator_brands_loop_length;
	
	private final List<OperatorBrand> operatorBrandList;


	public ONTSection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);
		int reserved = Utils.getInt(raw_data.getData(), 8, 2, 0xF000) >>> 12;
		bouquet_descriptors_loop_length = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_12BITS);
		bouquetDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),10,bouquet_descriptors_loop_length,this);
		int reserved2 = Utils.getInt(raw_data.getData(), 10 + bouquet_descriptors_loop_length, 2, 0xF000) >>> 12;
		operator_brands_loop_length = Utils.getInt(raw_data.getData(), 10 +bouquet_descriptors_loop_length , 2, Utils.MASK_12BITS);
		operatorBrandList = buildOperatorBrandList(raw_data.getData(), 12+bouquet_descriptors_loop_length, operator_brands_loop_length);
	}
	
	private List<OperatorBrand> buildOperatorBrandList(byte[] data, int i, int operator_brands_loop_length2) {
		final ArrayList<OperatorBrand> r = new ArrayList<>();
		int t =0;
		try {
			while(t<operator_brands_loop_length2){
				final OperatorBrand c = new OperatorBrand();
				r.add(c);
				c.setOperator_network_id(Utils.getInt(data, i+t, 2, Utils.MASK_16BITS));
				c.setOperator_sublist_id(Utils.getInt(data, i+t+2, 1, Utils.MASK_8BITS));
				c.setReserved_future_use(Utils.getInt(data, i+t+3, 1, 0xF0)>>>4);
				final int operator_descriptors_length = Utils.getInt(data, i+t+3, 2, Utils.MASK_12BITS);
				c.setOperator_descriptors_length(operator_descriptors_length);
				c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,i+t+5,operator_descriptors_length,this));
				t+=5+operator_descriptors_length;
			}
		} catch (final RuntimeException re) {
			logger.info("RuntimeException in buildOperatorBrandList;"+re);
		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		KVP kvp = (KVP) t.getUserObject();
		kvp.setTableSource(this::getTableModel);

		t.add(new DefaultMutableTreeNode(new KVP("bouquet_descriptors_loop_length",bouquet_descriptors_loop_length,null)));
		Utils.addListJTree(t,bouquetDescriptorList,modus,"bouquet_descriptors");
		t.add(new DefaultMutableTreeNode(new KVP("operator_brands_loop_length",operator_brands_loop_length,null)));
		Utils.addListJTree(t,operatorBrandList,modus,"operator_brands_loop");


		return t;
	}

	public String getOperatorName(int operator_network_id) {
		
		for( OperatorBrand operatorBrand: operatorBrandList) {
			if(operatorBrand.getOperator_network_id()==operator_network_id) {
				List<M7OperatorNameDescriptor> operatorNameDescriptors = Descriptor.findGenericDescriptorsInList(operatorBrand.getDescriptorList(), M7OperatorNameDescriptor.class);
				if(operatorNameDescriptors.size()>=1) {
					return operatorNameDescriptors.get(0).getOperatorName().toString();
				}
			}
		}
		return null;
	}

	public String getOperatorSublistName(int operator_network_id, int operator_sublist_id) {
		
		for( OperatorBrand operatorBrand: operatorBrandList) {
			if((operatorBrand.getOperator_network_id()==operator_network_id) && 
					(operatorBrand.getOperator_sublist_id()==operator_sublist_id)){
				List<M7OperatorSublistNameDescriptor> operatorSublistNameDescriptors = Descriptor.findGenericDescriptorsInList(operatorBrand.getDescriptorList(), M7OperatorSublistNameDescriptor.class);
				if(operatorSublistNameDescriptors.size()>=1) {
					return operatorSublistNameDescriptors.get(0).getOperatorSublistName().toString();
				}
			}
		}
		return null;
	}

	protected String getTableIdExtensionLabel() {
		return "bouquet_id";
	}

	public TableModel getTableModel() {
		FlexTableModel<ONTSection,OperatorBrand> tableModel =  new FlexTableModel<>(buildOntTableHeader());

		tableModel.addData(this, getOperatorBrandList());

		tableModel.process();
		return tableModel;
	}

	static TableHeader<ONTSection,OperatorBrand>  buildOntTableHeader() {
		return new TableHeaderBuilder<ONTSection,OperatorBrand>().
				addRequiredRowColumn("operator network id", OperatorBrand::getOperator_network_id, Integer.class).
				addRequiredRowColumn("operator_sublist_id", OperatorBrand::getOperator_sublist_id, Integer.class).

				addOptionalRowColumn("operator name",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorNameDescriptor.class,
								ond -> ond.getOperatorName().toString()),
						String.class).


				addOptionalRowColumn("operator sublist name",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorSublistNameDescriptor.class,
								osnd -> osnd.getOperatorSublistName().toString()),
						String.class).

				addOptionalRowColumn("country code",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getCountry_code),
						String.class).
				addOptionalRowColumn("menu osd",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getMenu_ISO_639_language_code),
						String.class).
				addOptionalRowColumn("audio 1",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getAudio1_ISO_639_language_code),
						String.class).
				addOptionalRowColumn("audio 2",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getAudio2_ISO_639_language_code),
						String.class).
				addOptionalRowColumn("subs lang",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorPreferencesDescriptor.class,
								M7OperatorPreferencesDescriptor::getSubs_ISO_639_language_code),
						String.class).

				addOptionalRowColumn("subs usage",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getSubtitles_enabled),
						Integer.class).
				addOptionalRowColumn("parental control",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getParentalControlString),
						String.class).

				addOptionalRowColumn("FST char set",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getEncodingTypeString),
						String.class).
				addOptionalRowColumn("region",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								M7OperatorOptionsDescriptor.class,
								M7OperatorOptionsDescriptor::getSpecial_regions_setup),
						Integer.class).

				addOptionalRepeatingRowColumn("DiSeq Pos",
						operator -> findDescriptorApplyListFunc(operator.getDescriptorList(),
								M7OperatorDiSEqCTDescriptor.class,
								odd -> odd.getDiSEqCList().
								stream().
								map(M7OperatorDiSEqCTDescriptor.DiSEqC::getTotalPositionString).
								collect(Collectors.toList())),

						String.class).

				build();
	}

	public List<OperatorBrand> getOperatorBrandList() {
		return operatorBrandList;
	}

}
