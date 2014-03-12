def commandLength(codeLine):
    if codeLine[0].upper() in ('ADD', 'SUB', 'MUL', 'DIV', 'MOD', 'CMP', 'AND', 'OR'):
        if codeLine[2].isdigit():
            length = 2
        else:
            length = 1
    elif codeLine[0].upper() == 'DW':
        length =1
    elif codeLine[0].upper() == 'DM':
        length = codeLine[2]
    elif codeLine[0].upper() == 'DS':
        length = len(codeLine[2]) / 8
        if (len(codeLine[2]) % 8) !=0:
            length = length +1
    else:
        length = 1
    return length
