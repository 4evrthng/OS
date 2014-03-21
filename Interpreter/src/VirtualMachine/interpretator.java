package VirtualMachine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;

public class interpretator {
	static long AX, BX, CX;
	static byte SF, IP;
	static long[] memory = new long[256];
	
	public static void Interpretator() {
		
		File pFile = new File("C:/Users/Helch/Desktop/kzk.txt");
	    FileInputStream inFile = null;
	    try {
	      inFile = new FileInputStream(pFile);
	    } catch (FileNotFoundException e) {
	      e.printStackTrace(System.err);
	    }
	    FileChannel inChannel = inFile.getChannel();
	    ByteBuffer buf = ByteBuffer.allocate(8);
	    int i = 0;
	    try {
	      while (inChannel.read(buf) != -1) {
	        memory[i] = ((ByteBuffer) (buf.flip())).asLongBuffer().get();
	        buf.clear();
	        i++;
	      }
	      inFile.close();
	    } catch (IOException e) {
	      e.printStackTrace(System.err);
	    }
		
	    AX = 0;
	    BX = 0;
	    CX = 0;
	    SF = 0;
	    IP = 0;		
	}

	public void Interpreting() {
		long cmd = 0;
		cmd = memory[IP];
		byte[] cmdB = new byte[4];
		cmd = cmd >>> 32;
		for(int i = 0; i<4; i++){
			cmdB[i] = (byte)((cmd >>> 8*i) % 0xFF);
		}
		switch (cmdB[0]) {
			case 0x1:
		}
	}

	public static void main(String[] args) {
		Interpretator();
		System.out.println(memory[0]);
		System.out.println(memory[1]);
		System.out.println(memory[2]);
		System.out.println(memory[3]);
		//Interpreting();
		
	}

}
