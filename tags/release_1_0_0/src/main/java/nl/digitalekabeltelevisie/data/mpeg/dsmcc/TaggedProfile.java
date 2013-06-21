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
 * based on ETSI ES 201 812 V1.1.1, ETSI TS 102 809 V1.1.1, ETSI TS 102 796 V1.1.1
 */

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class TaggedProfile implements TreeNode {

	/** The tag, represented as a profile id. */
	private long profileId_tag = 0;
	private final long profile_data_length;

	public static final long TAG_BIOP=0x49534F06;
	public static final long TAG_LITE_OPTIONS=0x49534F05;

	// BIOP
	private final int profile_data_byte_order;
	private final int liteComponents_count;
	private final List<LiteComponent>liteComponents=new ArrayList<LiteComponent>();


	/** The associated profile data. */
	public byte profile_data[] = null;

	public TaggedProfile( final byte[] data, final int offset) {
		profileId_tag = getLong(data, offset, 4, MASK_32BITS);
		profile_data_length = getLong(data, offset+4, 4, MASK_32BITS);
		profile_data = copyOfRange(data, offset+8, offset+8+(int)profile_data_length);
		profile_data_byte_order = getInt(data,offset+8,1,MASK_8BITS);
		liteComponents_count = getInt(data,offset+9,1,MASK_8BITS);
		int liteOffset= offset+10;
		for (int i = 0; i < liteComponents_count; i++) {
			final LiteComponent liteComponent = LiteComponentsFactory.createLiteComponent(data,liteOffset);
			liteComponents.add(liteComponent);
			liteOffset += 5+liteComponent.getComponentDataLength(); // tag=4 bytes, length itself =1
		}

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(
				"TaggedProfile"));
		t.add(new DefaultMutableTreeNode(new KVP("profileId_tag",profileId_tag ,getProfileIdTagString(profileId_tag))));
		t.add(new DefaultMutableTreeNode(new KVP("profile_data_length",profile_data_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("profile_data",profile_data ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("profile_data_byte_order",profile_data_byte_order ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("liteComponents_count",liteComponents_count ,null)));
		addListJTree(t,liteComponents,modus,"LiteComponents");
		return t;
	}



	public static String getProfileIdTagString(final long profileId_tag){
		if(profileId_tag==TAG_BIOP){
			return "TAG_BIOP (BIOP Profile Body)";
		}else if(profileId_tag==TAG_LITE_OPTIONS){
			return "TAG_LITE_OPTIONS (Lite Options Profile Body)";
		}else{
			return null;
		}


	}
	public long getProfile_data_length() {
		return profile_data_length;
	}

	public byte[] getProfile_data() {
		return profile_data;
	}

	public long getProfileId_tag() {
		return profileId_tag;
	}

	public static long getTAG_BIOP() {
		return TAG_BIOP;
	}

	public static long getTAG_LITE_OPTIONS() {
		return TAG_LITE_OPTIONS;
	}

	public int getProfile_data_byte_order() {
		return profile_data_byte_order;
	}

	public int getLiteComponents_count() {
		return liteComponents_count;
	}

	public List<LiteComponent> getLiteComponents() {
		return liteComponents;
	}

}
