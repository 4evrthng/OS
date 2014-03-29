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
	long[][] userMemory = null;
	SMem  supervMemory = null;
	int TIME = 0;
	Reg8B AX, BX, CX;
	StatusFlag SF;
	RegB IP, CH1, CH2, CH3, TI, SI, PI, MODE, PTR;
	
	
	
	public RM() {
		//iskirti atminties dalis, init dar kitus daiktus
		AX = new Reg8B();
		BX = new Reg8B();
		CX = new Reg8B();
		SF = new StatusFlag();
		IP = new RegB();
		CH1 = new RegB();
		CH2 = new RegB();
		CH3 = new RegB();
		TI = new RegB();
		SI = new RegB();
		PI = new RegB();
		MODE = new RegB();
		PTR = new RegB();
		userMemory = new long[128][16];
		supervMemory = new SMem();
		setSharedMem();
	}
	
	//TODO padaryti nenuosekliai ir sukurti shared memory deskriptoriu?
	private void setSharedMem() {
		for(int i=112;i<128;i++) supervMemory.pageTable[i] = i+1;
		
	}


	public Interpretator createVM(String path) throws Exception {//dar reikia sud4ti atminti i lentele ir rodyti su ptr?..
		int i = 0, temp=0, j, s=0;
		long[][] memory = new long[16][16];
		Random rand = new Random();
		for(int k=0; k<128;k++) {
			if (!this.pageUsed(k)) s++;
		}
		if (s<16) throw new Exception();//TODO
		while (i < 16) {
			j = rand.nextInt(128);
			if (!this.pageUsed(j)) {
				if (i==0) {
					PTR.value = (byte)(j&0xFF);
				}
				else this.setPageUsed(temp,j); //TODO
				this.setPageUsed(j, 128);
				temp = j;
				memory[i] = userMemory[j];
				i++;
			}
		}
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
	    Interpretator vmachine = new Interpretator(AX, BX, CX, SF, IP, memory);
		this.saveVM(vmachine);
		return new Interpretator(AX, BX, CX, SF, IP, memory);
	}

	private void setPageUsed(int point, int to) {
		// TODO test
		supervMemory.pageTable[point] = to;
		if (to!=128) supervMemory.pageTable[to] = 128;
	}


	private boolean pageUsed(int k) {
		//TODO test
		if (supervMemory.pageTable[k]!=0) return true;
		else return false;
	}


	private void saveVM(Interpretator vmachine) {
		this.supervMemory.desc.addFirst(new VMDesc(vmachine, PTR));
	}

	
	public void destroyCurrentVM() {
		int b = this.supervMemory.desc.get(0).PTR;
		this.supervMemory.desc.remove();
		clearPage(b);
	}
	
	public void clearPage(int b) {
		int i = supervMemory.pageTable[b];
		if (i!=128) clearPage(i);
		supervMemory.pageTable[b] = 0;
	}
	
	//TODO pabaigti
	public boolean run(Interpretator VM) {
		Interrupt inter = null;
		while (true){
			while (inter == null) { //or anything else
				inter = VM.interpreting();
			}
			if (inter.interruptCode==0) return true; //TODO apdorojimai
		}
	}
	

	public static void main(String[] args) {
		RM r = new RM();
		int s=0,d=0;
		Interpretator VM = null;
		try {
			VM = r.createVM("/home/helchon/Desktop/git/OS/Assembler/codeBytes");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<128;i++) {
			if (r.pageUsed(i)) s++;
		}
		r.run(VM);

		
		//TODO 
		r.destroyCurrentVM();
		for(int i=0; i<128;i++) {
			if (r.pageUsed(i)) d++;
		}
		System.out.println(s);
		System.out.println(d);
	}
}