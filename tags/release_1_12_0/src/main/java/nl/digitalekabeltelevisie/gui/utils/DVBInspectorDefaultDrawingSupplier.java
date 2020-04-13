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

package nl.digitalekabeltelevisie.gui.utils;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.DefaultDrawingSupplier;

/**
 * Extended DefaultDrawingSupplier to use larger shapes
 * @author Eric
 *
 */
public class DVBInspectorDefaultDrawingSupplier extends DefaultDrawingSupplier {

    /** The default shape sequence. */
    public static final Shape[] DVBI_SHAPE_SEQUENCE
            = createDVBISeriesShapes();

    /** The default outline paint sequence. */
    public static final Paint[] DVB_OUTLINE_PAINT_SEQUENCE = new Paint[] {
            Color.black};
    /**
	 *
	 */
	public DVBInspectorDefaultDrawingSupplier() {
        super(DEFAULT_PAINT_SEQUENCE, DEFAULT_FILL_PAINT_SEQUENCE,
        		DVB_OUTLINE_PAINT_SEQUENCE,
                DEFAULT_STROKE_SEQUENCE,
                DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DVBI_SHAPE_SEQUENCE);
	}

	/**
	 * @param paintSequence
	 * @param outlinePaintSequence
	 * @param strokeSequence
	 * @param outlineStrokeSequence
	 * @param shapeSequence
	 */
	public DVBInspectorDefaultDrawingSupplier(Paint[] paintSequence, Paint[] outlinePaintSequence,
			Stroke[] strokeSequence, Stroke[] outlineStrokeSequence, Shape[] shapeSequence) {
		super(paintSequence, outlinePaintSequence, strokeSequence, outlineStrokeSequence, shapeSequence);
	}

	/**
	 * @param paintSequence
	 * @param fillPaintSequence
	 * @param outlinePaintSequence
	 * @param strokeSequence
	 * @param outlineStrokeSequence
	 * @param shapeSequence
	 */
	public DVBInspectorDefaultDrawingSupplier(Paint[] paintSequence, Paint[] fillPaintSequence,
			Paint[] outlinePaintSequence, Stroke[] strokeSequence, Stroke[] outlineStrokeSequence, Shape[] shapeSequence) {
		super(paintSequence, fillPaintSequence, outlinePaintSequence, strokeSequence, outlineStrokeSequence,
				shapeSequence);
		// TODO Auto-generated constructor stub
	}



    /**
     * Creates an array of standard shapes to display for the items in series
     * on charts.
     *
     * @return The array of shapes.
     */
    public static Shape[] createDVBISeriesShapes() {

        Shape[] result = new Shape[10];

        double size = 10.0;
        double delta = size / 2.0;
        int[] xpoints = null;
        int[] ypoints = null;

        // square
        result[0] = new Rectangle2D.Double(-delta, -delta, size, size);
        // circle
        result[1] = new Ellipse2D.Double(-delta, -delta, size, size);

        // up-pointing triangle
        xpoints = new int[] {(int) 0.0, (int) delta, (int) -delta};
        ypoints = new int[] {(int) -delta, (int) delta, (int) delta};
        result[2] = new Polygon(xpoints, ypoints, 3);

        // diamond
        xpoints = new int[] {(int) 0.0, (int) delta, (int) 0.0, (int) -delta};
        ypoints = new int[] {(int) -delta, (int) 0.0, (int) delta, (int) 0.0};
        result[3] = new Polygon(xpoints, ypoints, 4);

        // horizontal rectangle
        result[4] = new Rectangle2D.Double(-delta, -delta / 2, size, size / 2);

        // down-pointing triangle
        xpoints = new int[] {(int) -delta, (int) +delta, (int) 0.0};
        ypoints = new int[] {(int) -delta, (int) -delta, (int) delta};
        result[5] = new Polygon(xpoints, ypoints, 3);

        // horizontal ellipse
        result[6] = new Ellipse2D.Double(-delta, -delta / 2, size, size / 2);

        // right-pointing triangle
        xpoints = new int[] {(int) -delta, (int) delta, (int) -delta};
        ypoints = new int[] {(int) -delta, (int) 0.0, (int) delta};
        result[7] = new Polygon(xpoints, ypoints, 3);

        // vertical rectangle
        result[8] = new Rectangle2D.Double(-delta / 2, -delta, size / 2, size);

        // left-pointing triangle
        xpoints = new int[] {(int) -delta, (int) delta, (int) delta};
        ypoints = new int[] {(int) 0.0, (int) -delta, (int) +delta};
        result[9] = new Polygon(xpoints, ypoints, 3);

        return result;

    }


}
