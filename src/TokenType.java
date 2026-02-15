package src;

public enum TokenType {
    // Keywords [cite: 33]
    KEYWORD,
    
    // Identifiers [cite: 34]
    IDENTIFIER,
    
    // Literals [cite: 38, 40, 44, 47, 49]
    INTEGER_LITERAL,
    FLOAT_LITERAL,
    STRING_LITERAL,
    CHAR_LITERAL,
    BOOLEAN_LITERAL,
    
    // Operators [cite: 51]
    OPERATOR_ARITHMETIC,
    OPERATOR_RELATIONAL,
    OPERATOR_LOGICAL,
    OPERATOR_ASSIGNMENT,
    OPERATOR_INC_DEC,
    
    // Punctuators [cite: 61]
    PUNCTUATOR,
    
    // Special
    EOF,    // End of File
    ERROR   // Lexical Errors
}