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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class BIOPDirectoryMessage extends BIOPMessage {

	public static class Binding implements TreeNode{

		private final BIOPName biopName;
		private final int bindingType;
		private final IOR ior;
		private final int objectInfo_length;
		private final byte[] objectInfo_data_byte;

		private int len = 0;


		public Binding(byte[] data, int offset) {
			biopName = new BIOPName(data, offset);
			len=biopName.getLen();

			bindingType  = getInt(data, offset+len, 1, Utils.MASK_8BITS);
			len+=1;
			ior = new IOR(data, offset+len);
			len+=ior.getLength();
			objectInfo_length  = getInt(data, offset+len, 2, Utils.MASK_16BITS);
			len+=2;
			objectInfo_data_byte = copyOfRange(data, offset + len, offset + len + objectInfo_length);
			len+=objectInfo_length;
		}

		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("Binding",biopName.getName());
			t.add(biopName.getJTreeNode(modus));
			t.add(new KVP("bindingType",bindingType ,getBindingTypeString(bindingType)));
			t.add(ior.getJTreeNode(modus));

			return t;
		}



		public int length(){
			return len;
		}

		public BIOPName getBiopName() {
			return biopName;
		}

		public int getBindingType() {
			return bindingType;
		}

		public IOR getIor() {
			return ior;
		}

		public int getObjectInfo_length() {
			return objectInfo_length;
		}

		public byte[] getObjectInfo_data_byte() {
			return objectInfo_data_byte;
		}

		public int getLen() {
			return len;
		}

	}

	public record ServiceContext(long context_id, int context_data_length,
								 byte[] context_data_byte) implements TreeNode {

		public KVP getJTreeNode(int modus) {
				KVP t = new KVP("ServiceContext");
				t.add(new KVP("context_id", context_id));
				t.add(new KVP("context_data_length", context_data_length));
				t.add(new KVP("context_data_byte", context_data_byte));
				return t;
			}

		}

	protected byte[] objectInfo_data_byte;
	protected int serviceContextList_count;
	private final List<ServiceContext> serviceContextList = new ArrayList<>();
	protected long messageBody_length;
	protected int bindings_count;
	private final List<Binding> bindingList = new ArrayList<>();

	public BIOPDirectoryMessage(byte[] data, int offset) {
		super(data, offset);
		objectInfo_data_byte = copyOfRange(data, byte_counter, byte_counter + objectInfo_length);
		byte_counter += objectInfo_length;
		serviceContextList_count =  getInt(data, byte_counter, 1, Utils.MASK_8BITS);
		byte_counter += 1;
		for (int i = 0; i < serviceContextList_count; i++) {
			long context_id = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
			byte_counter += 4;
			int  context_data_length = getInt(data, byte_counter, 2, Utils.MASK_16BITS);
			byte_counter += 2;
			byte[] context_data_byte = copyOfRange(data, byte_counter, byte_counter + context_data_length);
			byte_counter += context_data_length;
			ServiceContext serviceContext = new ServiceContext(context_id, context_data_length, context_data_byte);
			serviceContextList.add(serviceContext);
		}
		messageBody_length = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
		byte_counter += 4;
		bindings_count = getInt(data, byte_counter, 2, Utils.MASK_16BITS);
		byte_counter += 2;
		for (int i = 0; i <bindings_count; i++) {
			Binding binding = new Binding(data,byte_counter);
			bindingList.add(binding);
			byte_counter += binding.length();

		}
	}

	@Override
	public KVP getJTreeNode(int modus, String label) {
		KVP t = super.getJTreeNode(modus,label);
		t.add(new KVP("objectInfo_data_byte",objectInfo_data_byte));
		t.add(new KVP("serviceContextList_count",serviceContextList_count));
		addListJTree(t,serviceContextList,modus,"ServiceContextList");
		t.add(new KVP("messageBody_length",messageBody_length));
		t.add(new KVP("bindings_count",bindings_count));
		addListJTree(t,bindingList,modus,"Bindings");
		return t;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		return getJTreeNode(modus,"");
	}

	public static String getBindingTypeString(int bindingType) {

		if(bindingType==0x1){
			return "nobject";
		}else if(bindingType==0x2){
			return "ncontext";
		}
		return null;
	}


	public byte[] getObjectInfo_data_byte() {
		return objectInfo_data_byte;
	}


	public int getServiceContextList_count() {
		return serviceContextList_count;
	}


	public List<ServiceContext> getServiceContextList() {
		return serviceContextList;
	}


	public long getMessageBody_length() {
		return messageBody_length;
	}


	public int getBindings_count() {
		return bindings_count;
	}


	public List<Binding> getBindingList() {
		return bindingList;
	}
}
