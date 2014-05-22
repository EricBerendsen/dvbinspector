package nl.digitalekabeltelevisie.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtendedEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ShortEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.EIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDT;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Sample program to extract and print the EIT information
 * @author Eric
 *
 */
public class ExtractEIT {

	/**
	 * @param args
	 */
	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		TransportStream transportStream = null;;
		try {
			transportStream = new TransportStream("d:\\ts\\Ziggo Oost 369000 11-21 15-52-01.ts");
		   // parse general (P)SI information
		   transportStream.parseStream();
		} catch (final Exception e) {
		   e.printStackTrace();
		}



		final PSI psi = transportStream.getPsi();

		final SDT sdt = psi.getSdt();
		final EIT eit = psi.getEit();

		final Map<Integer, HashMap<Integer, EITsection[]>> sections = eit.getEITsectionsMap();

		final TreeSet<Integer> tableSet = new TreeSet<Integer>(sections.keySet());


		// iterate over the different types of EIT info (now/next actual stream, now/next other stream, schedule current stream,
		//  schedule other stream).

		for(final Integer tableID : tableSet ){

			System.out.println("");
			System.out.println("TableID="+tableID +": ("+TableSection.getTableType(tableID)+")");
			System.out.println("");
			final HashMap<Integer, EITsection []> table= sections.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<Integer>(table.keySet());
			// iterate over the services in this table
			for(final Integer serviceNo : serviceSet){
				System.out.println("service_id:"+serviceNo+": ("+ sdt.getServiceName(serviceNo)+")");
				System.out.println("");

				// data for this service may be in  multiple sections
				for(final EITsection section :sections.get(tableID).get(serviceNo)){
					if(section!=null){
						final List<Event> eventList = section.getEventList();
						// all events in this section
						for(final Event event:eventList){
							System.out.print("Start: "+Utils.getUTCFormattedString(event.getStartTime()));
							System.out.print(" length: "+ event.getDuration()+" ");
							final List<Descriptor> descList = event.getDescriptorList();
							final List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(descList, ShortEventDescriptor.class);
							if(shortDesc.size()>0){
								final ShortEventDescriptor shortEventDescriptor = shortDesc.get(0);
								System.out.print(" Name: "+ shortEventDescriptor.getEventName().toString());
								System.out.print(" Description: "+shortEventDescriptor.getText().toString()+" ");
							}
							final List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(descList, ExtendedEventDescriptor.class);
							for(final ExtendedEventDescriptor extEvent: extendedDesc){ // no check whether we have all extended event descriptors
								System.out.print(extEvent.getText().toString());
							}

							System.out.println();
						}
					}
				}
				System.out.println("");
			}
		}
	}

}
