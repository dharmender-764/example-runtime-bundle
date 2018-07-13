package com.ironmountain.rmaas.activiti.google.spanner;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
		SpannerOptions options = SpannerOptions.newBuilder().setProjectId("rmaas-dit-1").build();
		Spanner spanner = options.getService();
		try {
			DatabaseId db = DatabaseId.of(options.getProjectId(), instanceId, dbId);
			dbClient = spanner.getDatabaseClient(db);
			dbAdminClient = spanner.getDatabaseAdminClient();
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					while (true) {
						try {
							if (dbClient != null) {
								update(documentGuid, 1);
								Thread.sleep(10000);
							} else {
								Thread.sleep(10000);
							}
						} catch (Exception e) {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e1) {
								logger.error("Error sleeping spanner update thread", e);
							}
							logger.error("Error in spanner update thread", e);
						}
					}
				}
			}).start();
		} catch (Exception e) {
			logger.error("Could not initialize spanner", e);
		}
	}

	public void update(String documentGuid, int documentState) {
		logger.info("updating spanner asset_metadata table");
		List<Mutation> mutations = Arrays.asList(Mutation.newUpdateBuilder("asset_metadata").set("document_guid").to(1)
				.set("document_state").to(documentState).build());
		Timestamp write = dbClient.write(mutations);
		logger.info("update complete: " + write);
	}
	
	

}
