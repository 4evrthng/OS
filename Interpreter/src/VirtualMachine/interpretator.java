package VirtualMachine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File; //ar imports gerai os.. like.. idk,k?..
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;



public class Interpretator {
	Reg8B AX, BX, CX;
	StatusFlag SF;
	RegB IP;					
//	long[] memory = new long[256];
	long[][] memory = new long[16][16];
	File[] f = new File[1]; //laikina, failo atidarymui ir naudojimui
	
																			
	/* Path file = ...;
byte[] fileArray;
fileArray = Files.readAllBytes(file);*/								/*test perkelti tai i RM +-
	public Interpretator() {		
		File pFile = new File("C:/Users/Helch/Desktop/testProg02_OUT");
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
	    AX = new Reg8B();
	    BX = new Reg8B();
	    CX = new Reg8B();
	    SF = new StatusFlag();
	    IP = new RegB();
	}
	*/
	public Interpretator(Reg8B a, Reg8B b, Reg8B c, StatusFlag s, RegB i, long[][] mem) {
		AX = a;
		BX = b;
		CX = c;
		SF = s;
		IP = i;
		memory = mem;
	}
	
	
	
// interrupt del kreipimosi uz adresacijos ribu? 
	public boolean interpreting() {
		Reg8B reg = null;
		long op2 = 0;
		long cmd = memory[(IP.value & 0xFF)/16][(IP.value & 0xFF)%16];
		byte[] cmdB = new byte[4];
		cmd = cmd >>> 32;
		for(int i = 0; i<4; i++){
			cmdB[3-i] = (byte)((cmd >>> 8*i) & 0xFF);
		}
		switch (cmdB[0]) {
		//ADD--------------------------------+
		case 0x1:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			ADD(reg, op2);
			break;
		//SUB--------------------------------+
		case 0x2:	
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			ADD(reg, -op2);
			break;
		//MUL--------------------------------+			
		case 0x3:	
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			MUL(reg, op2);
			break;
		//DIV--------------------------------+
		case 0x4:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			reg.value = reg.value / op2;
			break;
		//MOD--------------------------------+
		case 0x5:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			reg.value = reg.value % op2;
			break;
		//CMP--------------------------------+
		case 0x6:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			CMP(reg, op2);
			break;
		//LOAD--------------------------------+
		case 0x7:
			reg = getRegister(cmdB[2]);
			reg.value = memory[(cmdB[3] & 0xFF)/16][(cmdB[3] & 0xFF)%16];		
			break;
		//STORE--------------------------------+
		case 0x8:	
			reg = getRegister(cmdB[3]);
			memory[(cmdB[2] & 0xFF)/16][(cmdB[2] & 0xFF)%16] = reg.value;
			break;
		//LOADSHR--------------------------------??reik jau su pusliapiavimu turet?
		case 0x9:
			
			break;
		//STORESHR--------------------------------??reik jau su pusliapiavimu turet?
		case 0xA:	
			
			break;
		//IN--------------------------------?
		case 0xB://!!!!!!!!!!!!kanalo pra6yti turi	
			IN(cmdB[2]);
			break;
		//OUT--------------------------------+
		case 0xC://!!!!!!!!!!!!kanalo pra6yti turi
			System.out.println(readFromMemoryCX(cmdB[2]));
			break;
		//FOPEN--------------------------------?
		case 0xD:	
			String path = "";
			int i,j=0;
			byte cha = 0;
			long word = 0;
			while ((cha != 10)|((cmdB[2]&0xFF)+j!=256)) {
				word = memory[((cmdB[2] & 0xFF)+j)/16][((cmdB[2] & 0xFF)+j)%16];
				i = 0;
				while((cha!=10)||(i<8)) {
					cha = (byte) ((word >>>(7-i)*8)&0xFF);
					if (cha!=10) path+=(char)cha; //ar butina per kastinimus?
				}
				j++;
			}
			File pFile = new File(path); // reiktu i6saugoti i rm masyva
			i = 0; //laikina, failo atidarymui ir naudojimui
			f[i] = pFile; //laikina, failo atidarymui ir naudojimui
			BX.value = i; //laikina, failo atidarymui ir naudojimui
			break;
		//FREAD--------------------------------? netestuota, nesaugo kursoriaus
		case 0xE:	
			if (CX.value/8+(cmdB[2]&0xFF) >= 256) {
				//daugiau nei atmintis reiktu error
				//break;
			}
			String s = "";
			int c=0;
			BufferedReader reader;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(f[(int)BX.value])));
			} catch (FileNotFoundException e) {
				//error?
				break;
			}
			try {
				while ((s.length()<CX.value)||((c = reader.read()) != -1)) {
					s += (char) c;
				}
			} catch (IOException e) {
				//error?
			}
			AX.value = s.length();
			writeToMemoryAX(cmdB[2], s);
			break;
		//FWRITE--------------------------------? netestuota, nesaugo kursoriaus
		case 0xF:	
			String s1 = readFromMemoryCX(cmdB[2]);//what, that's dumb of me
			BufferedWriter writer;
			try {
				writer = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(f[(int)BX.value])));
			} catch (FileNotFoundException e) {
				//error
			}
			break;
		//FSEEK--------------------------------
		case 0x10:	
			
			break;
		//FCLOSE--------------------------------
		case 0x11:	
		//f[(int)BX.value]; i masyva sudeti ne FILE, o scanner/writter? streamus
			break;
		//FDELETE--------------------------------
		case 0x12:	
			f[(int)BX.value].delete();
			break;
		//JMP--------------------------------+
		case 0x13:	
			IP.value = cmdB[2];
			break;
		//JE--------------------------------+
		case 0x14:
			if (SF.getZF()) {
				IP.value = cmdB[2];
			}
			break;
		//JNE--------------------------------+
		case 0x15:
			if (!SF.getZF()) {
				IP.value = cmdB[2];
			}
			break;
		//JL--------------------------------+
		case 0x16: //OF != SF arba CF = 1
			if ((SF.getOF() != SF.getSF()) || SF.getCF()) {
				IP.value = cmdB[2];
			}
			break;
		//JS--------------------------------+
		case 0x17:	 // jei OF = SF , arba CF = 0, bet ZF != 1.
			if ((SF.getOF() == SF.getSF()) || (SF.getCF() && !SF.getZF())) {
				IP.value = cmdB[2];
			}
			break;
		//EXIT--------------------------------+
		case 0x18:
			return false;
		//AND--------------------------------+
		case 0x19:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			reg.value = reg.value & op2;
			break;
		//OR--------------------------------+
		case 0x1A:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			reg.value = reg.value | op2;
			break;
		//NOT--------------------------------+
		case 0x1B:
			reg = getRegister(cmdB[2]);
			reg.value = ~reg.value;
			break;
		//LOOP--------------------------------+
		case 0x1C:
			CX.value--;
			if (CX.value != 0) {
				IP.value = cmdB[2];
			}
			break;
		default:
			return false;//Error del nezinomos komandos ir interrupt?
			//break;
		} 
		IP.value++;
		if ((IP.value&0xFF) == 0) return false;
		return true;
	}
	

	public Reg8B getRegister(byte cmdB) {
		Reg8B reg = null;
		switch(cmdB){
		//AX
		case 0x1:
			reg = AX;
			break;
		//BX
		case 0x2:
			reg = BX;
			break;
		//CX
		case 0x3:
			reg = CX;
			break;
		}
		return reg;
	}
	
	public long getOperand2(byte[] cmdB) {
		long op2 = 0;
		switch (cmdB[1]) {
		// reg
		case 0x1:
			op2 = getRegister(cmdB[3]).value;
			break;
		//  atm
		case 0x2:
			op2 = memory[(cmdB[3] & 0xFF)/16][(cmdB[3] & 0xFF)%16];
			break;
		// reg bet.op
		case 0x3:
			IP.value++;
			op2 = memory[(IP.value & 0xFF)/16][(IP.value & 0xFF)%16];
			break;
		}
		return op2;		
	}

	
	public void ADD(Reg8B reg, long op2) {		
		long res = reg.value + op2;
		if ((reg.value + op2)!= res) SF.setCF(); //CF
		else SF.clearCF();		
		reg.value = res;
	}
	
	public void MUL(Reg8B reg, long op2) {
		long res = reg.value * op2;
		if ((reg.value + op2)!= res) SF.setCF(); //CF
		else SF.clearCF();	
		reg.value = res;
	}
	
	public void CMP(Reg8B reg, long op2) {
		long res = reg.value - op2;
		
		if (reg.value == op2) SF.setZF();
		else SF.clearZF();
	
		if (reg.value < op2) SF.setCF();
		else SF.clearCF();
		
		if (((op2 >>> 31) != (res >>> 31))&&((reg.value >>> 31) == (op2 >>> 31))) 
			SF.setOF(); //OF
		else SF.clearOF();
		
		if ((res >>> 31) == 1) SF.setSF();
		else SF.clearSF();	
	}
	
	
	public void IN(byte cmdB) {
		if (CX.value/8+(cmdB&0xFF) >= 256) {
			//daugiau nei atmintis reiktu error
			//break;
		}
		char c;
		String input = "";
		AX.value = 0;
		BufferedReader br = new	BufferedReader(new InputStreamReader(System.in));
		for(int i=0; i<CX.value;i++) {
			try {
				c = (char) br.read();
			} catch (IOException e) {
				break;         ///kazkaip apdoroti ar ka?
			}
			input +=c;
			AX.value++;
		}
		writeToMemoryAX(cmdB, input);
	}
	
	public void writeToMemoryAX(byte cmdB, String input) {
		long mem = 0;
		byte b;
		int words = (int) AX.value/8;
		int chars = (int) AX.value%8;
		for(int i=0; i<words; i++) {
			mem = 0;
			for(int j=0; j<8; j++) {
				b = (byte) input.charAt(i*8+j);
				mem = mem | (b << (7-j)*8);
			}
			memory[((cmdB&0xFF)+i)/16][((cmdB&0xFF)+i)%16] = mem;
		}
		for(int i=0; i<chars; i++) {
			b = (byte) input.charAt(words*8+i);
			mem = mem | (b << (7-i)*8);
		}
		memory[((cmdB&0xFF)+words)/16][((cmdB&0xFF)+words)%16] = mem;
	}
	
	
	public String readFromMemoryCX(byte cmdB) {
		String s = "";
		if (CX.value/8+(cmdB&0xFF) >= 256) {
			//daugiau nei atmintis reiktu error
			//break;
		}
		long word = 0;
		int words =(int) CX.value/8;
		byte cha = 0;
		for(int i=0; i<words; i++) {
			word = memory[((cmdB&0xFF)+i)/16][((cmdB&0xFF)+i)%16];
			for(int j=0;j<8;j++) {
				cha = (byte) ((word >>>(7-j)*8)&0xFF);
				s+=((char)cha);
			}
		}
		int chars = (int) (CX.value%8);
		if (chars != 0) {
			word = memory[((cmdB&0xFF)+words)/16][((cmdB&0xFF)+words)%16];
			for(int i=0;i<chars;i++) {
				cha = (byte) ((word >>>(7-i)*8)&0xFF);
				s+=(char)cha;
			}
		}
		return s;
	}

}
