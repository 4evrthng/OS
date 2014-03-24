// pati pati prad=ia
package VirtualMachine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

public class RM {
	
	File[] f = null; //padaryti ,kad open file failus saugotu cia or smt...
	long[][] oMemory = new long[160][16];
	boolean[] PUsed = new boolean[160];  //pakeist?
	int TIME = 0;
	Reg8B AX, BX, CX, PTR;
	StatusFlag SF;
	RegB IP, CH1, CH2, CH3, TI, SI, PI;
	
	
	
	public RM() {
		//iskirti atminties dalis, init dar kitus daiktus
		AX = new Reg8B();
		BX = new Reg8B();
		CX = new Reg8B();
		PTR = new Reg8B();
		SF = new StatusFlag();
		IP = new RegB();
		CH1 = new RegB();
		CH2 = new RegB();
		CH3 = new RegB();
		TI = new RegB();
		SI = new RegB();
		PI = new RegB();
	}
	
	public Interpretator createVM() {//dar reikia sud4ti atminti i lentele ir rodyti su ptr?..
		int i = 0, j;
		long[][] memory = new long[16][16];
		Random rand = new Random();
		while (i < 10) {
			j = rand.nextInt(160);
			if (!PUsed[j]) {
				PUsed[j] = true;
				memory[i] = oMemory[j];
				i++;
			}
		}	
		File pFile = new File("C:/Users/Helch/Desktop/testProg02_OUT");
	    FileInputStream inFile = null;
	    try {
	    	inFile = new FileInputStream(pFile);
	    } catch (FileNotFoundException e) {
	    	e.printStackTrace(System.err);
	    }
	    FileChannel inChannel = inFile.getChannel();
	    ByteBuffer buf = ByteBuffer.allocate(8);
	    i = 0;
	    j = 0;
	    try {
	    	while (inChannel.read(buf) != -1) {
	    		memory[i][j] = ((ByteBuffer) (buf.flip())).asLongBuffer().get();
	    		buf.clear();
	    		j++;
	    		if (j==8) {
	    			i++;
	    			j=0;
	    		}
	      }
	      inFile.close();
	    } catch (IOException e) {
	      e.printStackTrace(System.err);
	    }
		
		return new Interpretator(AX, BX, CX, SF, IP, memory);
	}
	

	public static void main(String[] args) {
	/*	Interpretator VM = new Interpretator();
		boolean a = true;
		while (a) {
			System.out.println();
			a = VM.interpreting();
		} */
		RM r = new RM();
		Interpretator VM = r.createVM();
		boolean a = true;
		while (a) {
			System.out.println();
			a = VM.interpreting();
		} 
	}
}