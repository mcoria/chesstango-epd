package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.*;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.epd.EPDDecoder;
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
public class EpdSearchMain implements Function<Path, EpdSearchResultCollection> {
    /**
     * Parametros
     * -d Depth
     * -t TimeOut in milliseconds
     * -i Directorio donde se encuentran los archivos de posicion
     * -f Filtro de archivos
     * -b baseline search
     * <p>
     * Ejemplo:
     * -d 6 -t 0 -i C:\java\projects\chess\chess-utils\testing\EPD\database -f "(mate-[wb][123].epd|Bratko-Kopec.epd|Kaufman.epd|wac-2018.epd|STS*.epd|Nolot.epd|sbd.epd)"
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

        int depth = Integer.parseInt(parsedArgs.getOptionValue('d'));

        int timeOut = parsedArgs.hasOption('t') ? Integer.parseInt(parsedArgs.getOptionValue('t')) : 0;

        String suiteDirectoryStr = parsedArgs.getOptionValue('i');

        String filePattern = parsedArgs.getOptionValue('f');

        log.info("depth={}; timeOut={}; directory={}; filePattern={}", depth, timeOut, suiteDirectoryStr, filePattern);

        Path suiteDirectory = Path.of(suiteDirectoryStr);
        if (!Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + suiteDirectoryStr);
        }

        boolean baseline = parsedArgs.hasOption('b');

        /**
         * Input
         */
        String sessionId = createSessionId(depth);

        Path sessionDirectory = Common.createSessionDirectory(suiteDirectory, sessionId);

        List<Path> epdFiles = Common.listEpdFiles(suiteDirectory, filePattern);


        /**
         * Processors
         */
        EpdSearchMain epdSearchMain = new EpdSearchMain(depth, timeOut);


        SearchReportSaver searchReportSaver = new SearchReportSaver(sessionId, sessionDirectory, baseline);

        /**
         * Execute
         */
        epdFiles.stream()
                .map(epdSearchMain)
                .forEach(searchReportSaver);

    }

    private final EpdSearchParallel epdSearchParallel;

    public EpdSearchMain(int depth, int timeOut) {
        EpdSearch epdSearch = new EpdSearch();
        epdSearch.setDepth(depth);
        if (timeOut > 0) {
            epdSearch.setTimeOut(timeOut);
        }

        this.epdSearchParallel = new EpdSearchParallel();
        this.epdSearchParallel.setEpdSearch(epdSearch);
        this.epdSearchParallel.setSearchSupplier(new SearchSupplier());
    }

    @Override
    public EpdSearchResultCollection apply(Path epdFile) {
        EPDDecoder epdDecoder = new EPDDecoder();
        try {
            String suiteName = epdFile.getFileName().toString();

            Stream<EPD> edpEntries = epdDecoder.decodeEPDs(epdFile);

            List<EpdSearchResult> epdSearchResults = epdSearchParallel.run(edpEntries.toList());

            return new EpdSearchResultCollection(suiteName, epdSearchResults);
        } catch (IOException ioException) {
            log.error("Error reading file: {}", epdFile, ioException);
            throw new RuntimeException(ioException);
        }
    }


    private static CommandLine parseArguments(String[] args) {
        final Options options = new Options();

        Option depthOpt = Option.builder("d").argName("depth").hasArg().required().desc("search depth").build();
        options.addOption(depthOpt);

        Option timeOutOpt = Option.builder("t").argName("timeOut").hasArg().desc("timeout in milliseconds").build();
        options.addOption(timeOutOpt);

        Option directoryOpt = Option.builder("i").argName("directory").hasArg().required().desc("directory where epd files are located").build();
        options.addOption(directoryOpt);

        Option filePatternOpt = Option.builder("f").argName("filePattern").hasArg().required().desc("epd file name pattern").build();
        options.addOption(filePatternOpt);

        Option baselineOpt = Option.builder("b").argName("baseline").desc("baseline search").build();
        options.addOption(baselineOpt);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            new HelpFormatter().printHelp(EpdSearchMain.class.getName(), options);
            System.exit(-1);
        }
        return null;
    }
}
