/**
 *
 * http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 * This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 *
 * This file is part of DVB Inspector.
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

package nl.digitalekabeltelevisie.controller;

import java.awt.Paint;

/**
 * @author Eric Berendsen
 *
 *         Used as a label for the Charts as produced by jfreechart..
 *
 */

@SuppressWarnings("rawtypes")
public class ChartLabel implements Comparable {

	private String label;
	private short pid;
	private Paint color;

	public ChartLabel(final String label, final short pid, final Paint color) {
		super();
		this.label = label;
		this.pid = pid;
		this.color = color;
	}

	public ChartLabel(final String label, final short pid) {
		this(label, pid, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Object o) {
		if (o == null) {
			throw new NullPointerException(); // NOPMD by Eric on 23-8-14 12:31
		}
		if (!(o instanceof ChartLabel)) {
			throw new ClassCastException();
		}

		return Short.valueOf(this.pid).compareTo(((ChartLabel) o).getPid());
	}

	@Override
	public String toString() {
		return label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public short getPid() {
		return pid;
	}

	public void setPid(final short pid) {
		this.pid = pid;
	}

	/**
	 * @return the color
	 */
	public Paint getColor() {
		return color;
	}

	/**
	 * @param color the color to set
	 */
	public void setColor(final Paint color) {
		this.color = color;
	}

}
