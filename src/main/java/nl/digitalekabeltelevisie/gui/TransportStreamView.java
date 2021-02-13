/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.gui;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;


public interface TransportStreamView {

	/**
	 * Called on views when transportstream changed, or viewcontext changed. Most of time means need to recalculate view
	 * @param transportStream can be null
	 * @param viewContext
	 */
	void setTransportStream(TransportStream transportStream,  ViewContext viewContext);
	
	/**
	 * To be implemented by views that are depended on Preferences to change their representation.
	 * Like when timestamp format changes from secs to hh:mm:ss, timestamp view needs to repaint.
	 * DVBTree needs to rebuild entire tree.  
	 */
	default void refreshView() {
		
	}
}
