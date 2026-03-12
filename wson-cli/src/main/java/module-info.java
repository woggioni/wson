module net.woggioni.wson.cli {
    requires static lombok;
    requires net.woggioni.jwo;
    requires net.woggioni.wson;
    requires net.woggioni.wson.wcfg;
    requires org.slf4j;
    requires org.slf4j.simple;
    requires info.picocli;
    exports net.woggioni.wson.cli;

    opens net.woggioni.wson.cli to info.picocli;
}