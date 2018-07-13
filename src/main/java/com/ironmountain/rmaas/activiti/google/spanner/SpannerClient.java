package com.ironmountain.rmaas.activiti.google.spanner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerOptions;

@Component
public class SpannerClient {

	private static final Logger logger = LoggerFactory.getLogger(SpannerClient.class);

	@Value("${gcp.projectid}")
	private String projectId;

	@Value("${gcp.spanner.instance.id}")
	private String instanceId;

	@Value("${gcp.spanner.dbid}")
	private String dbId;

	@Value("${gcp.spanner.sample.documentguid}")
	private String documentGuid;

	private DatabaseClient dbClient;

	private DatabaseAdminClient dbAdminClient;

	@PostConstruct
	public void intiSpanner() {

		try {
			GoogleCredentials credentials = ServiceAccountCredentials
					.fromStream(new FileInputStream("/root/rmaas-dit-1-2cb23d40da29.json"));

			SpannerOptions options = SpannerOptions.newBuilder().setProjectId("rmaas-dit-1").setCredentials(credentials)
					.build();
			Spanner spanner = options.getService();
			DatabaseId db = DatabaseId.of(options.getProjectId(), instanceId, dbId);

			dbClient = spanner.getDatabaseClient(db);
			dbAdminClient = spanner.getDatabaseAdminClient();

		} catch (FileNotFoundException e) {
			logger.error("Credentials file not found, Could not initialize spanner", e);
		} catch (IOException e) {
			logger.error("Credentials IOException, Could not initialize spanner", e);
		} catch (Exception e) {
			logger.error("Could not initialize spanner", e);
		}
	}

	public void updateDocumentState(String documentGuid, int documentState) {
		logger.info("updating spanner asset_metadata table with documentGuid: " + documentGuid + " and state: " + documentState);
		
		List<Mutation> mutations = Arrays.asList(Mutation.newUpdateBuilder("asset_metadata").set("document_guid")
				.to(documentGuid).set("document_state").to(documentState).build());
		Timestamp write = dbClient.write(mutations);
		
		logger.info("spanner asset_metadata update complete at : " + write + " with documentGuid: " + documentGuid+ " and state: " + documentState);
	}

	@PreDestroy
	public void stopMessageReciever() {
		// TODO: Close spanner connection if can be done
	}

}
