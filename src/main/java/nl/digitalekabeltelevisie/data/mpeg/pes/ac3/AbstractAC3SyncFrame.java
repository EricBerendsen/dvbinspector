package nl.digitalekabeltelevisie.data.mpeg.pes.ac3;

import nl.digitalekabeltelevisie.util.BitSource;


public class AbstractAC3SyncFrame {

	public static String getACModString(final int acmod) {
		switch (acmod) {
		case 0:
			return "1 + 1";
		case 1:
			return "1/0";
		case 2:
			return "2/0";
		case 3:
			return "3/0";
		case 4:
			return "2/1";
		case 5:
			return "3/1";
		case 6:
			return "2/2";
		case 7:
			return "3/2";
		default:
			return "Illegal Value for acmod: "+acmod;
		}
	}

	protected int syncword;
	protected int fscod;
	protected int bsid;
	protected int lfeon;
	protected int dialnorm;
	protected int compre;
	protected int compr;
	protected int audprodie;
	protected int mixlevel;
	protected int roomtyp;
	protected int dialnorm2;
	protected int compr2e;
	protected int audprodi2e;
	protected int mixlevel2;
	protected int roomtyp2;
	protected BitSource bs = null;
	protected int offset;
	protected int dmixmod;
	protected int compr2;
	protected int ltrtcmixlev;
	protected int ltrtsurmixlev;
	protected int lorocmixlev;
	protected int lorosurmixlev;
	protected int bsmod;
	protected int dsurmod;
	protected int copyrightb;
	protected int origbs;
	protected int dheadphonmod;

	public AbstractAC3SyncFrame(final byte[] data, final int offset) {
		super();
		this.offset = offset;
		bs = new BitSource(data, offset);
	}

	public static String getBsidString(final int bsid) {
		String t="";
		if(bsid==16){
			t = "E-AC-3";
		}else if(bsid==6){
			t = "AC3 Alternate bit stream syntax";
		}else if(bsid==8){
			t = "AC3 normal syntax";
		}else if(bsid<8){
			t = "AC3 backward compatible";
		}else if((bsid==9)||(bsid==10)){
			t = "AC3 newer version";
		}else if((bsid>10)||(bsid<16)){
			t = "E-AC-3 backward compatible";
		}else{
			t = "E-AC-3 newer version";
		}
		return "Bit stream identification "+t;
	}

	/**
	 * @return
	 */
	protected static String getLfeOnString(int lfeon) {
		return (lfeon==1)?"Low frequency effects channel is on":"Low frequency effects channel is off";
	}

	/**
	 * @return
	 */
	protected static String getDialNormString(int dialnorm) {
		return "Dialogue normalization: "+((dialnorm==0)?"Reserved":"-"+dialnorm+" dB");
	}

	/**
	 * @return
	 */
	protected static String getCompreString(int compre) {
		return compre==1?"Compression gain word exists":"No compression gain word exists";
	}

	public static String getSampleRateCodeString(final int fscod) {
		switch (fscod) {
		case 0:
			return "48 kHz";
		case 1:
			return "44,1 kHz";
		case 2:
			return "32 kHz";
		case 3:
			return "Reserved";

		default:
			return "illegal value";
		}
	}

	public static String getDmixmodString(final int dmixmod2) {
		switch (dmixmod2) {

		case 0:
			return "Not indicated";
		case 1:
			return "Lt/Rt downmix preferred";
		case 2:
			return "Lo/Ro downmix preferred";
		case 3:
			return "Reserved";

		default:
			return "Illegal value";
		}
	}

	public int getCompre() {
		return compre;
	}

	public int getCompr() {
		return compr;
	}

	public int getCompr2e() {
		return compr2e;
	}

	public int getCompr2() {
		return compr2;
	}

	public int getLtrtcmixlev() {
		return ltrtcmixlev;
	}

	public int getLtrtsurmixlev() {
		return ltrtsurmixlev;
	}

	public int getLorocmixlev() {
		return lorocmixlev;
	}

	public int getLorosurmixlev() {
		return lorosurmixlev;
	}

	public int getBsmod() {
		return bsmod;
	}

	public int getDsurmod() {
		return dsurmod;
	}

	public int getOrigbs() {
		return origbs;
	}

}