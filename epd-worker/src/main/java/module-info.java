module net.chesstango.epd.worker {
    exports net.chesstango.epd.worker;

    requires net.chesstango.board;
    requires net.chesstango.gardel;
    requires net.chesstango.search;
    requires net.chesstango.evaluation;
    requires net.chesstango.epd.core;

    requires org.slf4j;
    requires com.rabbitmq.client;
    requires static lombok;
}