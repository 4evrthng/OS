import commandLength
import struct

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
	
codeText = open ('C:/Users/Roman/Documents/GitHub/OS/Assembler/testProg01.txt', 'r')
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
	else:
		position += commandLength.commandLength(codeLine)

# Second pass determines the machine code and appends it to the assembledCode
assembledCode = bytearray()

for codeLine in codeLineList:
	#Commands that may take 1 or 2 words
	if codeLine[0].upper() in ('ADD', 'SUB', 'MUL', 'DIV', 'MOD', 'CMP', 'AND', 'OR'):
		assembledCode.append( commands[codeLine[0].upper()] )
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
			#Second operand in memory
			variableAddress = -1
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
				#ERROR	
		assembledCode.extend((0x00, 0x00, 0x00, 0x00))	
		#Append a number operand
		if codeLine[2].isdigit():
			hexString =  struct.pack('>Q', long(codeLine[2]))
			#if len(hexString) > 8:
				#ERROR
			for i in range(0, 8 - len(hexString)):
				assembledCode.append(0x00)
			for i in range(0, len(hexString)):
				assembledCode.append( hexString[i])
	
	#Commands with no operands
	if codeLine[0].upper() in ('FSEEK', 'FLOSE', 'FDELETE', 'EXIT'):
		assembledCode.append( commands[codeLine[0].upper()] )
		assembledCode.extend((0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
		
	#Commands with one operand in the memory
	if codeLine[0].upper() in ('IN', 'OUT', 'FOPEN', 'FREAD', 'FWRITE'):
		assembledCode.append( commands[codeLine[0].upper()] )
		assembledCode.append(0x00)
		#Find the address of the variable
		variableAddress = -1
		for variable in variables:
			if codeLine[1] == variable.name:
				variableAddress = variable.address
				#Append the address of the variable
				assembledCode.append( variableAddress)
				break
		#if variableAddress == -1:
			#ERROR codeLine[1] is not a variable
		assembledCode.extend((0x00, 0x00, 0x00, 0x00, 0x00))
		
	#Commands with one label operand
	if codeLine[0].upper() in ('JMP', 'JE', 'JNE', 'JL', 'JS', 'LOOP'):
		assembledCode.append( commands[codeLine[0].upper()] )
		assembledCode.append(0x00)
		#Find the address of the label
		labelAddress = -1
		for label in labels:
			if codeLine[1] == label.name:
				labelAddress = label.address
				#Append the address of the label
				assembledCode.append( labelAddress)
				break
		#if labelAddress == -1:
			#ERROR codeLine[1] is not a label
		assembledCode.extend((0x00, 0x00, 0x00, 0x00, 0x00))
			
	#Command LOAD op1 register op2 memory
	if codeLine[0].upper() in ('LOAD'):
		assembledCode.append( commands[codeLine[0].upper()] )
		assembledCode.append(0x00)
		#if codeLine[1].upper() not in ('AX', 'BX', 'CX'):
			#ERROR first operand must be a register
		assembledCode.append( registers[ codeLine[1].upper()])
		#Find the address of the variable
		variableAddress = -1
		for variable in variables:
			if codeLine[2] == variable.name:
				variableAddress = variable.address
				#Append the address of the variable
				assembledCode.append( variableAddress)
				break
		#if variableAddress == -1:
			#ERROR codeLine[1] is not a variable
		assembledCode.extend((0x00, 0x00, 0x00, 0x00))
		
	#Command STORE op1 memory op2 register
	if codeLine[0].upper() in ('STORE'):
		assembledCode.append( commands[codeLine[0].upper()] )
		assembledCode.append(0x00)
		variableAddress = -1
		#Find the address of the variable
		for variable in variables:
			if codeLine[1] == variable.name:
				variableAddress = variable.address
				#Append the address of the variable
				assembledCode.append( variableAddress)
				break
		#if variableAddress == -1:
			#ERROR codeLine[1] is not a variable
		#if codeLine[1].upper() not in ('AX', 'BX', 'CX'):
			#ERROR first operand must be a register
		assembledCode.append( registers[ codeLine[2].upper()])
		assembledCode.extend((0x00, 0x00, 0x00, 0x00))
	
	#DW
	if codeLine[0].upper() in ('DW'):
		if len(codeLine) == 2:
			assembledCode.extend((0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00))
		elif codeLine[2].isdigit():
			hexString =  struct.pack('>Q', long(codeLine[2]))
			#if len(hexString) > 8:
				#ERROR
			for i in range(0, 8 - len(hexString)):
				assembledCode.append(0x00)
			for i in range(0, len(hexString)):
				assembledCode.append( hexString[i])
		#else:
			#ERROR
		
	
	
	
	
codeBytes = open('codeBytes', 'wb')
codeBytes.write(assembledCode)
codeBytes.close()
