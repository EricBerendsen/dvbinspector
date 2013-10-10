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

package nl.digitalekabeltelevisie.util;

import java.awt.Component;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

import javax.swing.ProgressMonitor;

/**
 *
 * Based on ProgressMonitorInputStream, but that only uses available bytes to makes a guess at progress,
 * And is limited to int (32 bits) size files. We need more!
 *
 * @author Eric
 *
 */
public class ProgressMonitorLargeInputStream  extends FilterInputStream{


    private ProgressMonitor monitor;
    private long nread = 0;
    private long size = 0;
    private long divider = 1;

    /**
     * Constructs an object to monitor the progress of an input stream.
     *
     * @param message Descriptive text to be placed in the dialog box
     *                if one is popped up.
     * @param parentComponent The component triggering the operation
     *                        being monitored.
     * @param in The input stream to be monitored.
     * @param size The size of the stream of which loading to be monitored. (Works only if size know, like for a File)
     */
	public ProgressMonitorLargeInputStream(Component parentComponent,
            Object message,
            InputStream in,
            long size) {
        super(in);
        this.size = size;
        if(size>Integer.MAX_VALUE){
        	divider = (size / Integer.MAX_VALUE) + 1;
        	System.out.println("divider calculated: "+divider+", size: "+size);
        }

        monitor = new ProgressMonitor(parentComponent, message, null, 0, (int)(size / divider));
	}

    /**
     * Get the ProgressMonitor object being used by this stream. Normally
     * this isn't needed unless you want to do something like change the
     * descriptive text partway through reading the file.
     * @return the ProgressMonitor object used by this object
     */
    public ProgressMonitor getProgressMonitor() {
        return monitor;
    }


    /**
     * Overrides <code>FilterInputStream.read</code>
     * to update the progress monitor after the read.
     */
    public int read() throws IOException {
        int c = in.read();

        if (c >= 0){
        	nread+= c;
        	monitor.setProgress(getProgress());
        }
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            //exc.bytesTransferred = -1;
            throw exc;
        }
        return c;
    }

    /**
     * Overrides <code>FilterInputStream.read</code>
     * to update the progress monitor after the read.
     */
    public int read(byte b[]) throws IOException {
        int nr = in.read(b);
        if (nr > 0){
        	nread += nr;
        	monitor.setProgress(getProgress());
        }
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            //exc.bytesTransferred = -1;
            throw exc;
        }
        return nr;
    }

    public int read(byte b[], int off, int len) throws IOException {
    	int nr =  in.read(b, off, len);
        if (nr > 0){
        	nread += nr;
        	monitor.setProgress(getProgress());
        }
        if (monitor.isCanceled()) {
            InterruptedIOException exc =
                                    new InterruptedIOException("progress");
            //exc.bytesTransferred = -1;
            throw exc;
        }
        return nr;
   }


    /**
     * Overrides <code>FilterInputStream.skip</code>
     * to update the progress monitor after the skip.
     */
    public long skip(long n) throws IOException {
        long nr = in.skip(n);
        if (nr > 0){
        	nread += nr;
        	monitor.setProgress(getProgress());
        }
        return nr;
    }

    /**
     * Overrides <code>FilterInputStream.close</code>
     * to close the progress monitor as well as the stream.
     */
    public void close() throws IOException {
        in.close();
        monitor.close();
    }


    /**
     * Overrides <code>FilterInputStream.reset</code>
     * to reset the progress monitor as well as the stream.
     */
    public synchronized void reset() throws IOException {
        in.reset();
        nread = size - in.available();
        monitor.setProgress(getProgress());
    }



	/**
	 * @return
	 */
	private int getProgress() {

		return (int)(nread / divider);
	}


}
