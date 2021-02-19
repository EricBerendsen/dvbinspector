package nl.digitalekabeltelevisie.util.tablemodel.cellrenderer;

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


import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import nl.digitalekabeltelevisie.util.Utils;

public class StreamTypeTableCellRenderer extends DefaultTableCellRenderer {
	String toolTip;
	
	public StreamTypeTableCellRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	
    @Override
	public String getToolTipText() {
        return toolTip;
    }

    
    @Override
	protected void setValue(Object value) {
    	
    	super.setValue(value);
    	if((value instanceof Integer)) {
    		int streamType = (Integer) value;
    		toolTip = Utils.getStreamTypeShortString(streamType);
    	}else {
    		toolTip = null;
    	}
    }

}
