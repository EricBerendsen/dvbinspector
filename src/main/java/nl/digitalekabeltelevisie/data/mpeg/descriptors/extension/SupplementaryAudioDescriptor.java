/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class SupplementaryAudioDescriptor extends ExtensionDescriptor {

	private static Logger logger = Logger.getLogger(SupplementaryAudioDescriptor.class.getName());

	//Supplementary audio descriptor
	private int mix_type;
	private int editorial_classification;
	private int language_code_present;
	private String iso639LanguageCode;
	private byte[] private_data_byte;



	public SupplementaryAudioDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		mix_type = getInt(b, offset+3, 1, 0x80)>>7;
		editorial_classification = getInt(b, offset+3, 1, 0x7c)>>2;
		language_code_present = getInt(b, offset+3, 1, MASK_1BIT);
		int t=4;
		if(language_code_present==1){
			iso639LanguageCode = getISO8859_1String(b, offset+4, 3);
			t=7;
		}
		private_data_byte = Utils.copyOfRange(b, offset+t,offset+descriptorLength+2);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("mix_type",mix_type,(mix_type==0?"Audio stream is a supplementary stream":"Audio stream is a complete and independent stream"))));
		t.add(new DefaultMutableTreeNode(new KVP("editorial_classification",editorial_classification,getEditorialClassificationString(editorial_classification))));
		t.add(new DefaultMutableTreeNode(new KVP("language_code_present",language_code_present,null)));
		if(language_code_present==1){
			t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code",iso639LanguageCode,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("private_data_byte",private_data_byte,null)));

		return t;
	}

	public static String getEditorialClassificationString(final int editorial_classification) {
		switch (editorial_classification) {

		case 0x0:
			return "Main audio";
		case 0x1:
			return "Audio description for the visually impaired";
		case 0x2:
			return "Clean audio for the hearing impaired";
		case 0x3:
			return "Spoken subtitles for the visually impaired";

		default:
			return "reserved for future use";

		}
	}



}
