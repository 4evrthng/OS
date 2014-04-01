package VirtualMachine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class Disk {
	
	private Reg8B AX;
	private Reg8B BX;
	private Reg8B CX;
	private long[][] userMemory;
	private SMem  supervMemory;
	private FileChannel diskChanel;
	
	//TODO failu pavadinimai, Ir kaip juos ten patalpinti
	public Disk (Reg8B AX, Reg8B BX, Reg8B CX, long[][] userMemory, SMem  supervMemory) {
		this.AX = AX;
		this.BX = BX;
		this.CX = CX;
		this.userMemory = userMemory;
		this.supervMemory = supervMemory;
		
		Path diskLocation = Paths.get("C:/Users/Roman/Documents/GitHub/OS/HDD/hdd01");
		
		try {
			FileChannel diskChanel = (FileChannel.open(diskLocation, StandardOpenOption.WRITE, StandardOpenOption.READ));
		    this.diskChanel = diskChanel;
		} catch (IOException x) {
		    System.out.println("I/O Exception: " + x);
		}
	}	
	private byte getFirstSector(long fileHandle) {
		try {
			ByteBuffer firstSector = ByteBuffer.allocate(1); 
			diskChanel.position(fileHandle + 13);
			diskChanel.read(firstSector);
			return firstSector.get(0);
				
		} catch (IOException x) {
			System.out.println("I/O Exception while creating a file: " + x);
		}
		return (byte) 0;
	}
	private byte getNextSector (byte currentSector) {
		try {
			ByteBuffer nextSector = ByteBuffer.allocate(1);
			diskChanel.position(currentSector);
			diskChanel.read(nextSector);
			return nextSector.get(0);
				
		} catch (IOException x) {
			System.out.println("I/O Exception: " + x);
		}
		return (byte) 0;
	}
	private void setSector (byte sector, byte valueToSet){
		try {
			ByteBuffer valueToSetBuff = ByteBuffer.allocate(1);
			valueToSetBuff.put(valueToSet);
			valueToSetBuff.rewind();
			
			
			System.out.print(sector);
			
			diskChanel.position(sector & 0xFF);
			diskChanel.write(valueToSetBuff);
			
		} catch (IOException x) {
			System.out.println("I/O Exception: " + x);
		}
	}
	private byte returnEmptySector() {
		try {
			for (int i = 1; i < 255; i++) {
				diskChanel.position(i);
				ByteBuffer sector = ByteBuffer.allocate(1);
				diskChanel.read(sector);
				if (sector.get(0) == (byte) 0) {
					sector = ByteBuffer.allocate(1);
					sector.put((byte) 255);
					sector.rewind();
					diskChanel.position(i);
					diskChanel.write(sector);
					return (byte) i;
				}
			}
				
		} catch (IOException x) {
			System.out.println("I/O Exception: " + x);
		}
		return (byte) 0;
	}
	public long fileOpen(String fileName) {
		try {
			//Read the root directory which starts in sector 1 (byte 512)
			for (long i = 512; i < 1024; i = i + 16) {
				fileName = fileName.substring(0, Math.min(fileName.length() - 1, 12));
				ByteBuffer fileNameCandidate = ByteBuffer.allocate(fileName.length());
				diskChanel.position(i);
				diskChanel.read(fileNameCandidate);
				if (fileName.equals(new String(fileNameCandidate.array()))) {
					return i;
				}		
			}
			
		}catch (IOException x) {
		    System.out.println("I/O Exception while creating a file: " + x);
		}
		return 0;
	}
	public boolean fileCreate (String name) {
		name = name.substring(0, Math.min(name.length(), 13));
		try {
			//Read the root directory which starts in sector 1 (byte 512)
			for (int i = 512; i < 1024; i = i + 16) {
				diskChanel.position(i);
				ByteBuffer fileName = ByteBuffer.allocate(13);
				diskChanel.read(fileName);
				
				fileName.rewind();
				
				//Check if the spot is empty if it is create a file there
				if (0 == fileName.compareTo(ByteBuffer.allocate(13))) {
					diskChanel.position(i);
					ByteBuffer newFile = ByteBuffer.allocate(16);
					newFile.put(ByteBuffer.wrap(name.getBytes()));
					newFile.position(13);
					byte firstSector = returnEmptySector();
					newFile.put(firstSector);
					newFile.putShort((short)0);
					newFile.rewind();
					
					diskChanel.position(i);
					diskChanel.write(newFile);
					
					return true;
				}		
			}
			
		}catch (IOException x) {
		    System.out.println("I/O Exception while creating a file: " + x);
		}
		return false;
	}
	public void fileWrite (long fileHandle, byte[] data) {
		try {
			byte currentSector = getFirstSector(fileHandle);
			
			for (int i = 0; i < data.length; i += 512) {
				
				byte dataSector[] = Arrays.copyOfRange(data, i, Math.min(i + 512, data.length));
				
				diskChanel.position((currentSector & 0xFF) * 512);
				diskChanel.write(ByteBuffer.wrap(dataSector));
				
				if (getNextSector(currentSector) == (byte) 255 && i + 512 < data.length){
					setSector(currentSector, returnEmptySector());
				}
				
				currentSector = getNextSector(currentSector);

			}
			
			
		} catch (IOException x) {
			System.out.println("I/O Exception in fileWrite: " + x);
		}
	}
	public ByteBuffer fileRead (long fileHandle, long lenght) {
		
		byte currentSector = getFirstSector(fileHandle);
		ByteBuffer output = ByteBuffer.allocate((int)lenght);
		
		try {
			while ((currentSector & 0xFF) != 255 && lenght > 0) {
				
				System.out.print(currentSector + "\n");
				
				ByteBuffer sector = ByteBuffer.allocate(Math.min((int) lenght, 512));
				diskChanel.position((currentSector & 0xFF) * 512);
				diskChanel.read(sector);
				
				//System.out.print(new String(sector.array()) + "\n");
				sector.rewind();
				
				output.put(sector);
				
				//System.out.print(new String(output.array()) + "\n");
				
				lenght -= 512;
				currentSector = getNextSector(currentSector);
			}
			
		} catch (IOException x) {
			System.out.println("I/O Exception in fileRead: " + x);
		}
		
		return output;
	}
	public static void main(String[] args) throws IOException {
		Disk testDisk = new Disk (new Reg8B(), new Reg8B(), new Reg8B(), new long[RM.MAX_PAGES][16], new SMem());
		
		//ByteBuffer temp = testDisk.fileRead(512, 1000);
		//System.out.print(new String (temp.array()));
		
		testDisk.fileWrite(512, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccdddddddddddddddddddddddddeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee".getBytes());
		
		//System.out.print(testDisk.fileOpen("lauryyyyyyynahahahahahah"));
	        
	}
}
