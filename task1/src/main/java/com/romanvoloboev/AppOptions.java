package com.romanvoloboev;

import picocli.CommandLine.*;

/**
 * @author romanvoloboev
 */
class AppOptions {

    @Option(names = "--help", usageHelp = true, description = "display this help and exit")
    boolean help;

    @Option(names = "--projectId", description = "Specifies projectId", required = true)
    String projectId;

    @Option(names = "--datasetId", description = "Specifies datasetId", required = true)
    String datasetId;

    @Option(names = "--prefix", description = "Specifies prefix", required = true)
    String prefix;

    @Option(names = "--count", description = "Specifies count", required = true)
    Integer count;

    @Option(names = "--credentialsFile", description = "for example: C:\\credentials.json", usageHelp = true)
    String credentialsFile;
}
