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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

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


		public Binding(final byte[] data, final int offset) {
			biopName = new BIOPName(data, offset);
			len=biopName.getLen();

			bindingType  = Utils.getInt(data, offset+len, 1, Utils.MASK_8BITS);
			len+=1;
			ior = new IOR(data, offset+len);
			len+=ior.getLength();
			objectInfo_length  = Utils.getInt(data, offset+len, 2, Utils.MASK_16BITS);
			len+=2;
			objectInfo_data_byte = Utils.copyOfRange(data,offset+len,offset+len+objectInfo_length);
			len+=objectInfo_length;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Binding",biopName.getName(),null));
			t.add(biopName.getJTreeNode(modus));
			t.add(new DefaultMutableTreeNode(new KVP("bindingType",bindingType ,getBindingTypeString(bindingType))));
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

	public static class ServiceContext implements TreeNode{

		/**
		 * @param context_id
		 * @param context_data_length
		 * @param context_data_byte
		 */
		public ServiceContext(final long context_id, final int context_data_length,
				final byte[] context_data_byte) {
			super();
			this.context_id = context_id;
			this.context_data_length = context_data_length;
			this.context_data_byte = context_data_byte;
		}

		private final long context_id;
		private final int context_data_length;
		private final byte[] context_data_byte;

		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ServiceContext"));
			t.add(new DefaultMutableTreeNode(new KVP("context_id",context_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("context_data_length",context_data_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("context_data_byte",context_data_byte,null)));
			return t;
		}

	}

	protected byte[] objectInfo_data_byte;
	protected int serviceContextList_count;
	private final List<ServiceContext> serviceContextList = new ArrayList<ServiceContext>();
	protected long messageBody_length;
	protected int bindings_count;
	private final List<Binding> bindingList = new ArrayList<Binding>();




	public BIOPDirectoryMessage(final byte[] data, final int offset) {
		super(data, offset);
		objectInfo_data_byte = Utils.copyOfRange(data,byte_counter,byte_counter+objectInfo_length);
		byte_counter += objectInfo_length;
		serviceContextList_count =  Utils.getInt(data, byte_counter, 1, Utils.MASK_8BITS);
		byte_counter += 1;
		for (int i = 0; i < serviceContextList_count; i++) {
			final long context_id = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
			byte_counter += 4;
			final int  context_data_length = Utils.getInt(data, byte_counter, 2, Utils.MASK_16BITS);
			byte_counter += 2;
			final byte[] context_data_byte = Utils.copyOfRange(data,byte_counter,byte_counter+context_data_length);
			byte_counter += context_data_length;
			final ServiceContext serviceContext = new ServiceContext(context_id, context_data_length, context_data_byte);
			serviceContextList.add(serviceContext);
		}
		messageBody_length = Utils.getLong(data, byte_counter, 4, Utils.MASK_32BITS);
		byte_counter += 4;
		bindings_count = Utils.getInt(data, byte_counter, 2, Utils.MASK_16BITS);
		byte_counter += 2;
		for (int i = 0; i <bindings_count; i++) {
			final Binding binding = new Binding(data,byte_counter);
			bindingList.add(binding);
			byte_counter += binding.length();

		}


	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus, final String label) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus,label);
		t.add(new DefaultMutableTreeNode(new KVP("objectInfo_data_byte",objectInfo_data_byte ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("serviceContextList_count",serviceContextList_count ,null)));
		addListJTree(t,serviceContextList,modus,"ServiceContextList");
		t.add(new DefaultMutableTreeNode(new KVP("messageBody_length",messageBody_length ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("bindings_count",bindings_count ,null)));
		addListJTree(t,bindingList,modus,"Bindings");
		return t;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		return getJTreeNode(modus,"");
	}

	public static String getBindingTypeString(final int bindingType) {

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
