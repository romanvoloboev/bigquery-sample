package com.romanvoloboev;

import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author romanvoloboev
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Pattern pattern = Pattern.compile("tmp.+");
    private static AppOptions options;

    public static void main(String[] args) {
        try {
            options = CommandLine.populateCommand(new AppOptions(), args);
            log.debug("Command line args parsed");
            BigQuery bigqueryService = initBigQueryService();
            if (bigqueryService != null) {
                log.debug("BigQuery service init success.");
                long expirationTime = LocalDateTime.now().plusWeeks(2).toInstant(ZoneOffset.UTC).toEpochMilli();
                updateTablesExpirationTime(bigqueryService, options.datasetId, expirationTime);
            }
        } catch (CommandLine.PicocliException e) {
            log.error("Error parsing arguments. {}", e.getMessage());
        } catch (IOException e) {
            log.error("Error loading credentials. {}", e.getMessage());
        }
    }

    public static List<Table> updateTablesExpirationTime(BigQuery bigqueryService, String datasetId, long expirationTime) {
        Dataset dataset = bigqueryService.getDataset(datasetId);
        if (dataset != null) {
            Iterator<Table> tableIterator = bigqueryService.listTables(datasetId).iterateAll().iterator();
            if (tableIterator.hasNext()) {
                List<Table> tableList = new ArrayList<>();
                while (tableIterator.hasNext()) {
                    Table table = tableIterator.next();
                    if (pattern.matcher(table.getTableId().getTable()).matches()) {
                        log.info("Found table matched pattern: {}", table.getTableId().getTable());
                        tableList.add(table.toBuilder().setExpirationTime(expirationTime).build().update());
                        log.info("Updated successfully");
                    }
                }
                return tableList;
            } else {
                log.error("Dataset has no tables.");
            }
        } else {
            log.error("Specified dataset not found.");
        }
        return Collections.emptyList();
    }

    private static BigQuery initBigQueryService() throws IOException {
        log.info("Init credentials from GOOGLE_APPLICATION_CREDENTIALS variable...");
        try {
            return BigQueryOptions.getDefaultInstance().getService();
        } catch (IllegalArgumentException e) {
            log.info("Looks like variable not set.");
            if (options.credentialsFile != null) {
                Credentials credentials = loadCredentialsFromFile(options.credentialsFile);
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

    public static Credentials loadCredentialsFromFile(String credentialsFile) throws IOException {
        log.debug("Loading credentials from specified file: {}", credentialsFile);
        File credentialsPath = new File(credentialsFile);
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
