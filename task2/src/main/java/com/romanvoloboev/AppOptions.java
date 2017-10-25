package com.romanvoloboev;

import picocli.CommandLine.Option;

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

    @Option(names = "--credentialsFile", description = "for example: C:\\credentials.json", usageHelp = true)
    String credentialsFile;
}
