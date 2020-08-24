grammar Features;

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

feature
   : featureHeader
     ((WS|EOL)* EOL scenario)*
     (WS|EOL|commentLine EOL)*
     commentLine?
     EOF
   ;

featureHeader
   : (WS* annotationLine EOL*)* WS* KW_FEATURE WS* COLON WS* name (EOL background)?
   ;

scenario
   : (WS* annotationLine EOL*)* WS* KW_SCENARIO WS* COLON WS* name (EOL steps)?
   ;

name
   : lineText
   ;

lineText
   : (~(WS|EOL|HASH)|ANY) (~EOL|ANY)*
   ;

background
   : (WS|EOL|commentLine EOL)* KW_BACKGROUND WS* COLON WS* EOL steps
   ;

steps
   : step (EOL step)*?
   ;

step
   : (WS|EOL|commentLine EOL)* lineText
     (docString | dataTable)?
   ;

docString
   : (WS|EOL|commentLine EOL)* TRIPLE_QUOTE WS* EOL
     ~ TRIPLE_QUOTE*? EOL
     WS* TRIPLE_QUOTE WS*
   ;

dataTable
   :  row+
   ;

row
   : (WS|EOL|commentLine EOL)*
     PIPE (cell PIPE)+ WS*
   ;

cell
   : ~(PIPE|EOL)*
   ;

annotationLine
   : commentLine
   | tag (WS+ tag)* (WS* HASH ~EOL*)?
   | WS+
   ;

commentLine
   : HASH comment
   ;

comment
   : (~EOL|ANY)*
   ;

tag
   : AT tagName
   ;

tagName
   : (~(AT|WS|EOL|HASH)|ANY)+
   ;

KW_FEATURE
   : 'Feature'
   ;

KW_BACKGROUND
   : 'Background'
   ;

KW_SCENARIO
   : 'Scenario'
   ;

COLON
   : ':'
   ;

HASH
   : '#'
   ;

AT
   : '@'
   ;

TRIPLE_QUOTE
   : '"""'
   ;

ESCAPED_PIPE
   : '\\|'
   ;

PIPE
   : '|'
   ;

WS
   : [ \t]
   ;

EOL
   : [\n\r]
   ;

ANY
   : .
   ;