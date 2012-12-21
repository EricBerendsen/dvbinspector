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

package nl.digitalekabeltelevisie.data.mpeg;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCCs;
import nl.digitalekabeltelevisie.data.mpeg.psi.AITs;
import nl.digitalekabeltelevisie.data.mpeg.psi.BAT;
import nl.digitalekabeltelevisie.data.mpeg.psi.CAT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.INT;
import nl.digitalekabeltelevisie.data.mpeg.psi.NIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.NetworkSync;
import nl.digitalekabeltelevisie.data.mpeg.psi.PAT;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTs;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDT;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDT;
import nl.digitalekabeltelevisie.data.mpeg.psi.TOT;
import nl.digitalekabeltelevisie.data.mpeg.psi.UNTs;

/**
 * Container for all PSI related data
 * 
 * @author Eric Berendsen
 * 
 */
public class PSI {


	private final PAT pat = new PAT(this);
	private final CAT cat = new CAT(this);
	private final BAT bat = new BAT(this);
	private final NIT nit = new NIT(this);
	private final SDT sdt = new SDT(this);
	private final PMTs pmts = new PMTs(this);
	private final EIT eit = new EIT(this);
	private final TDT tdt = new TDT(this);
	private final TOT tot = new TOT(this);
	private final NetworkSync networkSync = new NetworkSync(this);
	private final INT int_table = new INT(this);
	private final UNTs unt_table = new UNTs(this);
	private final AITs ait_table = new AITs(this);
	private final DSMCCs dsm_table = new DSMCCs(this);

	public NIT getNit() {
		return nit;
	}
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PSI"));
		t.add(pat.getJTreeNode(modus));
		t.add(cat.getJTreeNode(modus));
		t.add(bat.getJTreeNode(modus));
		t.add(pmts.getJTreeNode(modus));
		t.add(nit.getJTreeNode(modus));
		t.add(sdt.getJTreeNode(modus));
		t.add(eit.getJTreeNode(modus));
		t.add(tdt.getJTreeNode(modus));
		t.add(tot.getJTreeNode(modus));
		t.add(networkSync.getJTreeNode(modus));
		t.add(int_table.getJTreeNode(modus));
		t.add(unt_table.getJTreeNode(modus));
		t.add(ait_table.getJTreeNode(modus));
		t.add(dsm_table.getJTreeNode(modus));
		return t;
	}
	public PMTs getPmts() {
		return pmts;
	}

	public PAT getPat() {
		return pat;
	}
	public SDT getSdt() {
		return sdt;
	}
	public CAT getCat() {
		return cat;
	}
	public TOT getTot() {
		return tot;
	}
	public TDT getTdt() {
		return tdt;
	}
	public BAT getBat() {
		return bat;
	}
	public EIT getEit() {
		return eit;
	}
	public INT getInt() {
		return int_table;
	}
	public UNTs getUnts() {
		return unt_table;
	}
	public AITs getAits() {
		return ait_table;
	}
	public DSMCCs getDsms() {
		return dsm_table;
	}
	public NetworkSync getNetworkSync() {
		return networkSync;
	}

}
