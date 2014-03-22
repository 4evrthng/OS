package VirtualMachine;

import java.io.File; //ar imports gerai os.. like.. idk,k?..
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;



public class interpretator {
	static reg8B AX, BX, CX;
	static byte SF, IP;					//SF galima iskirti kaip klase ir tada metodus kiekvienam flag'ui
	static long[] memory = new long[256];
	
	
	
	
	public interpretator() {		
		File pFile = new File("C:/Users/Helch/Desktop/codeBytes");
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
	    AX = new reg8B();
	    BX = new reg8B();
	    CX = new reg8B();
	    SF = 0;
	    IP = 0;
	}
	
	
	
// interrupt del kreipimosi uz adresacijos ribu? 
	public boolean interpreting() {
		long cmd = memory[IP & 0xFF];
		byte[] cmdB = new byte[4];
		cmd = cmd >>> 32;
		for(int i = 0; i<4; i++){
			cmdB[3-i] = (byte)((cmd >>> 8*i) & 0xFF);
		}
		switch (cmdB[0]) {
		//ADD--------------------------------+
		case 0x1:	
			aritLog(cmdB);
			break;
		//SUB--------------------------------+
		case 0x2:	
			aritLog(cmdB);
			break;
		//MUL--------------------------------+			
		case 0x3:	
			aritLog(cmdB);
			break;
		//DIV--------------------------------+
		case 0x4:
			aritLog(cmdB);
			break;
		//MOD--------------------------------+
		case 0x5:
			aritLog(cmdB);
			break;
		//CMP--------------------------------+
		case 0x6:	
			aritLog(cmdB);		
			break;
		//LOAD--------------------------------+
		case 0x7:	
			switch(cmdB[2]){
			//AX
			case 0x1:	
				AX.value = memory[cmdB[3] & 0xFF];
				break;
			//BX
			case 0x2:
				BX.value = memory[cmdB[3] & 0xFF];
				break;
			//CX
			case 0x3:	
				CX.value = memory[cmdB[3] & 0xFF];
				break;
			}
			
			break;
		//STORE--------------------------------+
		case 0x8:	
			switch(cmdB[3]){
			//AX
			case 0x1:	
				memory[cmdB[2] & 0xFF] = AX.value;
				break;
			//BX
			case 0x2:	
				memory[cmdB[2] & 0xFF] = BX.value;
				break;
			//CX							
			case 0x3:	
				memory[cmdB[2] & 0xFF] = CX.value;									
				break;
			}
			
			break;
		//LOADSHR--------------------------------??reik jau su pusliapiavimu turet?
		case 0x9:
			
			break;
		//STORESHR--------------------------------??reik jau su pusliapiavimu turet?
		case 0xA:	
			
			break;
		//IN--------------------------------
		case 0xB:	
			
			break;
		//OUT--------------------------------
		case 0xC:	
			
			break;
		//FOPEN--------------------------------
		case 0xD:	
			
			break;
		//FREAD--------------------------------
		case 0xE:	
			
			break;
		//FWRITE--------------------------------
		case 0xF:	
			
			break;
		//FSEEK--------------------------------
		case 0x10:	
			
			break;
		//FCLOSE--------------------------------
		case 0x11:	
			
			break;
		//FDELETE--------------------------------
		case 0x12:	
			
			break;
		//JMP--------------------------------+
		case 0x13:	
			IP = cmdB[2];
			break;
		//JE--------------------------------+
		case 0x14:
			if (0x1 == ((SF >>> 4)&0xF)) {
				IP = cmdB[2];
			}
			break;
		//JNE--------------------------------+
		case 0x15:
			if (0x0 == ((SF >>> 4)&0xF)) {
				IP = cmdB[2];
			}
			break;
		//JL--------------------------------+
		case 0x16: //OF != SF arba CF = 1
			if (((SF&0xF) != ((SF >>> 2)&0xF))||(0x1 == ((SF >>> 6)&0xF))) {
				IP = cmdB[2];
			}
			break;
		//JS--------------------------------+
		case 0x17:	 // jei OF = SF , arba CF = 0, bet ZF != 1.
			if (((SF&0xF) == ((SF >>> 2)&0xF))||((0x0 == ((SF >>> 6)&0xF))&(0x1 != ((SF >>> 4)&0xF)))) {
				IP = cmdB[2];
			}
			break;
		//EXIT--------------------------------+
		case 0x18:
			return false;
		//AND--------------------------------+
		case 0x19:
			aritLog(cmdB);
			break;
		//OR--------------------------------+
		case 0x1A:
			aritLog(cmdB);
			break;
		//NOT--------------------------------+
		case 0x1B:
			switch (cmdB[2]) {
			//AX
			case 0x1:
				AX.value = ~AX.value;
				break;
			//BX	
			case 0x2:
				BX.value = ~BX.value;
				break;
			//CX
			case 0x3:
				CX.value = ~CX.value;
				break;
			}
			break;
		//LOOP--------------------------------+
		case 0x1C:
			CX.value--;
			if (CX.value != 0) {
				IP = cmdB[2];
			}
			break;
		default:
			return false;//Error del nezinomos komandos ir interrupt?
			//break;
		}
		IP++;
		return true;
	}
	
	
	
	
	public static void aritLog(byte[] cmdB) {
		long op2 = 0;
		long res = 0;
		reg8B reg = null;
		//nustato antra operanda
		switch (cmdB[1]) {
		// reg reg 
		case 0x1:
			switch(cmdB[3]){
			//AX
			case 0x1:
				op2 = AX.value;
				break;
			//BX
			case 0x2:
				op2 = BX.value;
				break;
			//CX
			case 0x3:
				op2 = CX.value;
				break;
			}
			break;
		// reg atm
		case 0x2:
			op2 = memory[cmdB[3]&0xFF];
			break;
		// reg bet.op
		case 0x3:
			IP++;
			op2 = memory[IP & 0xFF];
			break;
		}
		switch(cmdB[2]){
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
		switch (cmdB[0]) {
		case 0x1: //ADD
			res = reg.value + op2;
			if ((reg.value + op2)!= res) {
				SF = (byte)(SF | 1 << 6); //CF
			}
			reg.value = res;
			break;
		case 0x2: //SUB
			res = reg.value - op2;
			reg.value = res;
			break;
		case 0x3: //MUL
			res = reg.value * op2;
			if ((reg.value * op2)!= res) {
				SF = (byte)(SF | 1 << 6); //CF
			}
			reg.value = res;
			break;
		case 0x4: //DIV
			res = reg.value / op2;
			reg.value = res;
			break;
		case 0x5: //MOD
			res = reg.value % op2;
			reg.value = res;
			break;
		case 0x6: //CMP
			res = reg.value - op2;
			if (reg.value == op2) {
				SF = (byte)(SF | 1 << 4); //ZF
			}
			if (reg.value < op2) {
				SF = (byte)(SF | 1 << 6); //CF
			}
			if (((op2 >>> 31) != (res >>> 31))&&((reg.value >>> 31) == (op2 >>> 31))) {
				SF = (byte)(SF | 1 << 0); //OF
			}
			if ((res >>> 31) == 1) {
				SF = (byte)(SF | 1 << 2); //SF
			}
			break;
		case 0x19: //AND
			res = reg.value & op2;
			reg.value = res;
			break;
		case 0x1A: //OR
			res = reg.value | op2;
			reg.value = res;
			break;		
		}
	}

	
	
	
	public static void main(String[] args) {
		interpretator VM = new interpretator();
		boolean a = true;
		while (a) {
			a = VM.interpreting();
		}
		System.out.println();
	}

}
