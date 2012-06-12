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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class AudioStreamDescriptor extends Descriptor {

	private int freeFormatFlag;
	private int id;
	private int layer;
	private int variableRateAudioIndicator;
	private int reserved;


	public AudioStreamDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		freeFormatFlag = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		id = Utils.getInt(b, offset+2, 1, 0x40)>>6;
		layer = Utils.getInt(b, offset+2, 1, 0x30)>>4;
		variableRateAudioIndicator = Utils.getInt(b, offset+2, 1, 0x08)>>3;
		reserved = Utils.getInt(b, offset+2, 1, 0x07);


	}

	@Override
	public String toString() {
		return super.toString() + " freeFormatFlag"+freeFormatFlag ;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("free_format_flag",freeFormatFlag ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("ID",id ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("layer",layer ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("variable_rate_audio_indicator",variableRateAudioIndicator ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));


		return t;
	}

	public int getFreeFormatFlag() {
		return freeFormatFlag;
	}


	public void setFreeFormatFlag(final int serviceType) {
		this.freeFormatFlag = serviceType;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(final int l) {
		this.layer = l;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public int getVariableRateAudioIndicator() {
		return variableRateAudioIndicator;
	}

	public void setVariableRateAudioIndicator(final int va) {
		this.variableRateAudioIndicator = va;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(final int reserved) {
		this.reserved = reserved;
	}

}
