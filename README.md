# CS4031 Compiler Construction - Assignment 01
**Lexical Analyzer Implementation for Custom Language (.lang)**

## 👥 Team Members
**Member 1:** Haseeb Sultan 22i-0874 CS-E
**Member 2:** Bilal Naveed  22i-0811 CS-B


---

## 📂 Project Structure
The project follows the strict directory structure required by the assignment guidelines [cite: 147-151].

```text
RollNumber1-RollNumber2-Section/
├── src/                  # Source Code
│   ├── ManualScanner.java  # Custom DFA-based Scanner logic
│   ├── Token.java          # Token definition class
│   ├── TokenType.java      # Enum for token categories
│   ├── SymbolTable.java    # Symbol Table implementation
│   ├── ErrorHandler.java   # Error reporting and recovery
│   ├── Scanner.flex        # JFlex specification file
│   ├── Yylex.java          # Generated JFlex Scanner class
│   ├── Main.java           # Driver for Manual Scanner
│   └── JFlexDriver.java    # Driver for JFlex Scanner comparison
├── docs/                 # Documentation
│   ├── Automata_Design.pdf # NFA/DFA Diagrams
│   ├── Comparison.pdf      # Output comparison proof
│   └── LanguageGrammar.txt # Regex rules
├── tests/                # Test Cases
│   ├── test1.lang        # Basic valid tokens
│   ├── test2.lang        # Complex expressions
│   ├── test3.lang        # Error cases
│   └── TestResults.txt   # Log of results
└── README.md

🛠️ Compilation and Execution Instructions

Prerequisites
Java JDK (Version 8 or higher) 
JFlex (Optional, only required if regenerating Yylex.java) 

1. Compile the Project Open a terminal in the project root directory and run:Bashjavac src/*.java
2. Run Manual ScannerTo scan a file using the manually implemented DFA scanner (Part 1):Bashjava src.Main tests/test1.lang
3. Run JFlex ScannerTo scan a file using the JFlex-generated scanner for comparison (Part 2):Bashjava src.JFlexDriver tests/test1.lang

