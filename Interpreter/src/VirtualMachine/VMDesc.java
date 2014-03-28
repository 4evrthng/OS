package VirtualMachine;

public class VMDesc {
	long AX, BX, CX;
	byte SF, IP, PTR;
	
	public VMDesc(Interpretator vm, RegB ptr) {
		AX = vm.AX.value;
		BX = vm.BX.value;
		CX = vm.CX.value;
		SF = vm.SF.value;
		IP = vm.IP.value;
		PTR = ptr.value;
	}
}
