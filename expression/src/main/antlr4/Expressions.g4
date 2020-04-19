grammar Expressions;

// applies only to the parser:
@header {package io.stephub.expression.generated;}


@members {
  // For collecting info whilst processing rules that can be used in messages
  // protected Stack<String> paraphrase = new Stack<String>();
}

@rulecatch {
        catch(RecognitionException e) {
                reportError(e);
                throw e;
        }
}

expr: json EOF;


// JSON grammar from https://github.com/antlr/grammars-v4/blob/master/json/JSON.g4
json
   : value
   ;

obj
   : '{' pair (',' pair)* '}'
   | '{' '}'
   ;

pair
   : STRING ':' value
   ;

arr
   : '[' value (',' value)* ']'
   | '[' ']'
   ;

value
   : STRING
   | NUMBER
   | INT_NUMBER
   | obj
   | arr
   | BOOLEAN
   | NULL
   | reference
   | function
   | value OPERATOR value
   ;

reference
   : '${' path '}'
   ;

path
   : ID ('.' path)?
   | ID ('[' index ']')+ ('.' path)?
   ;


index
   : INT_NUMBER
   | STRING
   | path
   | function
   | index OPERATOR index
   ;


function
   : ID '(' arguments? ')'
   ;

arguments
   : value ( ',' value )*
   ;

OPERATOR
   : '+'
   | '-'
   | '*'
   | '%'
   | '/'
   ;

NULL
   : 'null'
   ;

BOOLEAN
   : 'true'
   | 'false'
   ;

STRING
   : '"' (ESC | SAFECODEPOINT)* '"'
   ;

ID
   : [a-zA-Z_][a-zA-Z0-9_]*
   ;

fragment ESC
   : '\\' (["\\/bfnrt] | UNICODE)
   ;
fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;
fragment HEX
   : [0-9a-fA-F]
   ;
fragment SAFECODEPOINT
   : ~ ["\\\u0000-\u001F]
   ;


INT_NUMBER
   : INT
   ;

NUMBER
   : '-'? INT ('.' [0-9] +)? EXP?
   ;


fragment INT
   : '0' | [1-9] [0-9]*
   ;

// no leading zeros

fragment EXP
   : [Ee] [+\-]? INT
   ;

// \- since - means "range" inside [...]

WS
   : [ \t\n\r] + -> skip
   ;