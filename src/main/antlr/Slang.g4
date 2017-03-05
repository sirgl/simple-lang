grammar Slang;


External: 'extern';

Int: 'int';
Bool: 'bool';
Unit: 'unit';
True : 'true';
False: 'false';

PLUS : '+';
MINUS : '-';
MUL : '*';
DIV : '/';
EQ : '=';
GT : '>';
GTEQ : '>=';
LTEQ : '<=';
LT : '<';
EQEQ : '==';
NEG : '~';
AND : '&&';
OR : '||';


IntLiteral : ('0'..'9')+;
StringLiteral
   : '"' (~ ["\\])* '"';
CharLiteral : '\'' (~ ["\\]) '\'';

WS  :   ( [ \t\r\n] | COMMENT) -> skip;
Id : ('A'..'Z' | 'a'..'z')+;

COMMENT
: '/*'.*?'*/' /*single comment*/
| '//'~('\r' | '\n')* /* multiple comment*/
;


compilationUnit: (functionDef)*;

functionDef: External? type Id'(' formalParameters? ')' block?;

formalParameters:   formalParameter (',' formalParameter)* ;

formalParameter: type Id;

type: Int | Bool | Unit;

block: '{' statement* '}';

statement: expr ';'
    | returnStmt
    | varDefStmt
    | assignStmt
    | ifStmt
    | whileStmt
    ;

returnStmt: 'return' expr?;

varDef: type Id EQ expr;

varDefStmt: varDef ';';

assignStmt: <assoc=right> Id EQ expr ';';

ifStmt: 'if' condition block ('else' block)?;

whileStmt: 'while' condition block;

condition: '(' expr ')';

expr: primary   #primaryExpr
    | functionCall #callExpr
    | expr operator=(DIV | MUL) expr  #multiplyExpr
    | expr operator=(PLUS | MINUS)  expr   #sumExpr
    | expr operator=(EQEQ | GT | GTEQ | LT | LTEQ)  expr   #comarsionExpr
    | expr operator=(AND | OR)  expr   #boolExpr
    | NEG expr  #negExpr
    | MINUS expr  #unaryMinus
    | PLUS expr #unaryPlus
    ;

functionCall: Id '(' expressionList? ')';

expressionList :   expr (',' expr)* ;

primary : '(' expr ')'
    | True
    | False
    | IntLiteral
    | Id
;