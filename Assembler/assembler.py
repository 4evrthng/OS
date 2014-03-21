import commandLength

class Command:
	commandCode = 0
	op2Type = 0
	op1 = 0
	op2 = 0
	directOp = 0
	value = 0
	lenght = 0
	
class Variable:
	name = 0
	address = 0

class Label:
	name = 0
	address = 0

commands = {
	'DW'			: 0 ,
	'DB'			: 0 ,
	'DS'			: 0 ,
	'ADD'			: 1 ,
	'SUB'         	: 2 ,
	'MUL'         	: 3 ,
	'DIV'         	: 4 ,
	'MOD'         	: 5 ,
	'CMP'         	: 6 ,
	'LOAD'        	: 7 ,
	'STORE'       	: 8 ,
	'LOADSHR'     	: 9 ,
	'STORESHR'    	: 10,
	'IN'          	: 11,
	'OUT'         	: 12,
	'FOPEN'       	: 13,
	'FREAD'       	: 14,
	'FWRITE'      	: 15,
	'FSEEK'       	: 16,
	'FCLOSE'      	: 17,
	'FDELETE'     	: 18,
	'JMP'         	: 19,
	'JE'          	: 20,
	'JNE'         	: 21,
	'JL'          	: 22,
	'JS'          	: 23,
	'EXIT'       	: 24,
	'AND'         	: 25,
	'OR'          	: 26,
	'NOT'         	: 27,
	'LOOP'        	: 28
}

registers = {
	'AX' : 1,
	'BX' : 2,
	'CX' : 3
}
	
codeText = open ('C:/Users/Helch/Documents/GitHub/OS/Assembler/testProg01.txt', 'r')
codeLineList = codeText.readlines()

while '\n' in codeLineList: codeLineList.remove('\n')
codeLineList = [codeLine.split() for codeLine in codeLineList]

# First pass finds labels and variables in the code and saves their names and addresses
variables = []
labels = []
	
position = 0
for codeLine in codeLineList:
    # Finds labels
	if codeLine[0][0] == '@':
		newLabel = Label()
		newLabel.name = codeLine[0]
		newLabel.address = position
		labels.append(newLabel)
	# Finds variables
	elif codeLine[0].upper() in ['DW', 'DS', 'DB']:
		newVariable = Variable()
		newVariable.name = codeLine[1]
		newVariable.address = position
		variables.append(newVariable)
		position += commandLength.commandLength(codeLine)

# Second pass determines the machine code and appends it to the assembledCode
assembledCode = bytearray()

for codeLine in codeLineList:
	#Commands that may take 1 or 2 words
	if codeLine[0].upper() in ('ADD', 'SUB', 'MUL', 'DIV', 'MOD', 'CMP', 'AND', 'OR'):
		assembledCode.append( commands[codeLine[0].upper()] )
		variableAddress = -1
		#Second operand is a register
		if codeLine[2].upper() in ('AX', 'BX', 'CX'):
			assembledCode.append(0x01)
			#Append the code of the first operand
			assembledCode.append( registers[ codeLine[1].upper()])
			#Append the code of the second operand
			assembledCode.append( registers[ codeLine[2].upper()])
		#Second operand is a number
		elif codeLine[2].isdigit():
			assembledCode.append(0x03)
			#Append the code of the first operand
			assembledCode.append( registers[ codeLine[1].upper()])
			#Append the code of the second operand
			assembledCode.append(0x00)
		else:
			#Second operand is a variable
			for variable in variables:
				if codeLine[2] == variable.name:
					variableAddress = variable.address
					assembledCode.append(0x02)
					#Append the code of the first operand
					assembledCode.append( registers[ codeLine[1].upper()])
					#Append the code of the second operand
					assembledCode.append( variableAddress)
					break
			#if variableAddress == -1:
				#UNRECOGNISED COMMAND
		
		assembledCode.extend((0x00, 0x00))
		
		
		
codeBytes = open('codeBytes', 'wb')
codeBytes.write(assembledCode)
codeBytes.close()
