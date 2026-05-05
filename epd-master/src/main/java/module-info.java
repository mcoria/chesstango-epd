module net.chesstango.epd.master {

    exports net.chesstango.epd.master.filters;
    exports net.chesstango.epd.master;

    requires net.chesstango.board;

    requires net.chesstango.evaluation;
    requires net.chesstango.gardel;
    requires net.chesstango.piazzolla;
    requires net.chesstango.search;
    requires net.chesstango.engine;
    requires net.chesstango.epd.worker;
    requires net.chesstango.epd.core;

    requires org.apache.commons.cli;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;

    requires com.rabbitmq.client;
    requires static lombok;
}