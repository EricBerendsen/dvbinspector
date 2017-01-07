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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

public class Subset_seq_parameter_set_rbsp extends RBSP {
	
	private static final Logger	logger	= Logger.getLogger(Subset_seq_parameter_set_rbsp.class.getName());

	Seq_parameter_set_data seqParameterSetData;
	// private int svc_vui_parameters_present_flag;
	private Seq_parameter_set_mvc_extension seq_parameter_set_mvc_extension;
	Mvc_vui_parameters_extension mvc_vui_parameters_extension;

	private int bit_equal_to_one;

	private int mvc_vui_parameters_present_flag;

	private int additional_extension2_flag;

	public Subset_seq_parameter_set_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		seqParameterSetData = new Seq_parameter_set_data(bitSource);
		int profile_idc = seqParameterSetData.getProfile_idc();
		
		if((profile_idc == 83) || (profile_idc == 86)) {
			//seq_parameter_set_svc_extension( ) /* specified in Annex G */
			logger.warning("seq_parameter_set_svc_extension not implemented");
//			svc_vui_parameters_present_flag = bitSource.u(1);
//			if(svc_vui_parameters_present_flag == 1){
//				//svc_vui_parameters_extension( ) /* specified in Annex G */
//				logger.warning("svc_vui_parameters_extension not implemented");
//			}
		}else if((profile_idc == 118) || (profile_idc == 128) || (profile_idc == 134)){
			bit_equal_to_one = bitSource.f(1); /* equal to 1 */
			seq_parameter_set_mvc_extension = new Seq_parameter_set_mvc_extension(bitSource,profile_idc);/* specified in Annex H */
			mvc_vui_parameters_present_flag = bitSource.u(1);
			if(mvc_vui_parameters_present_flag == 1){
				mvc_vui_parameters_extension = new Mvc_vui_parameters_extension(bitSource); /* specified in Annex H */
			}
		} else if(profile_idc == 138) {
			bit_equal_to_one = bitSource.f(1);/* equal to 1 */
			//seq_parameter_set_mvcd_extension( ) /* specified in Annex I */
			logger.warning("seq_parameter_set_mvcd_extension not implemented");
		} else if( profile_idc == 139 ) {
			bit_equal_to_one = bitSource.f(1);/* equal to 1 */
			//seq_parameter_set_mvcd_extension( ) /* specified in Annex I */
			//seq_parameter_set_3davc_extension( ) /* specified in Annex J */
			logger.warning("seq_parameter_set_3davc_extension not implemented");
		}
		additional_extension2_flag = bitSource.u(1);		


	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("seq_parameter_set_rbsp"));
		seqParameterSetData.addToJTree(t,modus);

		int profile_idc = seqParameterSetData.getProfile_idc();
		
		if((profile_idc == 83) || (profile_idc == 86)) {
			//seq_parameter_set_svc_extension( ) /* specified in Annex G */
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("seq_parameter_set_svc_extension")));
//			svc_vui_parameters_present_flag = bitSource.u(1);
//			if(svc_vui_parameters_present_flag == 1){
//				//svc_vui_parameters_extension( ) /* specified in Annex G */
//				logger.warning("svc_vui_parameters_extension not implemented");
//			}
		}else if((profile_idc == 118) || (profile_idc == 128) || (profile_idc == 134)){
			t.add(new DefaultMutableTreeNode(new KVP("bit_equal_to_one",bit_equal_to_one,null)));
			t.add(seq_parameter_set_mvc_extension.getJTreeNode(modus,profile_idc));
			t.add(new DefaultMutableTreeNode(new KVP("mvc_vui_parameters_present_flag",mvc_vui_parameters_present_flag,null)));
			if(mvc_vui_parameters_present_flag == 1){
				t.add(mvc_vui_parameters_extension.getJTreeNode(modus));
			}
		} else if(profile_idc == 138) {
			t.add(new DefaultMutableTreeNode(new KVP("bit_equal_to_one",bit_equal_to_one,null)));
			//seq_parameter_set_mvcd_extension( ) /* specified in Annex I */
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("seq_parameter_set_mvcd_extension")));
		} else if( profile_idc == 139 ) {
			t.add(new DefaultMutableTreeNode(new KVP("bit_equal_to_one",bit_equal_to_one,null)));
			//seq_parameter_set_mvcd_extension( ) /* specified in Annex I */
			//seq_parameter_set_3davc_extension( ) /* specified in Annex J */
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("seq_parameter_set_3davc_extension")));
		}
		t.add(new DefaultMutableTreeNode(new KVP("additional_extension2_flag",additional_extension2_flag,null)));
		return t;
	}


	public Seq_parameter_set_data getSeqParameterSetData() {
		return seqParameterSetData;
	}

	public void setSeqParameterSetData(Seq_parameter_set_data seqParameterSetData) {
		this.seqParameterSetData = seqParameterSetData;
	}


}
