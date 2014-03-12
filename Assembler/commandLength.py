def commandLength(codeLine):
    if codeLine[0].upper() in ('ADD', 'SUB', 'MUL', 'DIV', 'MOD', 'CMP', 'AND', 'OR'):
        if codeLine[3].isdigit():
            length = 2
        else:
            length = 1
    else:
        length = 1
    return length
        
