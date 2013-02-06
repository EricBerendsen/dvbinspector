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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class ApplicationNameDescriptor extends AITDescriptor {
	private List<ApplicationName> applicationNames= new ArrayList<ApplicationName>();


	public List<ApplicationName> getApplicationNames() {
		return applicationNames;
	}

	public static class ApplicationName implements TreeNode{
		/**
		 *
		 */
		private String iso639LanguageCode;
		/**
		 *
		 */
		private final int application_name_length;
		private final DVBString application_name;


		public ApplicationName(final String lCode, final int application_name_length, final DVBString application_name){
			iso639LanguageCode = lCode;
			this.application_name_length = application_name_length;
			this.application_name = application_name;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("application_name: "+application_name));
			s.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("application_name_length",application_name_length,null)));
			s.add(new DefaultMutableTreeNode(new KVP("application_name_encoding",application_name.getEncodingString(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("application_name",application_name,null)));
			return s;
		}



		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}


		public void setIso639LanguageCode(final String iso639LanguageCode) {
			this.iso639LanguageCode = iso639LanguageCode;
		}

		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode+"', application_name:"+application_name;
		}


		public int getApplication_name_length() {
			return application_name_length;
		}


		public DVBString getApplication_name() {
			return application_name;
		}


	}

	public ApplicationNameDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String languageCode=getISO8859_1String(b, offset+t+2, 3);

			final int application_name_length = getInt(b, offset+t+5, 1, MASK_8BITS);
			final DVBString application_name =new DVBString(b, offset+t+5);

			final ApplicationName s = new ApplicationName(languageCode, application_name_length,application_name);
			applicationNames.add(s);
			t+=4+application_name_length;
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,applicationNames,modus,"application_names");
		return t;
	}
}
