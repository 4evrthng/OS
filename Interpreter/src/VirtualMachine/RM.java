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
	public static final int MAX_PAGES =128;
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
		userMemory = new long[MAX_PAGES][16];
		supervMemory = new SMem();
		setSharedMem();
	}
	
	//TODO padaryti nenuosekliai ir sukurti shared memory deskriptoriu?
	private void setSharedMem() {
		int temp, j=0, i=0;
		Random rand = new Random();
		while (i < 16) {
			j = rand.nextInt(MAX_PAGES);
			while (this.pageUsed(j)) if (j!=MAX_PAGES-1) j++; else j=0;
			
			if (i==0) PTR.value = (byte)(j&0xFF);
			else this.setPageUsed(temp,j); 
			this.setPageUsed(j, MAX_PAGES);
			temp = j;
			i++;
		}
		supervMemory.shareMem = new SharedMemoryDesc(PTR.value);
	}
	
	//TODO test shared
	private void loadShr(Reg8B reg, int mem) {
		int block = getSharedBlock(mem/16);
		if (supervMemory.shareMem.getSemafor(block))  ;//TODO sugalvoti, k1 daryti, kai naudojama atmintis tuo metu
		else {
			supervMemory.shareMem.setSemafor(block);
			int word = mem%16;
			reg.value = userMemory[block][word];
			supervMemory.shareMem.clearSemafor(block);
		}
	}

	private void storeShr(Reg8B reg, int mem) {
		int block = getSharedBlock(mem/16);
		if (supervMemory.shareMem.getSemafor(block))  ;//TODO sugalvoti, k1 daryti, kai naudojama atmintis tuo metu
		else {
			supervMemory.shareMem.setSemafor(block);
			int word = mem%16;
			userMemory[block][word] = reg.value;
			supervMemory.shareMem.clearSemafor(block);
		}
	}
	
	private int getSharedBlock(int i) {
		int block = supervMemory.shareMem.PTR &0xFF;
		while(i!=0) {
			block = supervMemory.pageTable[block];
			i--;
		}
		return block;
	}


	public Interpretator createVM(String path) throws Exception {//dar reikia sud4ti atminti i lentele ir rodyti su ptr?..
		int i = 0, temp=0, j, s=0;
		long[][] memory = new long[16][16];
		Random rand = new Random();
		
		for(int k=0; k<MAX_PAGES;k++) if (!this.pageUsed(k)) s++;
		if (s<16) throw new Exception();//TODO
		
		while (i < 16) {
			j = rand.nextInt(MAX_PAGES);
			while (this.pageUsed(j)) if (j!=MAX_PAGES-1) j++; else j=0;
			
			if (i==0) PTR.value = (byte)(j&0xFF);
			else this.setPageUsed(temp,j); 
			this.setPageUsed(j, MAX_PAGES);
			temp = j;
			memory[i] = userMemory[j];
			i++;
		}
		
		//6ita dalis keliau lauk
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
	    //dalis iki cia.
	    
	    Interpretator vmachine = new Interpretator(AX, BX, CX, SF, IP, memory);
		this.saveNewVM(vmachine);
		return new Interpretator(AX, BX, CX, SF, IP, memory);
	}

	private void setPageUsed(int currentPage, int nextPage) {
		// TODO test
		supervMemory.pageTable[currentPage] = nextPage;
		if (nextPage!=MAX_PAGES) supervMemory.pageTable[nextPage] = MAX_PAGES;
	}


	private boolean pageUsed(int k) {
		if (supervMemory.pageTable[k]!=0) return true;
		else return false;
	}


	private void saveNewVM(Interpretator vmachine) {
		this.supervMemory.desc.addFirst(new VMDesc(vmachine, PTR));
	}
	
	public void saveCurrentVM() {
		boolean found = false;
		int i = -1;
		VMDesc desc = null;
		while (!found) {
			i++;
			if (i>=this.supervMemory.desc.size()) ;//TODO error some kind
			desc = this.supervMemory.desc.get(i);
			if (desc.PTR == PTR.value) found = true;
		}
		desc.AX = AX.value;
		desc.CX = CX.value;
		desc.BX = BX.value;
		desc.IP = IP.value;
		desc.SF = SF.value;
	}

	
	public void destroyCurrentVM() {
		boolean found = false;
		int i = -1;
		while (!found) {
			i++;
			if (i>=this.supervMemory.desc.size()) ;//TODO error some kind
			if (this.supervMemory.desc.get(i).PTR == PTR.value) found = true;
		}
		this.supervMemory.desc.remove(i);
		clearPage(PTR.value);
	}
	
	public void clearPage(int b) {
		int i = supervMemory.pageTable[b];
		if (i!=MAX_PAGES) clearPage(i);
		supervMemory.pageTable[b] = 0;
	}
	//TODO meniu parasyti
	public int meniu (String message[])
	{
		//TODO ateina string masyvas
		/*pvz a
			a[0]pranesimas, jei nera 1 tai tada nera issiunciami pasirinkimai
			a[1]valgyti
			a[2]gerti
			...
			a[n]miegoti
		Isvedimas: 
			Message: Pranesimas, Choose one of the options entering number of the option
			Option 1: valgyti
			Option 2: gerti
			Option 3: miegoti
		
		*/
		System.out.println("Message: "+message[0]);
		if (message[1])
		{
			System.out.println("")
		
			for (i = 0;i==message.length(),i++)
			{
			
			}
		}
	}
		
	}
	public void start(interpretator vm)
	{
		String trapFlagMenu[]("trapflag","NextStep","show details","change details","run","terminate");
		Interrupt code = this.run(vm);
		switch (code.interruptCode)
		{//apdoroti interupto koda
			case 0: // TODO EXIT
				System.out.println("Program has finished successfully");
				break;
			case 1: // TODO Trap FLag(menu)
				
				break;
			case 2://TODO neatpazinta komanda
				break;
			case 3://TODO dalyba is 0
				break;
			case 4://TODO perzengti adresacijos reziai
				break;
			case 5://TODO in
				break;
			case 6://TODO out
				break;
			case 7://TODO loadshare
				loadShr(code.reg, code.memAdress);
				break;
			case 8://TODO streshare
				storeShr(code.reg, code.memAdress);
				break;
			case 9://TODO  fopen
				break;
			case 10: //TODO fread
				break;
			case 11: //TODO fseek
				break;
			case 12://TODO Fclose
				break;
			case 13://TODO Fdelete
				break;
			case 14://TODO time
				break;
			//case 15://TODO
			//case 16://TODO
			//case 17://TODO
				
				
		}
	}
	//TODO pabaigti
	public boolean run(Interpretator VM) {//TODO: sitas metodas grazina interrupt koda?
		Interrupt inter = null;
		while (true){
			TIME = 12; //TODO sugalvoti k1 daryti su time
			while (inter == null) { //or anything else
				inter = VM.interpreting();
				TIME--;
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
		for(int i=0; i<MAX_PAGES;i++) {
			if (r.pageUsed(i)) s++;
		}
		//r.run(VM);
		r.start();
		
		//TODO 
		r.destroyCurrentVM();
		for(int i=0; i<MAX_PAGES;i++) {
			if (r.pageUsed(i)) d++;
		}
		System.out.println(s);
		System.out.println(d);
	}
}
