/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  (C) RIEDEL Communications Canada, Inc. All rights reserved
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

package nl.digitalekabeltelevisie.data.mpeg.pes.headers;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Generic interface used to parse an elementary stream's header. The elementary
 * stream header, if any, follows directly the PES header.
 *
 * @author Simon Provost
 */
public interface ElementaryStreamHeader {
    /**
     * Parse an elementary stream header from a byte buffer.
     *
     * @param buffer The buffer containing the header's information
     * @param offset Offset, within the buffer, at which the header starts
     */
    void parse(byte[] buffer, int offset);

    /**
     * @return A {@link  DefaultMutableTreeNode} representing this header. May return
     * null if there is an error (e.g. parsing error, buffer does not represent an
     * actual header, header type not handled by the code)
     */
    DefaultMutableTreeNode getJTreeNode();

    /**
     * @return The length, in bytes, of the header. Does NOT include the
     * offset (if any), passed to the method {@link #parse(byte[], int)}.
     */
    int getLength();
}
