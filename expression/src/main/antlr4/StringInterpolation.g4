lexer grammar StringInterpolation;


// applies only to the parser:
@header {package io.stephub.expression.generated;}

START_REF
   : '${' -> pushMode(Reference)
   ;

STRING_CONTENT
   : (SESC | SSAFECODEPOINT)+
   ;

fragment SESC
   : '\\' (["$\\/bfnrt] | UNICODE)
   ;
fragment SSAFECODEPOINT
   : ~ [$\\\u0000-\u001F]
   | '$' {_input.LA(1)!='{'}?
   ;

fragment UNICODE
   : 'u' HEX HEX HEX HEX
   ;
fragment HEX
   : [0-9a-fA-F]
   ;

mode Reference;
    JSON_OBJ_START
        : '{' -> pushMode(Json);
    PATH
        : ~[}{]*
        ;
    END_REF
        : '}' -> popMode
        ;
    WS
       : [ \t\n\r] + -> skip
       ;

mode Json;
    OBJ
       : '{' (SOME_JSON | OBJ)* '}'
       | '{' '}'
       ;
    SOME_JSON
       : ~ [}{]+
       ;
    JSON_OBJ_END
       : '}' -> popMode
       ;