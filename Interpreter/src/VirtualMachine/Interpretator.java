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
	
	//pagal adresa randa bloko numeri
	public static int block(int i) {
		return (i & 0xFF)/16;
	}
	
	//pagal adresa randa zodzio numeri bloke
	public static int word(int i) {
		return (i & 0xFF)%16;
	}
	
//TODO interrupt del netinkamos komandos, kai atpazista komanda, bet neranda ar registru ar 
	//TODO test test test
	public Interrupt interpreting() {
		Reg8B reg = null;
		long op2 = 0;
		long cmd = memory[block(IP.value)][word(IP.value)];
		IP.value++;
		if (IP.value == 0) return new Interrupt(4);
		byte[] cmdB = new byte[4];
		cmd = cmd >>> 32;
		for(int i = 0; i<4; i++){
			cmdB[3-i] = (byte)((cmd >>> 8*i) & 0xFF);
		}
		switch (cmdB[0]) {
		//ADD--------------------------------+
		case 0x1:
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			ADD(reg, op2);
			break;
		//SUB--------------------------------+
		case 0x2:	
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			ADD(reg, -op2);
			break;
		//MUL--------------------------------+			
		case 0x3:	
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			MUL(reg, op2);
			break;
		//DIV--------------------------------+
		case 0x4:
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			if (op2 == 0) return new Interrupt(3);
			else reg.value = reg.value / op2;
			break;
		//MOD--------------------------------+
		case 0x5:
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			if (op2 == 0) return new Interrupt(3);
			else reg.value = reg.value % op2;
			break;
		//CMP--------------------------------+
		case 0x6:
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			CMP(reg, op2);
			break;
		//LOAD--------------------------------+
		case 0x7:
			reg = getRegister(cmdB[2]);
			if (reg==null) return new Interrupt(2);
			reg.value = memory[block(cmdB[3])][word(cmdB[3])];		
			break;
		//STORE--------------------------------+
		case 0x8:	
			reg = getRegister(cmdB[3]);
			if (reg==null) return new Interrupt(2);
			memory[block(cmdB[2])][word(cmdB[2])] = reg.value;
			break;
		//LOADSHR--------------------------------
		case 0x9:
			reg = getRegister(cmdB[2]);
			if (reg==null) return new Interrupt(2);
			return new Interrupt(7, cmdB[3]&0xFF, reg);
		//STORESHR--------------------------------
		case 0xA:	
			reg = getRegister(cmdB[3]);
			if (reg==null) return new Interrupt(2);
			return new Interrupt(8, cmdB[2]&0xFF, reg);
		//IN--------------------------------
		case 0xB:
			return new Interrupt(5, cmdB[2]&0xFF);
		//OUT--------------------------------
		case 0xC:
			return new Interrupt(6, cmdB[2]&0xFF);
		//FOPEN--------------------------------?
		case 0xD:
			return new Interrupt(9, cmdB[2]&0xFF);
		//FREAD--------------------------------
		case 0xE:
			return new Interrupt(10, cmdB[2]&0xFF);
		//FWRITE--------------------------------
		case 0xF:	
			return new Interrupt(11, cmdB[2]&0xFF);
		//FSEEK--------------------------------
		case 0x10:	
			return new Interrupt(12);
		//FCLOSE--------------------------------
		case 0x11:	
			return new Interrupt(13);
		//FDELETE--------------------------------
		case 0x12:
			return new Interrupt(14);
		//JMP--------------------------------+
		case 0x13:	
			IP.value = cmdB[2];
			break;
		//JE--------------------------------+
		case 0x14:
			if (SF.getZF()) IP.value = cmdB[2];
			break;
		//JNE--------------------------------+
		case 0x15:
			if (!SF.getZF()) IP.value = cmdB[2];
			break;
		//JL--------------------------------+
		case 0x16: //OF != SF arba CF = 1
			if ((SF.getOF() != SF.getSF()) || SF.getCF()) 
				IP.value = cmdB[2];
			break;
		//JS--------------------------------+
		case 0x17:	 // jei OF = SF , arba CF = 0, bet ZF != 1.
			if ((SF.getOF() == SF.getSF()) || (SF.getCF() && !SF.getZF()))
				IP.value = cmdB[2];
			break;
		//EXIT--------------------------------+
		case 0x18:
			return new Interrupt(0);
		//AND--------------------------------+
		case 0x19:
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			reg.value = reg.value & op2;
			break;
		//OR--------------------------------+
		case 0x1A:
			if (!isByteDefined(cmdB[2])) return new Interrupt(2);
			if ((cmdB[1] == 1)&&!isByteDefined(cmdB[3])) return new Interrupt(2);
			reg = getRegister(cmdB[2]);
			op2 = getOperand2(cmdB);
			if (IP.value == 0) return new Interrupt(4);
			reg.value = reg.value | op2;
			break;
		//NOT--------------------------------+
		case 0x1B:
			reg = getRegister(cmdB[2]);
			if (reg==null) return new Interrupt(2);
			reg.value = ~reg.value;
			break;
		//LOOP--------------------------------+
		case 0x1C:
			CX.value--;
			if (CX.value >= 0) IP.value = cmdB[2];
			break;
		default:
			return new Interrupt(2);
		}
		if (SF.getTF()) return new Interrupt(1);
		return null;
	}
	
	//Ar baitas nurodo registra ar antro operando tipa.
	private boolean isByteDefined(byte b) {
		if ((b==1)||(b==2)|(b==3)) return true;
		return false;
}
	//Grazina Reg8B objekta, kuri atitinka baitas
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
	
	//Grazina long, kuri atitinka priklausomai nuo antro baito ar atmintis/registras 3 baite, ar sekanciame zodyje bet.op.
	public long getOperand2(byte[] cmdB) {
		long op2 = 0;
		switch (cmdB[1]) {
		// reg
		case 0x1:
			op2 = getRegister(cmdB[3]).value;
			break;
		//  atm
		case 0x2:
			op2 = memory[block(cmdB[3])][word(cmdB[3])];
			break;
		// reg bet.op
		case 0x3:
			op2 = memory[block(IP.value)][word(IP.value)];
			IP.value++;
			break;
		}
		return op2;		
	}

	//Sudeda 2 skaicius, talpina rezultata reg ir uzsetina CF
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
			SF.setOF();
		else SF.clearOF();
		
		if ((res >>> 31) == 1) SF.setSF();
		else SF.clearSF();	
	}
	
	

}
