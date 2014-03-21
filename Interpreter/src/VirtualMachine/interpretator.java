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
	    AX = 0;
	    BX = 0;
	    CX = 0;
	    SF = 0;
	    IP = 0;
	}

	public static boolean Interpreting() {
		long cmd = memory[IP & 0xFF];
		byte[] cmdB = new byte[4];
		cmd = cmd >>> 32;
		for(int i = 0; i<4; i++){
			cmdB[3-i] = (byte)((cmd >>> 8*i) & 0xFF);
		}
		switch (cmdB[0]) {
		//ADD--------------------------------+	
		case 0x1:	
			long op2 = 0;	//blogai, bet tai trumpina koda	
			long res = 0; //irgi blogai
			switch (cmdB[1]) {
			// reg reg 
			case 0x1:
				switch(cmdB[3]){
				//AX
				case 0x1:
					op2 = AX;
					break;
				//BX
				case 0x2:
					op2 = BX;
					break;
				//CX
				case 0x3:
					op2 = CX;
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
			}
			switch(cmdB[2]){
			//AX
			case 0x1:
				res = AX + op2;
				if ((AX + op2)!= res) {
					SF = (byte)(SF | 1 << 6);
				}
				AX = res;
				break;
			//BX
			case 0x2:
				res = BX + op2;
				if ((BX + op2)!= res) {
					SF = (byte)(SF | 1 << 6);
				}
				BX = res;
				break;
			//CX
			case 0x3:
				res = CX + op2;
				if ((CX + op2)!= res) {
					SF = (byte)(SF | 1 << 6);
				}
				CX = res;
				break;
			}
			break;
		//SUB--------------------------------
		case 0x2:	
			
			break;
		//MUL--------------------------------			
		case 0x3:	
			
			break;
		//DIV--------------------------------
		case 0x4:
			
			break;
		//MOD--------------------------------
		case 0x5:
			
			break;
		//CMP--------------------------------
		case 0x6:	
					
			break;
		//LOAD--------------------------------+
		case 0x7:	
			switch(cmdB[2]){
			//AX
			case 0x1:	
				AX = memory[cmdB[3] & 0xFF];
				break;
			//BX
			case 0x2:
				BX = memory[cmdB[3] & 0xFF];
				break;
			//CX
			case 0x3:	
				CX = memory[cmdB[3] & 0xFF];
				break;
			}
			
			break;
		//STORE--------------------------------+
		case 0x8:	
			switch(cmdB[3]){
			//AX
			case 0x1:	
				memory[cmdB[2] & 0xFF] = AX;
				break;
			//BX
			case 0x2:	
				memory[cmdB[2] & 0xFF] = BX;
				break;
			//CX							
			case 0x3:	
				memory[cmdB[2] & 0xFF] = CX;									
				break;
			}
			
			break;
		//LOADSHR--------------------------------
		case 0x9:
			
			break;
		//STORESHR--------------------------------
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
		//AND--------------------------------
		case 0x19:
			
			break;
		//OR--------------------------------
		case 0x1A:
			
			break;
		//NOT--------------------------------+
		case 0x1B:
			switch (cmdB[2]) {
			//AX
			case 0x1:
				AX = ~AX;
				break;
			//BX	
			case 0x2:
				BX = ~BX;
				break;
			//CX
			case 0x3:
				CX = ~CX;
				break;
			}
			break;
		//LOOP--------------------------------+
		case 0x1C:
			CX--;
			if (CX != 0) {
				IP = cmdB[2];
			}
			break;
		}
		IP++;
		return true;
	}

	public static void main(String[] args) {
		Interpretator();
		boolean a = true;
		while (a) {
			a = Interpreting();
		}
	}

}
