package com.tommo.nfcdatas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.smartcardio.*;

public class Main {
	
	public static void main(String[] args) {
		File dir = new File("uids");
		
		if(!dir.exists()) {
			dir.mkdirs();
		}
		
		TerminalFactory tf = TerminalFactory.getDefault();
		
		CardTerminals readers = tf.terminals();
		
		try {
			List<CardTerminal> readersList = readers.list();
			
			CardTerminal ct = readersList.get(0);
			
			Thread readCardThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while(true) {
						Card card = null;
							
						try {
							ct.waitForCardPresent(5000);
								
							if(ct.isCardPresent()) {
								card = ct.connect("*");
									
								if(card != null) {
									CardChannel channel = card.getBasicChannel();
										
									CommandAPDU command = new CommandAPDU((byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, 256);
									ResponseAPDU response = channel.transmit(command);
									
									String hexUID = bytesToHex(response.getBytes()).substring(0, 8);
									String decUID = hexToDec(hexUID);
									
									System.out.println(hexUID);
									System.out.println(decUID);
									
									File uidFile = new File("uids/card_" + hexUID + ".txt");
									
									if(!uidFile.exists()) {
										try {
											uidFile.createNewFile();
											
											BufferedWriter writer = new BufferedWriter(new FileWriter(uidFile));
											
											writer.write(hexUID);
											
											writer.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
									
									try {
										Thread.sleep(200);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
						} catch (CardException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			readCardThread.start();
		} catch (CardException e) {
			e.printStackTrace();
		}
	}
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static String hexToDec(String hex) {
    	char[] charArray = hex.toCharArray();
    	String dec = "";
    	
    	for(int i = 0; i < charArray.length; i++) {
    		String hexUnit = "" + charArray[i];
    		
    		int decUnit = Integer.parseInt(hexUnit, 16);
    		dec = dec + decUnit;
    	}
    	
    	return dec;
    }
}