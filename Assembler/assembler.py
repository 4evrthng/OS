import commandLenght

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
	'DW'			: 0	,
	'DB'			: 0	,
	'DS'			: 0	,
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

codeText = open ('C:/Users/Roman/Documents/GitHub/OS/Assembler/testProg01.txt', 'r')
codeLineList = codeText.readlines()

while '\n' in codeLineList: codeLineList.remove('\n')
codeLineList = [codeLine.split() for codeLine in codeLineList]

variables = []
labels = []
	
position = 0
for codeLine in codeLineList:
	if codeLine[0][0] == '@':
		newLabel = Label()
		newLabel.name = codeLine[0]
		newLabel.address = position
		labels.append(newLabel)
	elif codeLine[0].upper() in ['DW', 'DS', 'DB']:
		newVariable = Variable()
		newVariable.name = codeLine[1]
		newVariable.address = position
		variables.append(newVariable)
		position += commandLenght.commandLenght(codeLine)
	
print position	