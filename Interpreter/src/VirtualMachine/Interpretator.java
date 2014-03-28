package VirtualMachine;



public class Interpretator {
	Reg8B AX, BX, CX;
	StatusFlag SF;
	RegB IP;					
	long[][] memory = null;

	public Interpretator(Reg8B a, Reg8B b, Reg8B c, StatusFlag s, RegB i, long[][] mem) {
		AX = a;
		BX = b;
		CX = c;
		SF = s;
		IP = i;
		memory = mem;
	}
	//TODO ar veikia indeksavimas su ribiniais skaiciais
	public static int index1(int i) {
		return (i & 0xFF)/16;
	}
	
	public static int index2(int i) {
		return (i & 0xFF)%16;
	}
	
//TODO interrupt del kreipimosi uz adresacijos ribu? 
	public boolean interpreting() throws Exception {
		Reg8B reg = null;
		long op2 = 0;
		long cmd = memory[index1(IP.value)][index2(IP.value)];
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
			if (op2 == 0) throw new Exception(); //TODO dalyba is 0
			else reg.value = reg.value / op2;
			break;
		//MOD--------------------------------+
		case 0x5:
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (op2 == 0) throw new Exception(); //TODO dalyba is 0
			else reg.value = reg.value % op2;
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
			reg.value = memory[index1(cmdB[3])][index2(cmdB[3])];		
			break;
		//STORE--------------------------------+
		case 0x8:	
			reg = getRegister(cmdB[3]);
			memory[index1(cmdB[2])][index2(cmdB[2])] = reg.value;
			break;
		//TODO LOADSHR--------------------------------??reik jau su pusliapiavimu turet?
		case 0x9:
			
			break;
		//TODO STORESHR--------------------------------??reik jau su pusliapiavimu turet?
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
		//TODO FOPEN--------------------------------?
		case 0xD:

			break;
		//TODO FREAD--------------------------------? netestuota, nesaugo kursoriaus
		case 0xE:
			
			break;
		//TODO FWRITE--------------------------------? netestuota, nesaugo kursoriaus
		case 0xF:	

			break;
		//TODO FSEEK--------------------------------
		case 0x10:	
			
			break;
		//TODO FCLOSE--------------------------------
		case 0x11:	
			
			break;
		//TODO FDELETE--------------------------------
		case 0x12:
			
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
			if (CX.value >= 0) {
				IP.value = cmdB[2];
			}
			break;
		default:
			return false;//TODO Error del nezinomos komandos ir interrupt?
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
			op2 = memory[index1(cmdB[3])][index2(cmdB[3])];
			break;
		// reg bet.op
		case 0x3:
			IP.value++;
			op2 = memory[index1(IP.value)][index2(IP.value)];
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
				break;         //TODO kazkaip apdoroti ar ka?
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
			memory[index1(cmdB+i)][index2(cmdB+i)] = mem;
		}
		for(int i=0; i<chars; i++) {
			b = (byte) input.charAt(words*8+i);
			mem = mem | (b << (7-i)*8);
		}
		memory[index1(cmdB+words)][index2(cmdB+words)] = mem;
	}
	
	
	public String readFromMemoryCX(byte cmdB) {
		String s = "";
		if (CX.value/8+(cmdB&0xFF) >= 256) {
			//TODO daugiau nei atmintis reiktu error
			//break;
		}
		long word = 0;
		int words =(int) CX.value/8;
		byte cha = 0;
		for(int i=0; i<words; i++) {
			word = memory[index1(cmdB+i)][index2(cmdB+i)];
			for(int j=0;j<8;j++) {
				cha = (byte) ((word >>>(7-j)*8)&0xFF);
				s+=((char)cha);
			}
		}
		int chars = (int) (CX.value%8);
		if (chars != 0) {
			word = memory[index1(cmdB+words)][index2(cmdB+words)];
			for(int i=0;i<chars;i++) {
				cha = (byte) ((word >>>(7-i)*8)&0xFF);
				s+=(char)cha;
			}
		}
		return s;
	}

}
