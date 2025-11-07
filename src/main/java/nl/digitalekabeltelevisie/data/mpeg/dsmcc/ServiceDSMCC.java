/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.getHTMLHexview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.swing.JMenuItem;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.CarouselIdentifierDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc.CompressedModuleDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.BIOPDirectoryMessage.Binding;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.BIOPStreamEventMessage.EventName;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.DSMCC_UNMessageSection.ModuleInfo;
import nl.digitalekabeltelevisie.gui.*;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Represents the object carousel(s) and DSM-CC pids associated with one service
 * Could have multiple carousels per service, sharing DSMCC's
 * contains map of all DSMCC's (one for each PID) containing stream_type: 0xB (11) => ISO/IEC 13818-6 type B
 * @author Eric
 *
 */
public class ServiceDSMCC implements TreeNode {

	private static final Logger logger = Logger.getLogger(ServiceDSMCC.class.getName());

	public class DSMFile implements HTMLSource, SaveAble{

		private BIOPMessage biopMessage;
		private String label;

		public DSMFile(BIOPMessage biopMessage, String label) {
			this.biopMessage = biopMessage;
			this.label = label;
		}

		public BIOPMessage getBiopMessage() {
			return biopMessage;
		}

		public void setBiopMessage(BIOPFileMessage biopMessage) {
			this.biopMessage = biopMessage;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}



		public String getHTML() {
			StringBuilder b = new StringBuilder();
            switch (biopMessage) {
                case BIOPFileMessage fileMes -> {
                    b.append("Type: File<br>Size:");
                    b.append(fileMes.getContent_length());
                    b.append("<br>Data:<br>");
                    b.append(getHTMLHexview(fileMes.getData(), fileMes.getContentStartOffset(), (int) fileMes.getContent_length()));
                }
                case BIOPDirectoryMessage dirMes -> {
                    b.append("Type: Directory<br>Descendants:<br>");
                    for (Binding binding : dirMes.getBindingList()) {
                        b.append(binding.getBiopName().getName()).append(": (").append(BIOPDirectoryMessage.getBindingTypeString(binding.getBindingType())).append(")<br>");
                    }
                }
                case BIOPStreamEventMessage eventMsg -> {
                    b.append("Type: StreamEvent<br>EventNames:<br>");
                    for (EventName eventName : eventMsg.getEventNames()) {
                        b.append(new String(eventName.eventName_data_byte())).append("<br>");

                    }
                }
                case null, default -> {
                }
            }

			return b.toString();
		}
		
		@Override
		public void save(File file) {
			saveFile(file, biopMessage);
		}

		public void saveFile(File file, BIOPMessage biopMessage) {
			logger.info("saveFile called for file path:"+file.getAbsolutePath()+", name:"+file.getName());
			if(biopMessage instanceof BIOPFileMessage biopFile){
				logger.info("starting write file");
				try (FileOutputStream out = new FileOutputStream(file)){

                    out.write(biopFile.getData(),biopFile.getContentStartOffset(),(int)biopFile.getContent_length());

				} catch (IOException e) {
					logger.log(Level.WARNING,"could not write file",e);
				}
			}else if(biopMessage instanceof BIOPDirectoryMessage dirMes){
				//recursively save tree..
				// file now contains the dir to start from.
				// append label first

                logger.log(Level.INFO,"trying to create directory "+ file);
				boolean success = file.mkdir();
				logger.log(Level.INFO,"trying to create directory. result "+ success);
				for(Binding binding:dirMes.getBindingList()){
					String descName=binding.getBiopName().getName();
					IOR ior = binding.getIor();
					BIOPMessage child = getBIOPMessage(ior);
					if((child!=null)&&(descName!=null)){
						logger.log(Level.INFO,"now going down to save:"+descName);
						saveFile(new File(file,descName),child);
					}
				}
			}
		}
	}


	public class ObjectCarousel implements TreeNode{

		public ObjectCarousel(int associationTag, int dataBroadCastId) {
            this.associationTag = associationTag;
			this.dataBroadCastId = dataBroadCastId;
		}


		private final int associationTag;
		private final int dataBroadCastId;
		private CarouselIdentifierDescriptor carouselIdentifierDesc;

		// recursive find all biopMessagases, and add as flat list
		private  void addToTree(BIOPMessage biopMessage, String label, KVP s, int modus){
			if(biopMessage!=null){
				if(biopMessage instanceof BIOPDirectoryMessage dir){
                    s.add(dir.getJTreeNode(modus,label));
					List<Binding> bindings = dir.getBindingList();
					for(Binding binding:bindings){
						BIOPName biopName = binding.getBiopName();
						IOR ior = binding.getIor();
						BIOPMessage child = getBIOPMessage(ior);
						if((child!=null)&&(biopName!=null)){
							addToTree(child, label+"/"+biopName.getName(), s, modus);
						}
					}
				}else{
					s.add(biopMessage.getJTreeNode(modus,label));
				}
			}
		}

		// recursive find all biopMessagases, and build treeview
		private  KVP buildTree(BIOPMessage biopMessage, String label, int modus){
			KVP treeNode = new KVP(label);
			if(biopMessage instanceof BIOPDirectoryMessage dir){
                DSMFile dsmFile = new DSMFile(dir,label);
				treeNode.addHTMLSource(dsmFile,"directory_message");

				JMenuItem objectMenu = new JMenuItem("Export (sub)tree...");
				objectMenu.setActionCommand(DVBtree.EXPORT);
				treeNode.setSubMenuAndOwner(objectMenu,dsmFile);
				List<Binding> bindings = dir.getBindingList();
				for(Binding binding:bindings){
					BIOPName biopName = binding.getBiopName();
					IOR ior = binding.getIor();
					BIOPMessage child = getBIOPMessage(ior);
					if((child!=null)&&(biopName!=null)){
						treeNode.add(buildTree(child, biopName.getName(), modus));
					}
				}
			}else if(biopMessage instanceof BIOPFileMessage){
				JMenuItem objectMenu = new JMenuItem("Save as...");
				objectMenu.setActionCommand(DVBtree.SAVE);
				DSMFile dsmFile = new DSMFile(biopMessage,label);
				treeNode.addHTMLSource(dsmFile,"file_message");

				treeNode.setSubMenuAndOwner(objectMenu,dsmFile);

			}else if(biopMessage instanceof BIOPStreamEventMessage){
				DSMFile dsmFile = new DSMFile(biopMessage,label);
				treeNode.addHTMLSource(dsmFile,"stream_event_message");
			}
			return treeNode;
		}


		public KVP getJTreeNode(int modus){
			KVP s=new KVP("Object Carousel");
			s.add(new KVP("associationTag",associationTag));
			s.add(new KVP("dataBroadCastId",dataBroadCastId,Utils.getDataBroadCastIDString(dataBroadCastId)));
			if(carouselIdentifierDesc!=null){
				s.add(carouselIdentifierDesc.getJTreeNode(modus));
			}
			DSMCC_UNMessageSection dsi = dsmccs.get(associationTag).getDSI();
			if((carouselIdentifierDesc!=null)&&(carouselIdentifierDesc.getFormatId()==0x01)){  //enhanced boot
				// module must be in this PID
				BIOPMessage biopMessage = getBIOPMessage(carouselIdentifierDesc, associationTag);
				addTreeDetails(modus, s, biopMessage);
			}else if(dsi!=null){
				IOR ior = dsi.getServiceGatewayIOR();
				BIOPMessage biopMessage = getBIOPMessage(ior);
				addTreeDetails(modus, s, biopMessage);
			}// no DSI, no starting point....

			return s;
		}

		public void addTreeDetails(int modus, KVP s,
                                   BIOPMessage biopMessage) {
			KVP messages = new KVP("Messages");
			addToTree(biopMessage, "", messages, modus);
			s.add(messages);
			if(biopMessage!=null){
				s.add(buildTree(biopMessage, "ObjectHierarchy", modus));
			}
		}

		public void setCarouselIdentifierDescriptor(
				CarouselIdentifierDescriptor carouselIdentifierDescriptor) {
			carouselIdentifierDesc = carouselIdentifierDescriptor;

		}

	}



	public List<BIOPMessage> getBIOPMessagesForModule(
			DSMCC_UNMessageSection dii,
			int moduleId) {
		List<BIOPMessage> biopMessages = null;
		ModuleInfo moduleInfo = dii.getModule(moduleId);
		if(moduleInfo!=null){
			BIOPModuleInfo biopModInfo = moduleInfo.getBiopModuleInfo();
			if(biopModInfo!=null){ // find PID containing actual DDB with module data
				List<Tap> moduleTaps = biopModInfo.getTaps();
				if((moduleTaps!=null)&&(!moduleTaps.isEmpty())){
					Tap moduleTap = moduleTaps.getFirst();
					int moduleAssociationTag = moduleTap.getAssociation_tag();
					DSMCC ddbPid = dsmccs.get(moduleAssociationTag);
					byte [] rawData= ddbPid.getDDMbytes(moduleInfo);
					if(rawData!=null){
						rawData = uncompressModuleData(biopModInfo,rawData);
						biopMessages = BIOPMessageFactory.createBIOPMessages(rawData, 0);
					}
				}
			}
		}
		return biopMessages;
	}

	public byte[] uncompressModuleData(BIOPModuleInfo biopModInfo,
                                       byte[] rawData) {
		byte[] res =rawData;
		List<Descriptor> moduleDescriptors = biopModInfo.getDescriptors();
		if((moduleDescriptors!=null)&&(!moduleDescriptors.isEmpty())){
			List<CompressedModuleDescriptor> compressedModuleDescriptorsList = Descriptor.findGenericDescriptorsInList(moduleDescriptors, CompressedModuleDescriptor.class); //Descriptor: compressed_module_descriptor: 0x9 (9)
			if(!compressedModuleDescriptorsList.isEmpty()){ // comrpession used
				CompressedModuleDescriptor compressedModuleDescriptor =  compressedModuleDescriptorsList.getFirst();
				int original_size = (int)compressedModuleDescriptor.getOriginal_size();
				res = decompress(rawData, original_size);
			}
		}
		return res;
	}

	public byte[] decompress(byte[] rawData, int original_size) {
		byte []res = rawData;
		Inflater decompressor = new Inflater();
		decompressor.setInput(res);
		byte [] uncompressed =new byte[original_size];
		try {
			int inflated = decompressor.inflate(uncompressed);
			decompressor.end();
			if(inflated==original_size){
				res = uncompressed;
			}else{
				logger.log(Level.WARNING, "Error inflating data, inflated!=original_size");
			}

		} catch (DataFormatException e) {
			logger.log(Level.WARNING, "inflating DSM-CC failed:", e);
		}
		return res;
	}

	private Map<Integer, DSMCC> dsmccs = new HashMap<>();
	private Map<Integer,ObjectCarousel> bootList = new HashMap<>();


	private final int programNumber; // service id
	private PSI psi; // reference to parentPSI, to get service names etc...


	public ServiceDSMCC(int programNum) {
		programNumber = programNum;
	}

	public void addDSMCC(int association_tag, DSMCC dsmcc){
		dsmccs.put(association_tag, dsmcc);
		psi = dsmcc.getParentPSI();
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("DSM-CC ObjectCarousel components for program",programNumber,psi.getSdt().getServiceNameForActualTransportStream(programNumber));

		TreeSet<Integer> s = new TreeSet<>(dsmccs.keySet());
		for (int associationTag : s) {
			KVP association = new KVP("Association: " + associationTag);
			DSMCC dsmcc = dsmccs.get(associationTag);
			association.add(new KVP("PID", dsmcc.getPid()));
			t.add(association);
		}
		t.addList(bootList.values(), modus, "ObjectCarousels");
		return t;
	}

	public void addBootPID(int associationTag, int dataBroadCastId) {
		bootList.put(associationTag, new ObjectCarousel(associationTag, dataBroadCastId));

	}


	public BIOPMessage getBIOPMessage(CarouselIdentifierDescriptor carouselIdentifierDescriptor, int associationTag){
		BIOPMessage biopMessage = null;

		DSMCC ddbPid = dsmccs.get(associationTag);
		byte [] rawData= ddbPid.getDDMbytes(carouselIdentifierDescriptor.getModuleId());
		if(rawData!=null){
			if(carouselIdentifierDescriptor.getCompressionMethod()!=0){
				rawData = decompress(rawData, (int)carouselIdentifierDescriptor.getOriginalSize());
			}
			List<BIOPMessage> biopMessages = BIOPMessageFactory.createBIOPMessages(rawData, 0);
			if(biopMessages!=null){
				byte []findObjectKey = carouselIdentifierDescriptor.getObjectKeyData();
				for (BIOPMessage biopMessage2: biopMessages) {
					byte [] foundObjectKey = biopMessage2.getObjectKey_data_byte();
					if(Utils.equals(findObjectKey, 0, findObjectKey.length, foundObjectKey, 0, foundObjectKey.length)){
						return biopMessage2;
					}
				}
			}
		}

		return biopMessage;
	}


	public BIOPMessage getBIOPMessage(IOR ior){
		BIOPMessage biopMessage = null;

		List<TaggedProfile> profiles = ior.getProfiles();

		if((profiles!=null)&&(!profiles.isEmpty())){
			TaggedProfile profile = ior.getProfiles().getFirst(); // should be only one for the serviceGateway
			if(profile!=null){
				List<LiteComponent> components = profile.getLiteComponents(); // should be 2; BIOP::ObjectLocation and DSM::COnnBinder
				if((components!=null)&&(components.size()>1)){
					LiteComponent objectLocation = components.get(0);
					LiteComponent connBinder = components.get(1);
					if((objectLocation instanceof BIOPObjectLocation biopObjectLocation) &&
							(connBinder instanceof DSMConnBinder connBind)){
                        List<Tap> taps = connBind.getTaps();
						if((taps!=null)&&(!taps.isEmpty())){
							Tap tap = connBind.getTaps().getFirst();
							if(tap!=null){
								// now find the PID with association
								int association = tap.getAssociation_tag();
								DSMCC diiPid = dsmccs.get(association);
								if(diiPid!=null){
									// look for the DII with the right transaction ID
									long transaction = tap.getTransactionId();
									int tableExtension = (int) (transaction & Utils.MASK_16BITS);
									DSMCC_UNMessageSection dii = diiPid.getDII(tableExtension);
									if(dii!=null){
										int moduleId = biopObjectLocation.getModuleId();
										byte[] findObjectKey = biopObjectLocation.getObjectKey_data_byte();
										List<BIOPMessage> biopMessages= getBIOPMessagesForModule(dii, moduleId);
										if(biopMessages!=null){
											for (BIOPMessage biopMessage2: biopMessages) {
												byte [] foundObjectKey = biopMessage2.getObjectKey_data_byte();
												if(Utils.equals(findObjectKey, 0, findObjectKey.length, foundObjectKey, 0, foundObjectKey.length)){
													return biopMessage2;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		return biopMessage;
	}

	public void setCarouselIdentifierDescriptor(int associationTag,
                                                CarouselIdentifierDescriptor carouselIdentifierDescriptor) {
		ObjectCarousel objectCarousel = bootList.get(associationTag);
		if(objectCarousel!=null){
			objectCarousel.setCarouselIdentifierDescriptor(carouselIdentifierDescriptor);
		}

	}

}
