grammar Expressions;

// applies only to the parser:
@header {package org.mbok.cucumberform.expression.generated;}


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
   | obj
   | arr
   | BOOLEAN
   | NULL
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
   {setText(getText().substring(1, getText().length()-1));}
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