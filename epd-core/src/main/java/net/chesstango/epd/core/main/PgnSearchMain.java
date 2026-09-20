package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.*;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNDecoder;
import net.chesstango.search.Search;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.chesstango.epd.core.main.Common.createSessionId;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class PgnSearchMain implements Function<PGN, EpdSearchResultCollection> {
    /**
     * Parametros
     * -i Directorio donde se encuentra el archivo PGN
     * -f Archivo PGN
     * <p>
     * Ejemplo:
     * -i C:\java\projects\chess\chess-utils\testing\EPD\database -f games.pgn
     *
     * <p>
     * Ejecutar VM con
     * -Dlogback.configurationFile=./src/shade/logback.xml
     * </p>
     *
     * @param args
     */
    public static void main(String[] args) {
        CommandLine parsedArgs = parseArguments(args);

        String directory = parsedArgs.getOptionValue('i');

        String fileName = parsedArgs.getOptionValue('f');

        log.info("directory={}; file={}", directory, fileName);

        Path directoryPath = Path.of(directory);
        if (!Files.isDirectory(directoryPath)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        Path pgnFilePath = directoryPath.resolve(fileName);
        if (!Files.exists(pgnFilePath)) {
            throw new RuntimeException("File not found: " + fileName);
        }

        /**
         * Input
         */
        String sessionId = createSessionId(fileName);

        Path sessionDirectory = Common.createSessionDirectory(directoryPath, sessionId);

        /**
         * Processors
         */
        PgnSearchMain pgnSearchMain = new PgnSearchMain();

        SearchReportSaver searchReportSaver = new SearchReportSaver(sessionId, sessionDirectory);


        /**
         * Execute
         */
        try (Stream<PGN> pgnStream = new PGNDecoder().decodePGNs(pgnFilePath)) {
            pgnStream
                    .map(pgnSearchMain)
                    .forEach(searchReportSaver);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final EpdSearchSerial epdSearchSerial;
    private final EpdSearch epdSearch;

    public PgnSearchMain() {
        SearchSupplier searchSupplier = new SearchSupplier();

        epdSearch = new EpdSearch();
        Search search = searchSupplier.get();

        epdSearchSerial = new EpdSearchSerial();
        epdSearchSerial.setSearch(search);
        epdSearchSerial.setEpdSearch(epdSearch);

    }

    @Override
    public EpdSearchResultCollection apply(PGN pgn) {
        String suiteName = pgn.getEvent();

        PgnSearchParams pgnSearchParams = new PgnSearchParams(pgn);

        epdSearch.setDepth(pgnSearchParams.getDepth());

        List<EpdSearchResult> epdSearchResults = epdSearchSerial.run(pgnSearchParams.getEPDs());

        return new EpdSearchResultCollection(suiteName, epdSearchResults);
    }


    private static CommandLine parseArguments(String[] args) {
        final Options options = new Options();

        Option directoryOpt = Option.builder("i").argName("directory").hasArg().required().desc("directory where pgn file is located").build();
        options.addOption(directoryOpt);

        Option fileNameOpt = Option.builder("f").argName("fileName").hasArg().required().desc("pgn file name").build();
        options.addOption(fileNameOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            log.error("Parsing failed.", exp);
            new HelpFormatter().printHelp(PgnSearchMain.class.getName(), options);
            System.exit(-1);
        }
        return null;
    }
}
