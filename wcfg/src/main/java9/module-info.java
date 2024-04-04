module net.woggioni.wson.wcfg {
    requires static lombok;
    requires org.antlr.antlr4.runtime;

    requires net.woggioni.jwo;
    requires net.woggioni.wson;
    exports net.woggioni.wson.wcfg;
}