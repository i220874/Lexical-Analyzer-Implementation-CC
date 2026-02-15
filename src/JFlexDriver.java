package src;

import java.io.FileReader;
import java.io.IOException;

public class JFlexDriver {
    public static void main(String[] args) {
        String fileToScan = (args.length > 0) ? args[0] : "tests/test1.lang";
        System.out.println("Scanning file (JFlex): " + fileToScan);

        try {
            // Initialize JFlex Scanner (Yylex)
            Yylex lexer = new Yylex(new FileReader(fileToScan));
            Token t;

            System.out.println("\n================ TOKEN OUTPUT ================");
            
            // Loop until EOF (Yylex returns null at end)
            while ((t = lexer.yylex()) != null) {
                // Skip error tokens in output if you want identical behavior, 
                // or print them. For now, we print everything.
                System.out.println(t);
            }
            
            // Note: JFlex doesn't inherently store statistics unless we add counters to Yylex.
            // For the comparison, the Token Output is the most important part.

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (Error e) {
            System.err.println("JFlex Error: " + e.getMessage());
        }
    }
}