package VirtualMachine;

public class Interrupt {
	int interruptCode; //make it public?
	Reg8B reg = null;
	int memAdress = -1;
	
	public Interrupt(int code, int adress) {
		this.interruptCode = code;
		this.memAdress = adress;
	}
	
	public Interrupt(int code, int adress, Reg8B r) {
		this.interruptCode = code;
		this.memAdress = adress;
		this.reg = r;
	}

	public Interrupt(int code) {
		this.interruptCode = code;
	}

}
