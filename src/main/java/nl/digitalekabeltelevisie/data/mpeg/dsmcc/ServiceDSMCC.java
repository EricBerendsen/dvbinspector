/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import javax.swing.tree.DefaultMutableTreeNode;

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

		public DSMFile(final BIOPMessage biopMessage, final String label) {
			this.biopMessage = biopMessage;
			this.label = label;
		}

		public BIOPMessage getBiopMessage() {
			return biopMessage;
		}

		public void setBiopMessage(final BIOPFileMessage biopMessage) {
			this.biopMessage = biopMessage;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(final String label) {
			this.label = label;
		}



		public String getHTML() {
			final StringBuilder b = new StringBuilder();
			if(biopMessage instanceof BIOPFileMessage){
				b.append("Type: File<br>Size:");
				final BIOPFileMessage fileMes= (BIOPFileMessage) biopMessage;
				b.append(fileMes.getContent_length());

			}else if(biopMessage instanceof BIOPDirectoryMessage){
				b.append("Type: Directory<br>Descendants:<br>");
				final BIOPDirectoryMessage dirMes= (BIOPDirectoryMessage) biopMessage;
				for(final Binding binding:dirMes.getBindingList()){
					b.append(binding.getBiopName().getName()).append(": (").append(BIOPDirectoryMessage.getBindingTypeString(binding.getBindingType())).append(")<br>");
				}
			}else if(biopMessage instanceof BIOPStreamEventMessage){
				b.append("Type: StreamEvent<br>EventNames:<br>");
				BIOPStreamEventMessage eventMsg = (BIOPStreamEventMessage)biopMessage;
				for(EventName eventName:eventMsg.getEventNames()){
					b.append(new String(eventName.getEventName_data_byte())).append("<br>");

				}
			}

			return b.toString();
		}
		
		@Override
		public void save(final File file) {
			saveFile(file, this.getBiopMessage());
		}

		public void saveFile(final File file, final BIOPMessage biopMessage) {
			logger.info("saveFile called for file path:"+file.getAbsolutePath()+", name:"+file.getName());
			if(biopMessage instanceof BIOPFileMessage){
				logger.info("starting write file");
				try (FileOutputStream out = new FileOutputStream(file)){

					final BIOPFileMessage biopFile = (BIOPFileMessage)biopMessage;
					out.write(biopFile.getData(),biopFile.getContentStartOffset(),(int)biopFile.getContent_length());

				} catch (IOException e) {
					logger.log(Level.WARNING,"could not write file",e);
				}
			}else if(biopMessage instanceof BIOPDirectoryMessage){
				//recursively save tree..
				// file now contains the dir to start from.
				// append label first
				final BIOPDirectoryMessage dirMes = (BIOPDirectoryMessage)biopMessage;

				logger.log(Level.INFO,"trying to create directory "+ file);
				final boolean success = file.mkdir();
				logger.log(Level.INFO,"trying to create directory. result "+ success);
				for(final Binding binding:dirMes.getBindingList()){
					final String descName=binding.getBiopName().getName();
					final IOR ior = binding.getIor();
					final BIOPMessage child = getBIOPMessage(ior);
					if((child!=null)&&(descName!=null)){
						logger.log(Level.INFO,"now going down to save:"+descName);
						saveFile(new File(file,descName),child);
					}
				}
			}
		}
	}


	public class ObjectCarousel implements TreeNode{
		/**
		 * @param associationTag
		 * @param dataBroadCastId
		 */
		public ObjectCarousel(final int associationTag, final int dataBroadCastId) {
			super();
			this.associationTag = associationTag;
			this.dataBroadCastId = dataBroadCastId;
		}


		private final int associationTag;
		private final int dataBroadCastId;
		private CarouselIdentifierDescriptor carouselIdentifierDesc;

		// recursive find all biopMessagases, and add as flat list
		private  void addToTree(final BIOPMessage biopMessage, final String label, final DefaultMutableTreeNode s,final int modus){
			if(biopMessage!=null){
				if(biopMessage instanceof BIOPDirectoryMessage){
					final BIOPDirectoryMessage dir = (BIOPDirectoryMessage) biopMessage;
					s.add(dir.getJTreeNode(modus,label));
					final List<Binding> bindings = dir.getBindingList();
					for(final Binding binding:bindings){
						final BIOPName biopName = binding.getBiopName();
						final IOR ior = binding.getIor();
						final BIOPMessage child = getBIOPMessage(ior);
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
		private  DefaultMutableTreeNode buildTree(final BIOPMessage biopMessage, final String label, final int modus){
			DefaultMutableTreeNode treeNode = null;
			final KVP kvp = new KVP(label);
			treeNode = new DefaultMutableTreeNode(kvp);
			if(biopMessage instanceof BIOPDirectoryMessage){
				final BIOPDirectoryMessage dir = (BIOPDirectoryMessage) biopMessage;
				final DSMFile dsmFile = new DSMFile(dir,label);
				kvp.setHtmlSource(dsmFile);

				final JMenuItem objectMenu = new JMenuItem("Export (sub)tree...");
				objectMenu.setActionCommand(DVBtree.EXPORT);
				kvp.setSubMenuAndOwner(objectMenu,dsmFile);
				final List<Binding> bindings = dir.getBindingList();
				for(final Binding binding:bindings){
					final BIOPName biopName = binding.getBiopName();
					final IOR ior = binding.getIor();
					final BIOPMessage child = getBIOPMessage(ior);
					if((child!=null)&&(biopName!=null)){
						treeNode.add(buildTree(child, biopName.getName(), modus));
					}
				}
			}else if(biopMessage instanceof BIOPFileMessage){
				final JMenuItem objectMenu = new JMenuItem("Save as...");
				objectMenu.setActionCommand(DVBtree.SAVE);
				final DSMFile dsmFile = new DSMFile(biopMessage,label);
				kvp.setHtmlSource(dsmFile);

				kvp.setSubMenuAndOwner(objectMenu,dsmFile);

			}else if(biopMessage instanceof BIOPStreamEventMessage){
				final DSMFile dsmFile = new DSMFile(biopMessage,label);
				kvp.setHtmlSource(dsmFile);
			}
			return treeNode;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("Object Carousel"));
			s.add(new DefaultMutableTreeNode(new KVP("associationTag",associationTag,null)));
			s.add(new DefaultMutableTreeNode(new KVP("dataBroadCastId",dataBroadCastId,Utils.getDataBroadCastIDString(dataBroadCastId))));
			if(carouselIdentifierDesc!=null){
				s.add(carouselIdentifierDesc.getJTreeNode(modus));
			}
			final DSMCC_UNMessageSection dsi = dsmccs.get(associationTag).getDSI();
			if((carouselIdentifierDesc!=null)&&(carouselIdentifierDesc.getFormatId()==0x01)){  //enhanced boot
				// module must be in this PID
				final BIOPMessage biopMessage = getBIOPMessage(carouselIdentifierDesc, associationTag);
				addTreeDetails(modus, s, biopMessage);
			}else if(dsi!=null){
				final IOR ior = dsi.getServiceGatewayIOR();
				final BIOPMessage biopMessage = getBIOPMessage(ior);
				addTreeDetails(modus, s, biopMessage);
			}// no DSI, no starting point....

			return s;
		}

		public void addTreeDetails(final int modus, final DefaultMutableTreeNode s,
				final BIOPMessage biopMessage) {
			final DefaultMutableTreeNode messages=new DefaultMutableTreeNode(new KVP("Messages"));
			addToTree(biopMessage, "", messages, modus);
			s.add(messages);
			if(biopMessage!=null){
				s.add(buildTree(biopMessage, "ObjectHierarchy", modus));
			}
		}

		public void setCarouselIdentifierDescriptor(
				final CarouselIdentifierDescriptor carouselIdentifierDescriptor) {
			carouselIdentifierDesc = carouselIdentifierDescriptor;

		}

	}



	public List<BIOPMessage> getBIOPMessagesForModule(
			final DSMCC_UNMessageSection dii,
			final int moduleId) {
		List<BIOPMessage> biopMessages = null;
		final ModuleInfo moduleInfo = dii.getModule(moduleId);
		if(moduleInfo!=null){
			final BIOPModuleInfo biopModInfo = moduleInfo.getBiopModuleInfo();
			if(biopModInfo!=null){ // find PID containing actual DDB with module data
				final List<Tap> moduleTaps = biopModInfo.getTaps();
				if((moduleTaps!=null)&&(moduleTaps.size()>0)){
					final Tap moduleTap = moduleTaps.get(0);
					final int moduleAssociationTag = moduleTap.getAssociation_tag();
					final DSMCC ddbPid = dsmccs.get(moduleAssociationTag);
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

	public byte[] uncompressModuleData(final BIOPModuleInfo biopModInfo,
			final byte[] rawData) {
		byte[] res =rawData;
		final List<Descriptor> moduleDescriptors = biopModInfo.getDescriptors();
		if((moduleDescriptors!=null)&&(moduleDescriptors.size()>0)){
			final List<CompressedModuleDescriptor> compressedModuleDescriptorsList = Descriptor.findGenericDescriptorsInList(moduleDescriptors, CompressedModuleDescriptor.class); //Descriptor: compressed_module_descriptor: 0x9 (9)
			if((compressedModuleDescriptorsList!=null)&&(compressedModuleDescriptorsList.size()>0)){ // comrpession used
				final CompressedModuleDescriptor compressedModuleDescriptor =  compressedModuleDescriptorsList.get(0);
				final int original_size = (int)compressedModuleDescriptor.getOriginal_size();
				res = decompress(rawData, original_size);
			}
		}
		return res;
	}

	public byte[] decompress(final byte[] rawData, final int original_size) {
		byte []res = rawData;
		final Inflater decompressor = new Inflater();
		decompressor.setInput(res);
		final byte [] uncompressed =new byte[original_size];
		try {
			final int inflated = decompressor.inflate(uncompressed);
			decompressor.end();
			if(inflated==original_size){
				res = uncompressed;
			}else{
				logger.log(Level.WARNING, "Error inflating data, inflated!=original_size");
			}

		} catch (final DataFormatException e) {
			logger.log(Level.WARNING, "inflating DSM-CC failed:", e);
		}
		return res;
	}

	private Map<Integer, DSMCC> dsmccs = new HashMap<>();
	private Map<Integer,ObjectCarousel> bootList = new HashMap<>();


	private final int programNumber; // service id
	private PSI psi; // reference to parentPSI, to get service nmaes etc...


	public ServiceDSMCC(final int programNum) {
		programNumber = programNum;
	}

	public void addDSMCC(final int association_tag, final DSMCC dsmcc){
		dsmccs.put(association_tag, dsmcc);
		psi = dsmcc.getParentPSI();
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CC ObjectCarousel components for program",programNumber,psi.getSdt().getServiceNameForActualTransportStream(programNumber)));

		final TreeSet<Integer> s = new TreeSet<>(dsmccs.keySet());
		for (int associationTag : s) {
			final DefaultMutableTreeNode association = new DefaultMutableTreeNode(new KVP("Association: " + associationTag));
			final DSMCC dsmcc = dsmccs.get(associationTag);
			association.add(new DefaultMutableTreeNode(new KVP("PID", dsmcc.getPid(), null)));
			t.add(association);
		}
		Utils.addListJTree(t, bootList.values(), modus, "ObjectCarousels");
		return t;
	}

	public void addBootPID(final int associationTag, final int dataBroadCastId) {
		bootList.put(associationTag, new ObjectCarousel(associationTag, dataBroadCastId));

	}


	public BIOPMessage getBIOPMessage(final CarouselIdentifierDescriptor carouselIdentifierDescriptor, final int associationTag){
		final BIOPMessage biopMessage = null;

		final DSMCC ddbPid = dsmccs.get(associationTag);
		byte [] rawData= ddbPid.getDDMbytes(carouselIdentifierDescriptor.getModuleId());
		if(rawData!=null){
			if(carouselIdentifierDescriptor.getCompressionMethod()!=0){
				rawData = decompress(rawData, (int)carouselIdentifierDescriptor.getOriginalSize());
			}
			final List<BIOPMessage> biopMessages = BIOPMessageFactory.createBIOPMessages(rawData, 0);
			if(biopMessages!=null){
				final byte []findObjectKey = carouselIdentifierDescriptor.getObjectKeyData();
				for (final BIOPMessage biopMessage2: biopMessages) {
					final byte [] foundObjectKey = biopMessage2.getObjectKey_data_byte();
					if(Utils.equals(findObjectKey, 0, findObjectKey.length, foundObjectKey, 0, foundObjectKey.length)){
						return biopMessage2;
					}
				}
			}
		}

		return biopMessage;
	}


	public BIOPMessage getBIOPMessage(final IOR ior){
		final BIOPMessage biopMessage = null;

		final List<TaggedProfile> profiles = ior.getProfiles();

		if((profiles!=null)&&(profiles.size()>0)){
			final TaggedProfile profile = ior.getProfiles().get(0); // should be only one for the serviceGateway
			if(profile!=null){
				final List<LiteComponent> components = profile.getLiteComponents(); // should be 2; BIOP::ObjectLocation and DSM::COnnBinder
				if((components!=null)&&(components.size()>1)){
					final LiteComponent objectLocation = components.get(0);
					final LiteComponent connBinder = components.get(1);
					if((objectLocation instanceof BIOPObjectLocation) &&
							(connBinder instanceof DSMConnBinder)){
						final DSMConnBinder connBind =(DSMConnBinder)connBinder;
						final BIOPObjectLocation biopObjectLocation = (BIOPObjectLocation) objectLocation;
						final List<Tap> taps = connBind.getTaps();
						if((taps!=null)&&(taps.size()>0)){
							final Tap tap = connBind.getTaps().get(0);
							if(tap!=null){
								// now find the PID with association
								final int association = tap.getAssociation_tag();
								final DSMCC diiPid = dsmccs.get(association);
								if(diiPid!=null){
									// look for the DII with the right transaction ID
									final long transaction = tap.getTransactionId();
									final int tableExtension = (int) (transaction & Utils.MASK_16BITS);
									final DSMCC_UNMessageSection dii = diiPid.getDII(tableExtension);
									if(dii!=null){
										final int moduleId = biopObjectLocation.getModuleId();
										final byte[] findObjectKey = biopObjectLocation.getObjectKey_data_byte();
										final List<BIOPMessage> biopMessages= getBIOPMessagesForModule(dii, moduleId);
										if(biopMessages!=null){
											for (final BIOPMessage biopMessage2: biopMessages) {
												final byte [] foundObjectKey = biopMessage2.getObjectKey_data_byte();
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

	public void setCarouselIdentifierDescriptor(final int associationTag,
			final CarouselIdentifierDescriptor carouselIdentifierDescriptor) {
		final ObjectCarousel objectCarousel = bootList.get(associationTag);
		if(objectCarousel!=null){
			objectCarousel.setCarouselIdentifierDescriptor(carouselIdentifierDescriptor);
		}

	}

}
