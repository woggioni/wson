module net.woggioni.wson {
    requires static lombok;
    requires net.woggioni.jwo;

    exports net.woggioni.wson.xface;
    exports net.woggioni.wson.value;
    exports net.woggioni.wson.exception;
    exports net.woggioni.wson.serialization;
    exports net.woggioni.wson.serialization.json;
    exports net.woggioni.wson.serialization.binary;
    exports net.woggioni.wson.traversal;
}