package nl.digitalekabeltelevisie.util.CLI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import nl.digitalekabeltelevisie.util.CLI.beans.*;

/**
 * Write TransportStream data to a TsData object
 */
public class DisplayData {

	/**
	 * Display all available information in a bash
	 * @param transportStream - Data to be completed
	 */
	public void displayAllInformation(final TsData transportStream) {

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		// General information
		tsDisplay(transportStream, objectMapper);
		
		// PSI information
		psiDisplay(transportStream, objectMapper);

		// PIDs information
		pidsDisplay(transportStream, objectMapper);

	}

	/**
	 * Display on bash Transport Stream basic information
	 * @param tsData - data parsed from input ts file
	 * @param objectMapper
	 */
	private void tsDisplay(final TsData tsData, final ObjectMapper objectMapper) {

		try {
			System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tsData.getTs()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Display on bash PIDs information
	 * @param tsData - data parsed from input ts file
	 * @param objectMapper
	 */
	private void pidsDisplay(final TsData tsData, final ObjectMapper objectMapper) {

		try {
			System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tsData.getPids()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Display on bash PSI information
	 * @param tsData - data parsed from input ts file
	 * @param objectMapper
	 */
	private void psiDisplay(final TsData tsData, final ObjectMapper objectMapper) {

		try {
			System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(tsData.getPsi()));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
	}
}
