package src;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private static class Error {
        String type;
        int line;
        int col;
        String lexeme;
        String reason;

        Error(String type, int line, int col, String lexeme, String reason) {
            this.type = type;
            this.line = line;
            this.col = col;
            this.lexeme = lexeme;
            this.reason = reason;
        }

        @Override
        public String toString() {
            // Format: Error type, line, column, lexeme, reason 
            return String.format("[%s] Line: %d, Col: %d, Lexeme: \"%s\" -> %s", 
                                 type, line, col, lexeme, reason);
        }
    }

    private List<Error> errors = new ArrayList<>();

    public void reportError(String type, int line, int col, String lexeme, String reason) {
        errors.add(new Error(type, line, col, lexeme, reason));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void printErrors() {
        if (!hasErrors()) return;
        
        System.out.println("\n================ ERRORS ================");
        for (Error e : errors) {
            System.out.println(e);
        }
        System.out.println("========================================");
    }
}