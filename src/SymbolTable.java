package src;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap; // Used for sorted output

public class SymbolTable {
    // Inner class to store symbol details
    private static class SymbolEntry {
        String type;
        int firstLine;
        int frequency;

        SymbolEntry(String type, int firstLine) {
            this.type = type;
            this.firstLine = firstLine;
            this.frequency = 1;
        }
    }

    // Map to store symbols (Name -> Entry)
    private Map<String, SymbolEntry> table = new TreeMap<>(); // TreeMap keeps keys sorted

    public void add(String name, String type, int line) {
        if (table.containsKey(name)) {
            table.get(name).frequency++;
        } else {
            table.put(name, new SymbolEntry(type, line));
        }
    }

    public void printTable() {
        System.out.println("\n================ SYMBOL TABLE ================");
        System.out.printf("%-20s %-15s %-10s %-10s%n", "Name", "Type", "First Line", "Frequency");
        System.out.println("-----------------------------------------------------------");
        for (Map.Entry<String, SymbolEntry> entry : table.entrySet()) {
            SymbolEntry e = entry.getValue();
            System.out.printf("%-20s %-15s %-10d %-10d%n", entry.getKey(), e.type, e.firstLine, e.frequency);
        }
        System.out.println("===========================================================");
    }
}