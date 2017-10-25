package com.romanvoloboev;

import com.google.auth.Credentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Table;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author romanvoloboev
 */
public class AppTest {
    private static final Logger log = LoggerFactory.getLogger(AppTest.class);
    private BigQuery bigQueryService;
    private String datasetId;
    private Long expirationTime;

    @Before
    public void setUp() throws Exception {
        datasetId = "owox_dataset_1";
        Credentials credentials = App.loadCredentialsFromFile("src/test/resources/test.json");
        bigQueryService = BigQueryOptions.newBuilder().setCredentials(credentials).setProjectId("owox-test-1").build().getService();
        expirationTime = LocalDateTime.now().plusWeeks(2).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    @Test
    public void test1() throws Exception {
        log.info("expected table exp. time: {}", expirationTime);
        List<Table> tableList = App.updateTablesExpirationTime(bigQueryService, datasetId, expirationTime);
        assert !tableList.isEmpty();
        for (Table table : tableList) {
            Long receivedExpTime = table.getExpirationTime();
            log.info("received exp time: {}", receivedExpTime);
            assertEquals(this.expirationTime, receivedExpTime);
        }
    }
}