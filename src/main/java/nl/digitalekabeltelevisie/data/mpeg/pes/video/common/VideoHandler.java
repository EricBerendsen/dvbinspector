package nl.digitalekabeltelevisie.data.mpeg.pes.video.common;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

public abstract class VideoHandler extends GeneralPesHandler {
	
	protected Cea608 cea608 = new Cea608();


	public VideoHandler() {
		super();
	}
	
	/**
	 * @param modus
	 * @param s
	 */
	protected void addCCDataToTree(final int modus, final KVP s) {
		cea608.addCCDataToTree(modus, s);
	}
	
	
	
	@Override
	public void postProcess() {
		// collect sei_messages UserDataRegisteredItuT35Sei_message GA94 cc data Line 21  
		collectCEA708Data();
		cea608.handleXDSData();
	}

	


	protected abstract void collectCEA708Data() ;

	/**
	 * @param pts
	 * @param auxData
	 */
	protected void find708AuxData(long pts, AuxiliaryData auxData) {
		cea608.find708AuxData(pts, auxData);
	}

	/**
	 * @param num_units_in_tick
	 * @param time_scale
	 * @return
	 */
	public static String getClockTickString(final long num_units_in_tick, final long time_scale) {
		return String.format("clock tick:  %4.2f  seconds, framerate: %4.2f fps", 
				(double) num_units_in_tick / (double) time_scale,
				(double)time_scale  / (double) num_units_in_tick);
	}


}