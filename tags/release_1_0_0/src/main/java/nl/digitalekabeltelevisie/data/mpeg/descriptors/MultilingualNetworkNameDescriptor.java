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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class MultilingualNetworkNameDescriptor extends Descriptor {

	private final List<NetworkName> networkNameList = new ArrayList<NetworkName>();


	public static class NetworkName implements TreeNode{
		/**
		 *
		 */
		private final String iso639LanguageCode;
		private final DVBString network_name ;




		/**
		 * @param languageCode
		 * @param service_provider_name2
		 * @param service_name2
		 */
		public NetworkName(String languageCode, DVBString service_name2) {
			iso639LanguageCode = languageCode;
			network_name = service_name2;
		}



		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("network_name"));
			s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("network_name_encoding",network_name.getEncodingString(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("network_name_length",network_name.getLength(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("network_name",network_name,null)));
			return s;
		}



		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode;
		}


		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}





	}

	public MultilingualNetworkNameDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=offset+2;
		while (t<(descriptorLength+offset+2)) {
			final String languageCode=Utils.getISO8859_1String(b, t, 3);
			int network_name_length = Utils.getInt(b, t+3, 1, Utils.MASK_8BITS);;
			DVBString network_name = new DVBString(b, t+3);
			final NetworkName s = new NetworkName(languageCode, network_name);
			networkNameList.add(s);
			t+=4+network_name_length;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (NetworkName serviceName : networkNameList) {
			buf.append(serviceName.toString());
		}


		return buf.toString();
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		Utils.addListJTree(t,networkNameList,modus,"network_name_list");
		return t;
	}

	public List<NetworkName> getSubtitleList() {
		return networkNameList;
	}
}
