package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

public class Seq_parameter_set_mvc_extension{

	private static final Logger	logger	= Logger.getLogger(Seq_parameter_set_mvc_extension.class.getName());

	
	private int num_views_minus1;
	
	private int[] view_id;
	private int[] num_anchor_refs_l0;
	private int [] [] anchor_ref_l0;
	private int [] [] anchor_ref_l1;
	private int [] num_anchor_refs_l1;

	private int[] num_non_anchor_refs_l0;

	private int[][] non_anchor_ref_l0;

	private int[] num_non_anchor_refs_l1;

	private int[][] non_anchor_ref_l1;

	private int num_level_values_signalled_minus1;

	private int[] level_idc;

	private int[] num_applicable_ops_minus1;

	private int[][] applicable_op_temporal_id;

	private int[][] applicable_op_num_target_views_minus1;

	private int[][][] applicable_op_target_view_id;

	private int[][] applicable_op_num_views_minus1;

	public Seq_parameter_set_mvc_extension(BitSource bitSource, int profile_idc) {
		
		num_views_minus1 = bitSource.ue();
		view_id = new int[num_views_minus1+1];
		for(int i = 0; i <= num_views_minus1; i++ ){
			view_id[ i ] = bitSource.ue();
		}
		num_anchor_refs_l0 = new int[num_views_minus1+1];
		anchor_ref_l0 = new int[num_views_minus1+1][];
		anchor_ref_l1 = new int[num_views_minus1+1][];
		num_anchor_refs_l1 = new int[num_views_minus1+1];
		for(int i = 1; i <= num_views_minus1; i++ ) {
			num_anchor_refs_l0[ i ] = bitSource.ue();
			anchor_ref_l0[i] = new int[num_anchor_refs_l0[ i ]+1];
			for(int j = 0; j < num_anchor_refs_l0[ i ]; j++ ){
				anchor_ref_l0[ i ][ j ] = bitSource.ue();
			}
			num_anchor_refs_l1[ i ] = bitSource.ue();
			anchor_ref_l1[i] = new int[num_anchor_refs_l1[ i ]+1];
			for(int j = 0; j < num_anchor_refs_l1[ i ]; j++ ){
				anchor_ref_l1[ i ][ j ] = bitSource.ue();
			}
		}
		num_non_anchor_refs_l0 = new int[num_views_minus1+1];
		non_anchor_ref_l0 = new int[num_views_minus1+1][];
		num_non_anchor_refs_l1= new int[num_views_minus1+1];
		non_anchor_ref_l1 = new int[num_views_minus1+1][];
		for(int i = 1; i <= num_views_minus1; i++ ) {
			num_non_anchor_refs_l0[ i ] = bitSource.ue();
			non_anchor_ref_l0[i] = new int[num_non_anchor_refs_l0[i]];
			for(int j = 0; j < num_non_anchor_refs_l0[i]; j++ ){
				non_anchor_ref_l0[ i ][ j ] = bitSource.ue();
			}
			num_non_anchor_refs_l1[ i ] = bitSource.ue();
			non_anchor_ref_l1[i] = new int[num_non_anchor_refs_l1[i]];
			for(int j = 0; j < num_non_anchor_refs_l1[ i ]; j++ ){
				non_anchor_ref_l1[ i ][ j ] = bitSource.ue();
			}
		}

		num_level_values_signalled_minus1 = bitSource.ue();
		level_idc = new int[num_level_values_signalled_minus1 + 1];
		num_applicable_ops_minus1 = new int[num_level_values_signalled_minus1 + 1];
		applicable_op_temporal_id = new int[num_level_values_signalled_minus1 + 1][];
		applicable_op_target_view_id = new int[num_level_values_signalled_minus1 + 1][][];
		applicable_op_num_target_views_minus1 = new int[num_level_values_signalled_minus1 + 1][];
		applicable_op_num_views_minus1 = new int[num_level_values_signalled_minus1 + 1][];
		for (int i = 0; i <= num_level_values_signalled_minus1; i++) {
			level_idc[i] = bitSource.u(8);
			num_applicable_ops_minus1[i] = bitSource.ue();
			applicable_op_temporal_id[i] = new int[num_applicable_ops_minus1[i] + 1];
			applicable_op_target_view_id[i] = new int[num_applicable_ops_minus1[i] + 1][];
			applicable_op_num_target_views_minus1[i] = new int[num_applicable_ops_minus1[i] + 1];
			applicable_op_num_views_minus1[i] = new int[num_applicable_ops_minus1[i] + 1];
			for (int j = 0; j <= num_applicable_ops_minus1[i]; j++) {
				applicable_op_temporal_id[i][j] = bitSource.u(3);
				applicable_op_num_target_views_minus1[i][j] = bitSource.ue();
				applicable_op_target_view_id[i][j] = new int[applicable_op_num_target_views_minus1[i][j]+1];
				for (int k = 0; k <= applicable_op_num_target_views_minus1[i][j]; k++) {
					applicable_op_target_view_id[i][j][k] = bitSource.ue();
				}
				applicable_op_num_views_minus1[i][j] = bitSource.ue();
			}
		}
		
		if( profile_idc == 134 ) {
			logger.warning("profile_idc == 134 not implemented");
		}
		
		
	}

	
	public KVP getJTreeNode(int modus, int profile_idc) {
		KVP t = new KVP("seq_parameter_set_mvc_extension");
		
		KVP numViewsMinus1 = new KVP("num_views_minus1",num_views_minus1,"number of coded views: "+(num_views_minus1+1));
		t.add(numViewsMinus1);
		for(int i = 0; i <= num_views_minus1; i++ ){
			numViewsMinus1.add(new KVP("view_id["+i+"]",view_id[ i ]));
		}
		

		for(int i = 1; i <= num_views_minus1; i++ ) {
			KVP num_anchor_refs_l0Node = new KVP("num_anchor_refs_l0["+i+"]",num_anchor_refs_l0[ i ]);
			t.add(num_anchor_refs_l0Node);
			for(int j = 0; j < num_anchor_refs_l0[ i ]; j++ ){
				num_anchor_refs_l0Node.add(new KVP("anchor_ref_l0["+i+","+j+"]",anchor_ref_l0[i][j]));
			}
			t.add(new KVP("num_anchor_refs_l1["+i+"]",num_anchor_refs_l1[ i ]));
			for(int j = 0; j < num_anchor_refs_l1[ i ]; j++ ){
				num_anchor_refs_l0Node.add(new KVP("anchor_ref_l1["+i+","+j+"]",anchor_ref_l1[i][j]));
			}
		}
		
		for(int i = 1; i <= num_views_minus1; i++ ) {
			KVP num_non_anchor_refs_l0Node = new KVP("num_non_anchor_refs_l0["+i+"]",num_non_anchor_refs_l0[ i ]);
			t.add(num_non_anchor_refs_l0Node);
			for(int j = 0; j < num_non_anchor_refs_l0[i]; j++ ){
				num_non_anchor_refs_l0Node.add(new KVP("non_anchor_ref_l0["+i+","+j+"]",non_anchor_ref_l0[i][j]));
			}
			KVP num_non_anchor_refs_l1Node = new KVP("num_non_anchor_refs_l1["+i+"]",num_non_anchor_refs_l1[ i ]);
			t.add(num_non_anchor_refs_l1Node);
			for(int j = 0; j < num_non_anchor_refs_l1[ i ]; j++ ){
				num_non_anchor_refs_l1Node.add(new KVP("non_anchor_ref_l1["+i+","+j+"]",non_anchor_ref_l1[i][j]));
			}
		}
		KVP numLevelValuesNode = new KVP("num_level_values_signalled_minus1",num_level_values_signalled_minus1);
		t.add(numLevelValuesNode);

		for (int i = 0; i <= num_level_values_signalled_minus1; i++) {
			numLevelValuesNode.add(new KVP("level_idc["+i+"]",level_idc[i]));
			KVP num_applicable_ops_minus1Node = new KVP("num_applicable_ops_minus1["+i+"]",num_applicable_ops_minus1[i]);
			numLevelValuesNode.add(num_applicable_ops_minus1Node);
			for (int j = 0; j <= num_applicable_ops_minus1[i]; j++) {
				num_applicable_ops_minus1Node.add(new KVP("applicable_op_temporal_id["+i+","+j+"]",applicable_op_temporal_id[i][j]));
				KVP applicable_op_num_target_views_minus1Node = new KVP("applicable_op_num_target_views_minus1["+i+","+j+"]",applicable_op_num_target_views_minus1[i][j]);
				num_applicable_ops_minus1Node.add(applicable_op_num_target_views_minus1Node);
				for (int k = 0; k <= applicable_op_num_target_views_minus1[i][j]; k++) {
					applicable_op_num_target_views_minus1Node.add(new KVP("applicable_op_target_view_id["+i+","+j+","+k+"]",applicable_op_target_view_id[i][j][k]));
				}
				num_applicable_ops_minus1Node.add(new KVP("applicable_op_num_views_minus1["+i+","+j+"]",applicable_op_num_views_minus1[i][j]));
			}
		}
		
		if (profile_idc == 134) {
			t.add(GuiUtils.getNotImplementedKVP("profile_idc == 134"));
		}

		return t;
	}

}
