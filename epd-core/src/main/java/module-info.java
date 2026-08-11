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
    requires tools.jackson.databind;

    requires static lombok;

    opens net.chesstango.epd.core.report to tools.jackson.databind;
    opens net.chesstango.epd.core.main to tools.jackson.databind;
}