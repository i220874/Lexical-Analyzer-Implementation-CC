package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ManualScanner {
    private String input;
    private int pos = 0;
    private int line = 1;
    private int col = 1;
    private List<Token> tokens = new ArrayList<>();
    
    // Keyword Map
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "start", "finish", "loop", "condition", "declare", "output", "input", 
        "function", "return", "break", "continue", "else"
    ));

    // Boolean Map
    private static final Set<String> BOOLEANS = new HashSet<>(Arrays.asList("true", "false"));

    public ManualScanner(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        this.input = sb.toString();
    }

    public List<Token> scan() {
        while (pos < input.length()) {
            char current = peek();

            // 1. Whitespace (Skip but track line/col) [cite: 110]
            if (Character.isWhitespace(current)) {
                advance();
                continue;
            }

            // 2. Comments (Priority 1 & 2) [cite: 72, 73, 74]
            if (current == '#') {
                if (peekNext() == '#') { // Single-line ##
                    scanSingleLineComment();
                    continue;
                } else if (peekNext() == '*') { // Multi-line #*
                    scanMultiLineComment();
                    continue;
                }
            }

            // 3. Numbers (Integer & Float) [cite: 80, 81]
            if (Character.isDigit(current) || (current == '.' && Character.isDigit(peekNext()))) {
                scanNumber(); 
                continue;
            }
            
            // Handle negative numbers vs operators (Complex case)
            // Note: Usually '-' is an operator. Negative literals are handled in parser or 
            // if we strictly follow regex "[+-]?[0-9]+", we need to check if previous token was an operand.
            // For this assignment, we treat + and - as operators unless part of the literal immediately (start of line or after operator).
            // Simplified: We treat +/- as operators here. 

            // 4. Identifiers & Keywords [cite: 77, 79]
            if (Character.isUpperCase(current)) { // Identifiers MUST start with Uppercase [cite: 35]
                scanIdentifier();
                continue;
            }
            if (Character.isLowerCase(current)) { // Keywords start with lowercase [cite: 33]
                scanKeywordOrBoolean();
                continue;
            }

            // 5. String Literals [cite: 82]
            if (current == '"') {
                scanString();
                continue;
            }

            // 6. Character Literals [cite: 82]
            if (current == '\'') {
                scanChar();
                continue;
            }

            // 7. Operators & Punctuators (Longest Match) [cite: 76, 83, 84]
            if (isOperatorOrPunctuatorStart(current)) {
                scanOperatorOrPunctuator();
                continue;
            }

            // Unknown Character / Error
            System.err.println("Error: Unknown character '" + current + "' at Line " + line + ", Col " + col);
            advance(); // Skip to avoid infinite loop
        }
        
        tokens.add(new Token(TokenType.EOF, "", line, col));
        return tokens;
    }

    // --- Scanners ---

    private void scanSingleLineComment() {
        // Consumes ## until newline
        advance(); advance(); // Consume ##
        while (pos < input.length() && peek() != '\n') {
            advance();
        }
    }

    private void scanMultiLineComment() {
        // Consumes #* until *#
        int startLine = line;
        int startCol = col;
        advance(); advance(); // Consume #*
        
        boolean closed = false;
        while (pos < input.length() - 1) {
            if (peek() == '*' && peekNext() == '#') {
                advance(); advance(); // Consume *#
                closed = true;
                break;
            }
            advance();
        }
        if (!closed) {
             System.err.println("Error: Unclosed comment starting at Line " + startLine);
        }
    }

    private void scanIdentifier() {
        // Regex: [A-Z][a-z0-9]{0,30}
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        
        // DFA State 0: Accept Upper
        sb.append(advance()); 

        // DFA State 1: Accept Lower or Digit
        while (pos < input.length() && (Character.isLowerCase(peek()) || Character.isDigit(peek()) || peek() == '_')) {
             sb.append(advance());
        }
        
        if (sb.length() > 31) {
             System.err.println("Error: Identifier too long at Line " + line);
        }

        tokens.add(new Token(TokenType.IDENTIFIER, sb.toString(), line, startCol));
    }
    
    private void scanKeywordOrBoolean() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        
        // Consumes lowercase letters
        while (pos < input.length() && Character.isLowerCase(peek())) {
            sb.append(advance());
        }
        
        String text = sb.toString();
        if (KEYWORDS.contains(text)) {
            tokens.add(new Token(TokenType.KEYWORD, text, line, startCol));
        } else if (BOOLEANS.contains(text)) {
            tokens.add(new Token(TokenType.BOOLEAN_LITERAL, text, line, startCol));
        } else {
             // Invalid because Identifiers MUST start with Uppercase
             System.err.println("Error: Invalid identifier (must start with Uppercase) '" + text + "' at Line " + line);
        }
    }

    private void scanNumber() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        // Consume digits
        while (Character.isDigit(peek())) {
            sb.append(advance());
        }

        // Check for Dot
        if (peek() == '.' && Character.isDigit(peekNext())) {
            isFloat = true;
            sb.append(advance()); // Consume .
            while (Character.isDigit(peek())) {
                sb.append(advance());
            }
        }
        
        // Check for Exponent
        if ((peek() == 'e' || peek() == 'E')) {
            isFloat = true;
            sb.append(advance()); // Consume e/E
            if (peek() == '+' || peek() == '-') {
                sb.append(advance());
            }
            while (Character.isDigit(peek())) {
                sb.append(advance());
            }
        }

        if (isFloat) {
            tokens.add(new Token(TokenType.FLOAT_LITERAL, sb.toString(), line, startCol));
        } else {
            tokens.add(new Token(TokenType.INTEGER_LITERAL, sb.toString(), line, startCol));
        }
    }

    private void scanString() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        advance(); // Consume opening "
        
        while (pos < input.length() && peek() != '"') {
            if (peek() == '\\') { // Handle escapes
                sb.append(advance()); 
                if (pos < input.length()) sb.append(advance());
            } else {
                sb.append(advance());
            }
        }
        
        if (pos >= input.length()) {
            System.err.println("Error: Unclosed string literal at Line " + line);
            return;
        }
        advance(); // Consume closing "
        tokens.add(new Token(TokenType.STRING_LITERAL, sb.toString(), line, startCol));
    }

    private void scanChar() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        advance(); // Consume opening '
        
        if (peek() == '\\') {
             sb.append(advance());
             if (pos < input.length()) sb.append(advance());
        } else {
             sb.append(advance());
        }
        
        if (peek() == '\'') {
            advance(); // Consume closing '
            tokens.add(new Token(TokenType.CHAR_LITERAL, sb.toString(), line, startCol));
        } else {
            System.err.println("Error: Invalid character literal at Line " + line);
            advance();
        }
    }

    private void scanOperatorOrPunctuator() {
        int startCol = col;
        char c1 = advance();
        char c2 = (pos < input.length()) ? peek() : '\0';
        String twoChars = "" + c1 + c2;
        
        // Multi-char operators [cite: 76]
        // **, ==, !=, <=, >=, &&, ||, ++, --, +=, -=, *=, /=
        if (Arrays.asList("**", "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=").contains(twoChars)) {
            advance(); // Consume second char
            tokens.add(new Token(determineOpType(twoChars), twoChars, line, startCol));
        } else {
            // Single char
            TokenType type = determineSingleCharType(c1);
            if (type != null) {
                tokens.add(new Token(type, "" + c1, line, startCol));
            } else {
                System.err.println("Error: Unknown operator '" + c1 + "' at Line " + line);
            }
        }
    }

    private TokenType determineOpType(String op) {
        if (Arrays.asList("==", "!=", "<=", ">=", "<", ">").contains(op)) return TokenType.OPERATOR_RELATIONAL;
        if (Arrays.asList("&&", "||", "!").contains(op)) return TokenType.OPERATOR_LOGICAL;
        if (Arrays.asList("+=", "-=", "*=", "/=", "=").contains(op)) return TokenType.OPERATOR_ASSIGNMENT;
        if (Arrays.asList("++", "--").contains(op)) return TokenType.OPERATOR_INC_DEC;
        return TokenType.OPERATOR_ARITHMETIC;
    }
    
    private TokenType determineSingleCharType(char c) {
        if ("(){}[],;:".indexOf(c) != -1) return TokenType.PUNCTUATOR;
        if ("+-*/%".indexOf(c) != -1) return TokenType.OPERATOR_ARITHMETIC;
        if ("<>=!".indexOf(c) != -1) return TokenType.OPERATOR_RELATIONAL; // <, >, ! (relational/logical mix)
        if (c == '=') return TokenType.OPERATOR_ASSIGNMENT;
        return null;
    }
    
    private boolean isOperatorOrPunctuatorStart(char c) {
        return "(){}[],;:+-*/%<>=!&|".indexOf(c) != -1;
    }

    // --- Helpers ---
    private char peek() {
        if (pos >= input.length()) return '\0';
        return input.charAt(pos);
    }
    
    private char peekNext() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private char advance() {
        char c = input.charAt(pos++);
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }
}