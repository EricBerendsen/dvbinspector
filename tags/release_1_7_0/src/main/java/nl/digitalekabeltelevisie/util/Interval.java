package nl.digitalekabeltelevisie.util;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2013 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.Date;

/**
 * Represents a time interval (period of time).
 * @author Eric
 *
 */
public class Interval {

	private Date start;
	private Date end;

	/**
	 * @param start
	 * @param end
	 */
	public Interval(Date start, Date end) {
		super();
		this.start = start;
		this.end = end;
	}

	/**
	 * @param start
	 * @param duration milliseconds
	 */
	public Interval(Date start, long duration) {
		super();
		this.start = start;
		this.end = new Date(start.getTime()+duration);
	}

	/**
	 * @return start
	 */
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	/**
	 * @return length of interval in milliseconds
	 */
	public long getDuration(){
		return end.getTime() - start.getTime();
	}

}
