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
	
	File[] f = null; //TODO padaryti ,kad open file failus saugotu cia or smt...
	long[][] oMemory = new long[160][16];
	boolean[] PUsed = new boolean[160];  //TODO pakeist?
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
		//PUsed[0] = true; //puslapiu lentele??
	}
	
	public Interpretator createVM(String path) throws Exception {//dar reikia sud4ti atminti i lentele ir rodyti su ptr?..
		int i = 0, j, s=0;
		long[][] memory = new long[16][16];
		Random rand = new Random();
		for(int k=0; k<160;k++) {
			if (!PUsed[k]) s++;
		}
		if (s<16) throw new Exception();
		PTR.value = 0;                //TODO
		long [] pages = new long[2];
		while (i < 16) {
			j = rand.nextInt(160);
			if (!PUsed[j]) {
				//if (i<4) pages[0] = pages[0] | ((long)j << i*8 );
				//else pages[1] = pages[1] | ((long)j << (i-4)*8 );
				PUsed[j] = true;
				memory[i] = oMemory[j];
				i++;
			}
		}
		//oMemory[(int) (PTR.value/16)][(int) (PTR.value%16)] = pages[0];
		//oMemory[(int) ((PTR.value+1)/16)][(int) ((PTR.value+1)%16)] = pages[1];
		File pFile = new File(path);
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
	
	/*
	public void destroyVM(Interpretator VM) {
		for(int i=0;i<16;i++) {
			if (i<4) PUsed[(int) (oMemory[(int) (PTR.value/16)][(int) (PTR.value%16)] & (0xFF << i*8))] = false;
			else PUsed[(int) (oMemory[(int) ((PTR.value+1)/16)][(int) ((PTR.value+1)%16)] & (0xFF << (i-4)*8))] = false;
		}
	}*/

	public static void main(String[] args) {
	/*	Interpretator VM = new Interpretator();
		boolean a = true;
		while (a) {
			System.out.println();
			a = VM.interpreting();
		} */
		RM r = new RM();
		int s=0,d=0;
		Interpretator VM = null;
		try {
			VM = r.createVM("C:/Users/Helch/Desktop/testProg02_OUT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<160;i++) {
			if (r.PUsed[i]) s++;
		}
		boolean a = true;
		while (a) {
			System.out.println();
			a = VM.interpreting();
		} 
		//TODO r.destroyVM(VM);
		for(int i=0; i<160;i++) {
			if (r.PUsed[i]) d++;
		}
		System.out.println(s);
		System.out.println(d);
	}
}