package src;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        // Default to test1.lang if no argument provided
        String fileToScan = (args.length > 0) ? args[0] : "tests/test1.lang";

        System.out.println("Scanning file: " + fileToScan);

        try {
            ManualScanner scanner = new ManualScanner(fileToScan);
            List<Token> tokens = scanner.scan();
            SymbolTable symTable = new SymbolTable();
            
            // Statistics counters
            int totalTokens = 0;
            Map<TokenType, Integer> tokenCounts = new HashMap<>();

            System.out.println("\n================ TOKEN OUTPUT ================");
            for (Token t : tokens) {
                if (t.getType() == TokenType.EOF) break;
                
                // 1. Print Token in required format
                System.out.println(t); 
                
                // 2. Update Stats
                totalTokens++;
                tokenCounts.put(t.getType(), tokenCounts.getOrDefault(t.getType(), 0) + 1);

                // 3. Update Symbol Table (Identifiers only)
                if (t.getType() == TokenType.IDENTIFIER) {
                    symTable.add(t.getLexeme(), "IDENTIFIER", t.getLine());
                }
            }

            // 4. Print Statistics
            System.out.println("\n================ STATISTICS ================");
            System.out.println("Total Tokens: " + totalTokens);
            System.out.println("Lines Processed: " + scanner.getLineCount());
            System.out.println("Comments Removed: " + scanner.getCommentCount());
            
            System.out.println("\nToken Distribution:");
            for (TokenType type : tokenCounts.keySet()) {
                System.out.println("  " + type + ": " + tokenCounts.get(type));
            }
            
            // 5. Print Symbol Table
            symTable.printTable();

            // 6. Print Errors (NEW)
            scanner.getErrorHandler().printErrors();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}