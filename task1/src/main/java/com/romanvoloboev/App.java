package com.romanvoloboev;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author romanvoloboev
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static AppOptions options;

    public static void main(String[] args) {
        try {
            options = CommandLine.populateCommand(new AppOptions(), args);
            log.debug("Command line args parsed");
            BigQuery bigqueryService = initBigQueryService();
            if (bigqueryService != null) {
                log.debug("BigQuery service init success.");
                createTables(bigqueryService);
            }
        } catch (CommandLine.PicocliException e) {
            log.error("Error parsing arguments. {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error loading credentials. {}", e.getMessage());
        }
    }

    private static BigQuery initBigQueryService() throws IOException {
        log.info("Init credentials from GOOGLE_APPLICATION_CREDENTIALS variable...");
        try {
            return BigQueryOptions.getDefaultInstance().getService();
        } catch (IllegalArgumentException e) {
            log.info("Looks like variable not set.");
            if (options.credentialsFile != null) {
                Credentials credentials = loadCredentialsFromFile();
                if (credentials != null) {
                    return BigQueryOptions.newBuilder().setCredentials(credentials).setProjectId(options.projectId).build().getService();
                } else {
                    return null;
                }
            } else {
                log.error("You need specify additional start option --credentialsFile </path/to/credentials_file.json> to load credentials from file.");
                return null;
            }
        }
    }

    private static void createTables(BigQuery bigqueryService) {
        Dataset dataset = bigqueryService.getDataset(options.datasetId);
        if (dataset != null) {
            for (int i = 0; i < options.count; i++) {
                log.info("Creating table {}...", i);
                TableId tableId = TableId.of(options.datasetId, options.prefix + i);
                Field hitId = Field.of("hitId", Field.Type.bool()).toBuilder().setMode(Field.Mode.NULLABLE).build();
                Field userId = Field.of("userId", Field.Type.bool()).toBuilder().setMode(Field.Mode.NULLABLE).build();
                Schema schema = Schema.of(hitId, userId);
                TableDefinition tableDefinition = StandardTableDefinition.of(schema);
                TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).setExpirationTime(LocalDateTime.now().plusWeeks(1).toInstant(ZoneOffset.UTC).toEpochMilli()).build();
                Table table = bigqueryService.create(tableInfo);
                log.info("Created: {}", table.getTableId());
            }
        } else {
            log.error("Specified dataset not found.");
        }
    }

    private static Credentials loadCredentialsFromFile() throws IOException {
        log.debug("Loading credentials from specified file: {}", options.credentialsFile);
        File credentialsPath = new File(options.credentialsFile);
        if (credentialsPath.exists() && !credentialsPath.isDirectory()) {
            try (FileInputStream fileInputStream = new FileInputStream(credentialsPath)) {
                Credentials credentials = ServiceAccountCredentials.fromStream(fileInputStream);
                log.debug("Successfully loaded from file.");
                return credentials;
            }
        } else {
            log.error("Credentials file not found.");
            return null;
        }
    }
}
