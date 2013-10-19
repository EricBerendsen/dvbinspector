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

package nl.digitalekabeltelevisie.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * @author Eric
 *
 *Based on http://stackoverflow.com/questions/240294/given-a-java-inputstream-how-can-i-determine-the-current-offset-in-the-stream
 */
public class PositionPushbackInputStream extends PushbackInputStream {
    private long pos = 0;
    private long mark = 0;


	/**
	 * @param in
	 */
	public PositionPushbackInputStream(InputStream in) {
		super(in);
	}

	public PositionPushbackInputStream(InputStream in, int size) {
		super(in, size);
	}

	/**
	   * <p>Get the stream position.</p>
	   *
	   * <p>Eventually, the position will roll over to a negative number.
	   * Reading 1 Tb per second, this would occur after approximately three
	   * months. Applications should account for this possibility in their
	   * design.</p>
	   *
	   * @return the current stream position.
	   */
	  public long getPosition()
	  {
	    return pos;
	  }

	  @Override
	  public int read()
	    throws IOException
	  {
	    int b = super.read();
	    if (b >= 0) {
			pos += 1;
		}
	    return b;
	  }
	  @Override
	  public long skip(long skip)
	    throws IOException
	  {
	    long n = super.skip(skip);
	    if (n > 0) {
			pos += n;
		}
	    return n;
	  }

	  @Override
	  public void mark(int readlimit)
	  {
	    super.mark(readlimit);
	    mark = pos;
	  }

	  @Override
	  public synchronized void reset()
	    throws IOException
	  {
	    /* A call to reset can still succeed if mark is not supported, but the
	     * resulting stream position is undefined, so it's not allowed here. */
	    if (!markSupported()) {
			throw new IOException("Mark not supported.");
		}
	    super.reset();
	    pos = mark;
	  }

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
	    int n = super.read(b, off, len);
	    if (n > 0) {
			pos += n;
		}
	    return n;

	}

	@Override
	public void unread(int b) throws IOException {
		super.unread(b);
		pos -= 1;
	}

	@Override
	public void unread(byte[] b, int off, int len) throws IOException {
		super.unread(b, off, len);
		pos -= len;
	}

	@Override
	public void unread(byte[] b) throws IOException {
		super.unread(b);
		pos -= b.length;


	}

	@Override
	public int read(byte[] b) throws IOException {

	    int n = super.read(b);
	    if (n > 0) {
			pos += n;
		}
	    return n;


	}
}
