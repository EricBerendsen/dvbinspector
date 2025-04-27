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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class ApplicationNameDescriptor extends AITDescriptor {
	private List<ApplicationName> applicationNames= new ArrayList<>();


	public List<ApplicationName> getApplicationNames() {
		return applicationNames;
	}

	public record ApplicationName(String iso639LanguageCode, DVBString application_name) implements TreeNode{
		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("application_name: " + application_name);
			s.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			s.add(new KVP("application_name", application_name));
			return s;
		}
	}

	public ApplicationNameDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			String languageCode = getISO8859_1String(b, t + 2, 3);
			DVBString application_name = new DVBString(b, t + 5);
			ApplicationName applicationName = new ApplicationName(languageCode, application_name);
			applicationNames.add(applicationName);
			t += 4 + application_name.getLength();
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		addListJTree(t,applicationNames,modus,"application_names");
		return t;
	}
}
