package net.chesstango.epd.core.main;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.epd.core.search.EpdSearch;
import net.chesstango.epd.core.search.EpdSearchParallel;
import net.chesstango.epd.core.search.EpdSearchResult;
import net.chesstango.epd.core.search.SearchSupplier;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.epd.EPDDecoder;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static net.chesstango.epd.core.main.Common.createSessionId;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class EpdSearchMain implements Runnable {
    /**
     * Parametros
     * -d Depth
     * -t TimeOut in milliseconds
     * -i Directorio donde se encuentran los archivos de posicion
     * -f Filtro de archivos
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

        String directory = parsedArgs.getOptionValue('i');

        String filePattern = parsedArgs.getOptionValue('f');

        System.out.printf("depth={%d}; timeOut={%d}; directory={%s}; filePattern={%s}%n", depth, timeOut, directory, filePattern);

        Path suiteDirectory = Path.of(directory);
        if (!Files.isDirectory(suiteDirectory)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        List<Path> epdFiles = Common.listEpdFiles(suiteDirectory, filePattern);

        String sessionId = createSessionId(depth);

        Path sessionDirectory = Common.createSessionDirectory(suiteDirectory, sessionId);

        new EpdSearchMain(sessionId, sessionDirectory, epdFiles, depth, timeOut)
                .run();
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

    private final List<Path> epdFiles;
    private final int depth;
    private final int timeOut;

    private final EPDDecoder reader;
    private final SearchSupplier searchSupplier;
    private final SearchReportSaver searchReportSaver;

    public EpdSearchMain(String sessionId, Path sessionDirectory, List<Path> epdFiles, int depth, int timeOut) {
        this.epdFiles = epdFiles;
        this.depth = depth;
        this.timeOut = timeOut;
        this.reader = new EPDDecoder();
        this.searchSupplier = new SearchSupplier();
        this.searchReportSaver = new SearchReportSaver(sessionId, sessionDirectory);
    }

    @Override
    public void run() {
        EpdSearch epdSearch = new EpdSearch();
        epdSearch.setDepth(depth);
        if (timeOut > 0) {
            epdSearch.setTimeOut(timeOut);
        }

        EpdSearchParallel epdSearchParallel = new EpdSearchParallel();
        epdSearchParallel.setEpdSearch(epdSearch);
        epdSearchParallel.setSearchSupplier(searchSupplier);

        for (Path epdFile : epdFiles) {
            try {
                String suiteName = epdFile.getFileName().toString();

                Stream<EPD> edpEntries = reader.decodeEPDs(epdFile);

                List<EpdSearchResult> epdSearchResults = epdSearchParallel.run(edpEntries.toList());

                searchReportSaver.accept(suiteName, epdSearchResults);

            } catch (IOException ioException) {
                log.error("Error reading file: {}", epdFile, ioException);
            }
        }
    }
}
