grammar WCFG;

wcfg
   : assignment* export?
   ;

export
    : 'export' value ';'
    ;

assignment
   : IDENTIFIER ':=' value ';'
   ;

expression
   : (obj | IDENTIFIER) ('<<' (obj | IDENTIFIER))+
   ;

obj
   : '{' pair (',' pair)* '}'
   | '{' '}'
   ;

pair
   : STRING ':' value
   ;

array
   : '[' value (',' value)* ']'
   | '[' ']'
   ;

value
   : NULL
   | BOOLEAN
   | NUMBER
   | STRING
   | IDENTIFIER
   | obj
   | array
   | expression
   ;

BOOLEAN
   : 'true'
   | 'false'
   ;

NULL
   : 'null'
   ;

NUMBER
   : '-'? INT ('.' [0-9] +)? EXP?
   ;

IDENTIFIER: Letter LetterOrDigit*;

STRING
   : '"' (ESC | SAFECODEPOINT)* '"'
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


fragment INT
   : '0' | [1-9] [0-9]*
   ;

// no leading zeros

fragment EXP
   : [Ee] [+\-]? INT
   ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

WS
   : [ \t\n\r] + -> skip
   ;
