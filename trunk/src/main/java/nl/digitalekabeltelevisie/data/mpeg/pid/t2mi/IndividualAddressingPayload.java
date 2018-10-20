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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.*;

public class IndividualAddressingPayload extends Payload{

	public class IndividualAddressingData implements TreeNode {

		public class Function implements TreeNode {

			private int function_tag;
			private int function_length; // defines the total length of the function() in bytes, including the
											// function_tag, function_length and function_body() fields
			private byte[] body;

			public Function(BitSource bs) {

				function_tag = bs.readBits(8);
				function_length = bs.readBits(8);

				body = bs.readBytes(function_length - 2);
			}

			@Override
			public DefaultMutableTreeNode getJTreeNode(int modus) {
				DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Function"));
				t.add(new DefaultMutableTreeNode(
						new KVP("function_tag", function_tag, function_type_list.get(function_tag))));
				t.add(new DefaultMutableTreeNode(new KVP("function_length", function_length,
						" defines the total length of the function() in bytes, including the function_tag, function_length and function_body() fields")));
				if (body != null) {
					t.add(new DefaultMutableTreeNode(new KVP("body", body, null)));
				}
				return t;
			}

			public void setFunction_tag(int function_tag) {
				this.function_tag = function_tag;
			}

			public void setFunction_length(int function_length) {
				this.function_length = function_length;
			}

			public void setBody(byte[] body) {
				this.body = body;
			}

			public int getFunction_tag() {
				return function_tag;
			}

			public int getFunction_length() {
				return function_length;
			}

			public byte[] getBody() {
				return body;
			}

		}

		private int tx_identifier;
		private int function_loop_length;
		private List<Function> functionList = new ArrayList<>();

		public IndividualAddressingData(BitSource bs) {
			tx_identifier = bs.readBits(16);
			function_loop_length = bs.readBits(8);
			int bytesRead = 0;
			while (bytesRead < function_loop_length) {
				Function f = new Function(bs);
				bytesRead += f.getFunction_length();
				functionList.add(f);
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("IndividualAddressingData"));
			t.add(new DefaultMutableTreeNode(new KVP("tx_identifier", tx_identifier, null)));
			t.add(new DefaultMutableTreeNode(new KVP("function_loop_length", function_loop_length, null)));
			Utils.addListJTree(t, functionList, modus, "Functions");
			return t;
		}

	}

	private static final LookUpList function_type_list = new LookUpList.Builder().add(0x00, "Transmitter time offset")
			.add(0x01, "Transmitter frequency offset").add(0x02, "Transmitter power").add(0x03, "Private data")
			.add(0x04, "Cell id").add(0x05, "Enable").add(0x06, "Bandwidth").add(0x10, "ACE-PAPR")
			.add(0x11, "Transmitter MISO group").add(0x12, "TR-PAPR").add(0x13, "L1-ACE-PAPR")
			.add(0x14, "TX-SIG FEF: Sequence Numbers").add(0x15, "TX-SIG Aux stream: Transmitter ID")
			.add(0x16, "Frequency").build();

	private int individual_addressing_length;

	List<IndividualAddressingData> individualAddressingDataList;

	public IndividualAddressingPayload(byte[] data) {
		super(data);

		individual_addressing_length = getIndividualAddressingLength();

		BitSource bs1 = new BitSource(data, 8, data.length - 4); // crc (4)

		individualAddressingDataList = new ArrayList<>();
		while (bs1.available() > 0) {
			IndividualAddressingData individualAddressingData = new IndividualAddressingData(bs1);
			individualAddressingDataList.add(individualAddressingData);
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));

		payloadNode.add(new DefaultMutableTreeNode(
				new KVP("individual_addressing_length", individual_addressing_length, null)));

		Utils.addListJTree(payloadNode, individualAddressingDataList, modus, "Individual addressing");
		return payloadNode;
	}

	public int getIndividualAddressingLength() {
		return getInt(data, 7, 1, MASK_8BITS);
	}

}
