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
 * based on ETSI TR 101 202 Implementation guidelines for Data Broadcasting
 */

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * See ETSI TS 102 809 V1.3.1 (2017-06) Table B.21: IOP::IOR syntax
 * 
 */
public class IOR implements TreeNode {

	private final long type_id_length;
	private byte[] type_id = null;
	private final long taggedProfiles_count;
	private List<TaggedProfile> profiles = null;
	private final int length;

	public static final byte[] TYPE_DIRECTORY = {'d','i','r',0};
	public static final byte[] TYPE_FILE = {'f','i','l',0};
	public static final byte[] TYPE_STREAM = {'s','t','r',0};
	public static final byte[] TYPE_SERVICEGATEWAY = {'s','r','g',0};
	public static final byte[] TYPE_STREAMEVENT = {'s','t','e',0};

	public IOR(byte[] privateDataByte, int offset) {
		type_id_length = getLong(privateDataByte, offset+0, 4, Utils.MASK_32BITS);
		type_id = copyOfRange(privateDataByte, offset + 4, offset + 4 + (int) type_id_length);
		taggedProfiles_count = getLong(privateDataByte, offset+4+(int)type_id_length,4, Utils.MASK_32BITS);
		profiles = new ArrayList<>((int) taggedProfiles_count);
		int r = 8+(int)type_id_length;
		for (int i = 0; i < taggedProfiles_count; i++) {
			TaggedProfile tp = new TaggedProfile(privateDataByte, offset+r );
			r+=tp.getProfile_data_length()+8;
			profiles.add(tp);

		}

		length=r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */

	public KVP getJTreeNode(int modus) {

		KVP t = new KVP("IOP::IOR");
		t.add(new KVP("type_id_length",type_id_length));
		t.add(new KVP("type_id",type_id ,getTypeIdString(type_id)));
		t.add(new KVP("taggedProfiles_count",taggedProfiles_count));
		addListJTree(t,profiles,modus,"TaggedProfiles");


		return t;
	}

	public static String getTypeIdString(byte[] typeId){
		if(Utils.equals(typeId, 0, typeId.length,TYPE_DIRECTORY,0,TYPE_DIRECTORY.length)){
			return "DSM::Directory";
		}else if(Utils.equals(typeId, 0, typeId.length,TYPE_FILE,0,TYPE_FILE.length)){
			return "DSM::File";
		}else if(Utils.equals(typeId, 0, typeId.length,TYPE_STREAM,0,TYPE_STREAM.length)){
			return "DSM::Stream";
		}else if(Utils.equals(typeId, 0, typeId.length,TYPE_SERVICEGATEWAY,0,TYPE_SERVICEGATEWAY.length)){
			return "DSM::ServiceGateway";
		}else if(Utils.equals(typeId, 0, typeId.length,TYPE_STREAMEVENT,0,TYPE_STREAMEVENT.length)){
			return "DSM::StreamEvent";
		}else{
			return null;
		}

	}

	public long getType_id_length() {
		return type_id_length;
	}

	public byte[] getType_id() {
		return type_id;
	}

	public long getTaggedProfiles_count() {
		return taggedProfiles_count;
	}

	public List<TaggedProfile> getProfiles() {
		return profiles;
	}

	public int getLength() {
		return length;
	}

}
