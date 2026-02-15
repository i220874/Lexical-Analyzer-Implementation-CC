package src;
import java.io.IOException;

%%

%public
%class Yylex
%type Token
%line
%column

%{
  // Helper to create tokens easily
  private Token token(TokenType type, String lexeme) {
      return new Token(type, lexeme, yyline + 1, yycolumn + 1);
  }
  
  private Token token(TokenType type) {
      return new Token(type, yytext(), yyline + 1, yycolumn + 1);
  }
%}

/* Macro Definitions [cite: 123] */
DIGIT = [0-9]
LETTER_UPPER = [A-Z]
LETTER_LOWER = [a-z]
WHITESPACE = [ \t\r\n]+

/* Comments */
SINGLE_COMMENT = "##" [^\n]*
MULTI_COMMENT = "#*" ~"*#"

/* Identifiers: Starts with Upper, then lower/digit/_, max 31 [cite: 35] */
IDENTIFIER = {LETTER_UPPER} ({LETTER_LOWER}|{DIGIT}|_){0,30}

/* Literals */
INTEGER = [+-]? {DIGIT}+
FLOAT = [+-]? {DIGIT}+ \. {DIGIT}{1,6} ([eE] [+-]? {DIGIT}+)?
STRING = \" ([^\"\\\n] | \\. )* \"
CHAR = \' ([^\\\n] | \\. ) \'
BOOLEAN = "true" | "false"

%%

/* Keywords [cite: 33] */
"start"       { return token(TokenType.KEYWORD); }
"finish"      { return token(TokenType.KEYWORD); }
"loop"        { return token(TokenType.KEYWORD); }
"condition"   { return token(TokenType.KEYWORD); }
"declare"     { return token(TokenType.KEYWORD); }
"output"      { return token(TokenType.KEYWORD); }
"input"       { return token(TokenType.KEYWORD); }
"function"    { return token(TokenType.KEYWORD); }
"return"      { return token(TokenType.KEYWORD); }
"break"       { return token(TokenType.KEYWORD); }
"continue"    { return token(TokenType.KEYWORD); }
"else"        { return token(TokenType.KEYWORD); }

/* Boolean Literals */
{BOOLEAN}     { return token(TokenType.BOOLEAN_LITERAL); }

/* Operators [cite: 52-60] */
"+" | "-" | "*" | "/" | "%" | "**"   { return token(TokenType.OPERATOR_ARITHMETIC); }
"==" | "!=" | "<=" | ">=" | "<" | ">" { return token(TokenType.OPERATOR_RELATIONAL); }
"&&" | "||" | "!"                    { return token(TokenType.OPERATOR_LOGICAL); }
"+=" | "-=" | "*=" | "/=" | "="      { return token(TokenType.OPERATOR_ASSIGNMENT); }
"++" | "--"                          { return token(TokenType.OPERATOR_INC_DEC); }

/* Punctuators [cite: 62] */
"(" | ")" | "{" | "}" | "[" | "]" | "," | ";" | ":" { return token(TokenType.PUNCTUATOR); }

/* Literals */
{FLOAT}       { return token(TokenType.FLOAT_LITERAL); }
{INTEGER}     { return token(TokenType.INTEGER_LITERAL); }
{STRING}      { return token(TokenType.STRING_LITERAL); }
{CHAR}        { return token(TokenType.CHAR_LITERAL); }

/* Identifiers (Must be checked AFTER keywords) [cite: 79] */
{IDENTIFIER}  { return token(TokenType.IDENTIFIER); }

/* Comments and Whitespace */
{SINGLE_COMMENT} { /* Ignore */ }
{MULTI_COMMENT}  { /* Ignore */ }
{WHITESPACE}     { /* Ignore */ }

/* Error Fallback [cite: 136] */
.                { System.err.println("Error: Illegal character <" + yytext() + "> at Line " + (yyline+1)); return token(TokenType.ERROR); }