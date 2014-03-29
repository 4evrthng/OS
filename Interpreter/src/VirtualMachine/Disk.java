package VirtualMachine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Disk {
	
	private Reg8B AX;
	private Reg8B BX;
	private Reg8B CX;
	private long[][] userMemory;
	private SMem  supervMemory;
	private FileChannel diskChanel;
	
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
	
	public void fileWrite (ByteBuffer out) {
		try {
			while (out.hasRemaining())
				diskChanel.write(out);
		} catch (IOException x) {
			System.out.println("I/O Exception in fileWrite: " + x);
		}
	}
	
	public static void main(String[] args) {
		Disk testDisk = new Disk (new Reg8B(), new Reg8B(), new Reg8B(), new long[RM.MAX_PAGES][16], new SMem());
		
		String s = "I was here!\n";
		byte data[] = s.getBytes();
		ByteBuffer out = ByteBuffer.wrap(data);
		
		testDisk.fileWrite(out);
	        
	}
}
