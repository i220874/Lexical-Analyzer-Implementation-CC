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
    private int commentCount = 0;
    private List<Token> tokens = new ArrayList<>();
    private ErrorHandler errorHandler = new ErrorHandler(); // NEW: Error Handler
    
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "start", "finish", "loop", "condition", "declare", "output", "input", 
        "function", "return", "break", "continue", "else"
    ));
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

    public int getLineCount() { return line; }
    public int getCommentCount() { return commentCount; }
    public ErrorHandler getErrorHandler() { return errorHandler; } // NEW: Expose handler

    public List<Token> scan() {
        while (pos < input.length()) {
            char current = peek();

            if (Character.isWhitespace(current)) {
                advance();
                continue;
            }

            if (current == '#') {
                if (peekNext() == '#') { 
                    scanSingleLineComment();
                    continue;
                } else if (peekNext() == '*') { 
                    scanMultiLineComment();
                    continue;
                }
            }

            if (Character.isDigit(current) || (current == '.' && Character.isDigit(peekNext()))) {
                scanNumber(); 
                continue;
            }
            
            if (Character.isUpperCase(current)) { 
                scanIdentifier();
                continue;
            }
            if (Character.isLowerCase(current)) { 
                scanKeywordOrBoolean();
                continue;
            }

            if (current == '"') {
                scanString();
                continue;
            }

            if (current == '\'') {
                scanChar();
                continue;
            }

            if (isOperatorOrPunctuatorStart(current)) {
                scanOperatorOrPunctuator();
                continue;
            }

            // REPORT ERROR: Unknown Character [cite: 136]
            errorHandler.reportError("Lexical Error", line, col, String.valueOf(current), "Invalid character");
            advance(); 
        }
        
        tokens.add(new Token(TokenType.EOF, "", line, col));
        return tokens;
    }

    private void scanSingleLineComment() {
        commentCount++;
        advance(); advance(); 
        while (pos < input.length() && peek() != '\n') {
            advance();
        }
    }

    private void scanMultiLineComment() {
        commentCount++;
        int startLine = line;
        int startCol = col;
        advance(); advance(); 
        
        boolean closed = false;
        while (pos < input.length() - 1) {
            if (peek() == '*' && peekNext() == '#') {
                advance(); advance(); 
                closed = true;
                break;
            }
            advance();
        }
        if (!closed) {
             // REPORT ERROR: Unclosed Comment [cite: 139]
             errorHandler.reportError("Lexical Error", startLine, startCol, "#*", "Unclosed multi-line comment");
        }
    }

    private void scanIdentifier() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        sb.append(advance()); 

        while (pos < input.length() && (Character.isLowerCase(peek()) || Character.isDigit(peek()) || peek() == '_')) {
             sb.append(advance());
        }
        
        if (sb.length() > 31) {
             // REPORT ERROR: Identifier too long [cite: 138]
             errorHandler.reportError("Identifier Error", line, startCol, sb.toString(), "Identifier exceeds 31 characters");
        }

        tokens.add(new Token(TokenType.IDENTIFIER, sb.toString(), line, startCol));
    }
    
    private void scanKeywordOrBoolean() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isLowerCase(peek())) {
            sb.append(advance());
        }
        
        String text = sb.toString();
        if (KEYWORDS.contains(text)) {
            tokens.add(new Token(TokenType.KEYWORD, text, line, startCol));
        } else if (BOOLEANS.contains(text)) {
            tokens.add(new Token(TokenType.BOOLEAN_LITERAL, text, line, startCol));
        } else {
             // REPORT ERROR: Invalid Identifier Start [cite: 138]
             errorHandler.reportError("Identifier Error", line, startCol, text, "Identifiers must start with Uppercase");
        }
    }

    private void scanNumber() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        while (Character.isDigit(peek())) {
            sb.append(advance());
        }

        if (peek() == '.' && Character.isDigit(peekNext())) {
            isFloat = true;
            sb.append(advance()); 
            while (Character.isDigit(peek())) {
                sb.append(advance());
            }
        }
        
        if ((peek() == 'e' || peek() == 'E')) {
            isFloat = true;
            sb.append(advance()); 
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
        advance(); 
        
        while (pos < input.length() && peek() != '"') {
            if (peek() == '\\') { 
                sb.append(advance()); 
                if (pos < input.length()) sb.append(advance());
            } else {
                sb.append(advance());
            }
        }
        
        if (pos >= input.length()) {
            // REPORT ERROR: Unclosed String [cite: 137]
            errorHandler.reportError("Literal Error", line, startCol, sb.toString(), "Unclosed string literal");
            return;
        }
        advance(); 
        tokens.add(new Token(TokenType.STRING_LITERAL, sb.toString(), line, startCol));
    }

    private void scanChar() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        advance(); 
        
        if (peek() == '\\') {
             sb.append(advance());
             if (pos < input.length()) sb.append(advance());
        } else {
             sb.append(advance());
        }
        
        if (peek() == '\'') {
            advance(); 
            tokens.add(new Token(TokenType.CHAR_LITERAL, sb.toString(), line, startCol));
        } else {
            // REPORT ERROR: Invalid Char
            errorHandler.reportError("Literal Error", line, startCol, sb.toString(), "Invalid character literal");
            advance();
        }
    }

    private void scanOperatorOrPunctuator() {
        int startCol = col;
        char c1 = advance();
        char c2 = (pos < input.length()) ? peek() : '\0';
        String twoChars = "" + c1 + c2;
        
        if (Arrays.asList("**", "==", "!=", "<=", ">=", "&&", "||", "++", "--", "+=", "-=", "*=", "/=").contains(twoChars)) {
            advance(); 
            tokens.add(new Token(determineOpType(twoChars), twoChars, line, startCol));
        } else {
            TokenType type = determineSingleCharType(c1);
            if (type != null) {
                tokens.add(new Token(type, "" + c1, line, startCol));
            } else {
                errorHandler.reportError("Lexical Error", line, startCol, String.valueOf(c1), "Unknown operator");
            }
        }
    }

    // ... (Keep helper methods: determineOpType, determineSingleCharType, isOperatorOrPunctuatorStart, peek, peekNext, advance) ...
    // Note: I will strictly copy the helpers below to ensure the code is complete.
    
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
        if ("<>=!".indexOf(c) != -1) return TokenType.OPERATOR_RELATIONAL;
        if (c == '=') return TokenType.OPERATOR_ASSIGNMENT;
        return null;
    }
    
    private boolean isOperatorOrPunctuatorStart(char c) {
        return "(){}[],;:+-*/%<>=!&|".indexOf(c) != -1;
    }

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