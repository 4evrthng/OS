// pati pati prad=ia
package VirtualMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class RM {
	public static final int MAX_PAGES =128;
	public static final int NOT_USED_PAGE =131;
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
		for (int i=0; i<MAX_PAGES; i++) supervMemory.pageTable[i] = NOT_USED_PAGE;
		setSharedMem();
	}
	
	//TODO padaryti nenuosekliai ir sukurti shared memory deskriptoriu?
	private void setSharedMem() {
		int temp=MAX_PAGES, j=0, i=0;
		Random rand = new Random();
		while (i < 16) {
			j = rand.nextInt(MAX_PAGES);
			while (this.pageUsed(j)) if (j!=MAX_PAGES-1) j++; else j=0;
			
			if (i==0) PTR.value = (byte)(j&0xFF);
			else this.setPageUsed(temp,j); //TODO temp ne inicializuotas
			this.setPageUsed(j, MAX_PAGES);
			temp = j;
			i++;
		}
		supervMemory.shareMem = new SharedMemoryDesc(PTR.value);
	}
	
	//TODO test shared
	private void loadShr(Reg8B reg, int mem) {
		int block = getBlock(mem, supervMemory.shareMem.PTR&0xFF);
		if (supervMemory.shareMem.getSemafor(block))  ;//TODO sugalvoti, k1 daryti, kai naudojama atmintis tuo metu
		else {
			supervMemory.shareMem.setSemafor(block);
			int word = mem%16;
			reg.value = userMemory[block][word];
			supervMemory.shareMem.clearSemafor(block);
		}
	}

	private void storeShr(Reg8B reg, int mem) {
		int block = getBlock(mem, supervMemory.shareMem.PTR&0xFF);
		if (supervMemory.shareMem.getSemafor(block))  ;//TODO sugalvoti, k1 daryti, kai naudojama atmintis tuo metu
		else {
			supervMemory.shareMem.setSemafor(block);
			int word = mem%16;
			userMemory[block][word] = reg.value;
			supervMemory.shareMem.clearSemafor(block);
		}
	}
	
	private int getBlock(int i, int point) {
		int block = point;
		i = i/16;
		while(i!=0) {
			block = supervMemory.pageTable[block];
			i--;
		}
		return block;
	}
	
	//TODO test IN
	public void IN(int mem) {
		if ((CX.value+7)/8+ mem >= 255) AX.value =  (255 - mem +1) *8;
		else AX.value = CX.value;
		char c;
		String input = "";
		AX.value = 0;
		BufferedReader br = new	BufferedReader(new InputStreamReader(System.in));
		for(int i=0; i<AX.value;i++) {
			try {
				c = (char) br.read();
			} catch (IOException e) {
				break;         //TODO kazkaip apdoroti ar ka?
			}
			input +=c;
		}
		AX.value = input.length();
		long[] words = stringToLongs(input);
		int arrayLength = words.length, block, word;
		for(int i=0; i<arrayLength; i++) {
			block = getBlock(mem, PTR.value&0xFF);
			word = mem%16;
			userMemory[block][word] = words[i];
			mem++;
		}
	}


	private long[] stringToLongs(String input) {
		int arrayLength = (input.length() +7)/8;
		long[] words = new long[arrayLength];
		long word;
		byte b;
		for(int i=0; i<arrayLength; i++) {
			word = 0;
			for(int j=0; j<8; j++) {
				try {
					b = (byte) input.charAt(i*8+j);
				} catch (IndexOutOfBoundsException e) {
					b = 0;
				}
				word = word | (b << (7-j)*8);
			}
			words[i] = word;
		}
		return words;
	}
//TODO test OUT
	public void OUT(int mem) {
		String s = "";
		int charCount;
		if ((CX.value+7)/8+ mem >= 255) charCount =  (255 - mem +1) *8;
		else charCount = (int) CX.value;
		long word = 0;
		int words =(int) charCount/8;
		byte cha = 0;
		for(int i=0; i<words; i++) {
			word = userMemory[getBlock(mem, PTR.value&0xFF)][mem%16];
			for(int j=0;j<8;j++) {
				cha = (byte) ((word >>>(7-j)*8)&0xFF);
				s+=((char)cha);
			}
			mem++;
		}
		int chars = (int) (charCount%8);
		if (chars != 0) {
			word = userMemory[getBlock(mem, PTR.value&0xFF)][mem%16];
			for(int i=0;i<chars;i++) {
				cha = (byte) ((word >>>(7-i)*8)&0xFF);
				s+=(char)cha;
			}
		}
		System.out.println("OUT: "+s);
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
	    		if (j==16) {
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
		if (supervMemory.pageTable[k]!=NOT_USED_PAGE) return true;
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
		supervMemory.pageTable[b] = NOT_USED_PAGE;
	}
	//TODO meniu parasyti
	public int meniu (String[] message)
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
		int i=0;
		Scanner scanner = new Scanner(System.in);
		int answer;
		System.out.println("Message: "+message[0]);
		if (message[1]!="")
		{
			System.out.println("");
		
			for (i = 1;i==message.length;i++)
			{
				System.out.println("Option"+1+": "+message[i]);
			}
			try
			{
				answer = scanner.nextInt();
				if (answer>message.length-1 || answer<0)
				{
					throw new Exception("Wrong range" );
				}
				return answer;
			}
				
			catch(InputMismatchException e)
			{
				System.out.println("You should enter an integer. in range from ");
			}
			catch(Exception e)
			{
				System.out.println("Bad range number should be between 1..100");
			}
			scanner.nextLine();
		
		}
		else return 0;
		return 0;
		
	}
		
	
	
	//TODO pabaigti
	public boolean run(Interpretator VM) {//TODO: sitas metodas grazina interrupt koda?
		Interrupt inter = null;
		Disk disk = null;
		//String trapFlagMenu[] = {"trapflag","NextStep","show details","change details","run","terminate"};
		
		while (true){
			inter = null;
			TIME = 12; //TODO sugalvoti k1 daryti su time
			while (inter == null) { //or anything else
				inter = VM.interpreting();
				TIME--;
			}
			//if (inter.interruptCode==0) return true; //TODO apdorojimai
			switch (inter.interruptCode)
			{//apdoroti interupto koda
				case 0: // TODO EXIT
					System.out.println("Program has finished successfully");
					return true;
					
				case 1: // TODO Trap FLag(menu)
					trapFlag(VM);
					break;
				case 2://TODO neatpazinta komanda
					System.out.println("Unknown command code");
					return true;
					//break;
				case 3://TODO dalyba is 0
					System.out.println("Division by zero");
					return true;
					//break;
				case 4://TODO perzengti adresacijos reziai
					System.out.println("Address doesn't exist");
					return true;
					//break;
				case 5://TODO in
					if (CH1.value == 1) ;//TODO
					else {
						CH1.value = 1;
						IN(inter.memAdress);
						CH1.value = 0;
					}
					break;
				case 6://TODO out
					if (CH2.value == 1) ;//TODO
					else {
						CH2.value = 1;
						OUT(inter.memAdress);
						CH2.value = 0;
					}
					break;
				case 7://TODO loadshare
					loadShr(inter.reg, inter.memAdress);
					break;
				case 8://TODO streshare
					storeShr(inter.reg, inter.memAdress);
					break;
				case 9://TODO  fopen
					disk = new Disk(AX, BX, CX, userMemory, supervMemory);
					//TODO kaip patikrint ar toks failas jau egzistuoja?
					break;
				case 10: //TODO fread
					//kaip suzinoti kokie operandai naudojami?
					ByteBuffer i = disk.fileRead(0, 0);
					break;
				case 11: //TODO fseek
					disk.fileRead(0,0); //kiek suzinoti kiek juda ir kaip pajudeti i kita puse?
					break;
				case 12://TODO Fclose
					//nera failo uzdarymo
					break;
				case 13://TODO Fdelete
					//ner failo panaikinimo
					break;
				case 14://TODO time
					//ka daryti cia?
					break;
			}
		}
	}
//Details for trap flag	
	private void StepDetails()
	{
		System.out.println("Main registers and their values");//TODO ar dar reiksmiu reikia?
		System.out.println("AX = "+AX.value);
		System.out.println("CX = "+CX.value);
		System.out.println("BX = "+BX.value);
		System.out.println("IP = "+IP.value);
		System.out.println("SF = "+SF.value);
	}
	private void trapFlag(Interpretator vm) {
		// TODO Auto-generated method stub
		String trapFlagMenu[] = {"trapflag","NextStep","Show details","change details","run"};
		String ChangeStepDetails[] = {"Choose which register you want to change","AX","BX","CX","IP","SF"};
		String ChangeStepDetailsValue[] = {"Change to what?"};

		//Scanner scanner = new Scanner(System.in);
		int i = meniu(trapFlagMenu);
		Interrupt inter = null;
		switch (i)
		{
		case 1://NextStep
			inter = vm.interpreting();
			break;
		case 2://Show details
			StepDetails();
			break;
		case 3://Change details
			int register = meniu(ChangeStepDetails);
			int value = meniu(ChangeStepDetailsValue);
			switch(register)
			{
			case 1:
				AX.value=value;
				break;
			case 2:
				BX.value = value;
				break;
			case 3:
				CX.value = value;
				break;
			case 4:
				IP.value = (byte) value;
				break;
			case 5:
				SF.value = (byte) value;
				break;
			}
			inter = vm.interpreting();
			
			break;
		case 4://run
			run(vm);
			break;
		
		}
		
	}

	public static void main(String[] args) {
		String Choose = "Choose a program to run";
		//TODO get file names to variable FileNames
		String FileNames ="file";
		String ChooseFileNames[] = {Choose,FileNames};
		int filenum;
		//filenum= meniu(ChooseFileNames);
		RM r = new RM();
		int s=0,d=0;
		Interpretator VM = null;
		try {
			VM = r.createVM("C:/Users/akazakova/Documents/GitHub/OS/Assembler/CodeBytes");
			//VM = r.createVM("C:/Users/Helch/Documents/GitHub/OS/Assembler/testProg02_OUT");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(int i=0; i<MAX_PAGES;i++) {
			if (r.pageUsed(i)) s++;
		}
		r.run(VM);
		//r.start();
		
		//TODO 
		r.destroyCurrentVM();
		for(int i=0; i<MAX_PAGES;i++) {
			if (r.pageUsed(i)) d++;
		}
		System.out.println(s);
		System.out.println(d);
	}
}
