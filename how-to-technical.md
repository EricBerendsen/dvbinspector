# How to contribute

## Parser of a PES Data

### Handler class (./dvbinspector/src/main/java/nl/digitalekabeltelvisie/data/mpeg/pes)

This class, extending GeneralPesHandler should Override processPesDataBytes(PesPacketData). This overridden method allows to create a "Data Field" which is the data of the object we want to parse. By calling the constructors of a custom PesDataField, we read and build the given PesPacketData which is then added to the pesPackets list of the GeneralPesHandler.

Method getJTreeNode allows to display nodes and fields in the DVB Inspector GUI.

### DataField class (./dvbinspector/src/main/java/nl/digitalekabeltelvisie/data/mpeg/pes)

Extend PesPacketData. Allow to extend and add specific fields to the generic Pes packet.
This class is representing the object you want to parse.
 
 This class will Parse the data and display the parsed data, as it implements TreeNode (made through the extension of PesPacketData).
 It contains a constructor of the object taking an array of a following of bytes (byte[]) that are coming from the pesPacket - make sure that the offset is right and that the bytes being read are not already read by the PesPacketData Class. 

 This amount of bytes can be read easily using the method readBits("number of bits to read") from the BitSource class. It allows to read along the data and extract values by giving in parameter the length of the value to extract.


### Modify TransportStream class (./dvbinspector/src/main/java/nl/digitalekabeltelvisie/data/mpeg/TransportStream.java)

 Build the specific handler by adding the constructor, depending on the streamType corresponding to the type of the handler, in determinePesHandlerByStreamType()


### Modify DVBinspector class (./dvbinspector/src/main/java/nl/digitalekabeltelvisie/main/DVBinspector.java)

 Add the handler to the Enumeration PidHandlers, so the handler can be built.