module net.chesstango.epd.core {
    exports net.chesstango.epd.core.report;
    exports net.chesstango.epd.core.search;
    exports net.chesstango.epd.core.main;

    requires net.chesstango.gardel;
    requires net.chesstango.search;
    requires net.chesstango.board;
    requires net.chesstango.reports;
    requires net.chesstango.evaluation;
    requires net.chesstango.engine;

    requires org.slf4j;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;

    requires static lombok;

    opens net.chesstango.epd.core.report to com.fasterxml.jackson.databind;
    opens net.chesstango.epd.core.main to com.fasterxml.jackson.databind;
}