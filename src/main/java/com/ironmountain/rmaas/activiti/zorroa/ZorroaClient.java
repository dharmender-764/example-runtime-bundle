package com.ironmountain.rmaas.activiti.zorroa;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ironmountain.rmaas.activiti.zorroa.model.Clip;
import com.ironmountain.rmaas.activiti.zorroa.model.Document;
import com.ironmountain.rmaas.activiti.zorroa.model.Media;
import com.ironmountain.rmaas.activiti.zorroa.model.Source;
import com.ironmountain.rmaas.activiti.zorroa.model.ZorroaSearchResponse;
import com.zorroa.archivist.client.HmacClient;
import com.zorroa.archivist.client.Json;
import com.zorroa.archivist.client.search.AssetFilter;
import com.zorroa.archivist.client.search.AssetSearch;

@Service(value = "zorroaClient")
public class ZorroaClient {

	private static final Logger logger = LoggerFactory.getLogger(ZorroaClient.class);

	@Value("${zorroa.repository.url}")
	private String repoUrl;

	@Value("${zorroa.repository.username}")
	private String username;

	@Value("${zorroa.repository.apikey}")
	private String apiKey;

	private ZorroaClient() {

	}

	private ZorroaClient(String repoUrl, String username, String apiKey) {
		this.repoUrl = repoUrl;
		this.username = username;
		this.apiKey = apiKey;
	}

	public HmacClient initClient() {
		return new HmacClient(repoUrl, username, apiKey);
	}

	public void whoAmI() {
		HmacClient client = initClient();
		Map<String, Object> result = client.get("/api/v1/who", Map.class);
		logger.info(Json.prettyString(result));
		System.out.println(result.get("username"));
	}

	public Map<String, Map<String, Object>> performEmptySearch() {
		HmacClient client = initClient();
		Map<String, Map<String, Object>> result = client.post("/api/v4/assets/_search", new HashMap<String, Object>(),
				Map.class);
		logger.info(Json.prettyString(result));
		System.out.println((int) result.get("page").get("totalCount") > 0);
		System.out.println("totalCount from search = " + (int) result.get("page").get("totalCount"));
		return result;
	}

	public Map<String, Map<String, Object>> performTermSearchRawJson() throws IOException {
		HmacClient client = initClient();
		String json = "{'filter': {'terms': {'media.author': ['Iron Mountain'] }}}";
		Map<String, Map<String, Object>> result = client.post("/api/v4/assets/_search",
				Json.Mapper.readValue(json, Map.class), Map.class);
		logger.info(Json.prettyString(result));
		System.out.println("totalCount from search = " + (int) result.get("page").get("totalCount"));
		return result;
	}

	public Map<String, Map<String, Object>> performTermSearchFilterBuilder() throws IOException {
		HmacClient client = initClient();
		AssetSearch search = new AssetSearch().setFilter(new AssetFilter().addToTerms("media.author", "Iron Mountain"));

		Map<String, Map<String, Object>> result = client.post("/api/v4/assets/_search", search, Map.class);
		logger.info(Json.prettyString(result));
		System.out.println("totalCount from search = " + (int) result.get("page").get("totalCount"));
		return result;
	}

	public ZorroaSearchResponse performSearch() throws IOException {
		HmacClient client = initClient();
		AssetSearch search = new AssetSearch().setFilter(new AssetFilter().addToTerms("media.author", "Iron Mountain"));

		ZorroaSearchResponse result = client.post("/api/v4/assets/_search", search, ZorroaSearchResponse.class);
		//logger.info(Json.prettyString(result));
		System.out.println("totalCount from search = " + result.getPage().getTotalCount());
		return result;
	}

	public static void main(String[] args) throws IOException {
		ZorroaSearchResponse searchResponse = new ZorroaClient("https://irm-training.pool.zorroa.com",
				"rajesh.chandrasekhar@ironmountain.com",
				"6d98f9a2ac39b58202cbdf54214c289027ca89e88437005bb60001b9cd957e33").performSearch();
		System.out.println("\nTotal docs: " + searchResponse.getPage().getTotalCount());
		System.out.println("Document details:");
		for (Document document : searchResponse.getDocuments()) {
			System.out.println("##########################Document#########################");
			System.out.println("\tId: " + document.getId());
			System.out.println("\tType: " + document.getType());
			System.out.println("\tScore: " + document.getScore());

			Source source = document.getDocument().getSource();
			if (source != null) {
				System.out.println("\tSource details:");
				System.out.println("\t\tFile Name: " + source.getFilename());
				System.out.println("\t\tFile Size: " + source.getFileSize());
				System.out.println("\t\tBase Name: " + source.getBasename());
				System.out.println("\t\tExtension: " + source.getExtension());
				System.out.println("\t\tCreated time: " + source.getTimeCreated());
				System.out.println("\t\tPath: " + source.getPath());
			}
			
			Media media = document.getDocument().getMedia();
			if (media != null) {
				System.out.println("\tMedia details:");
				System.out.println("\t\tAuthor: " + media.getAuthor());
				System.out.println("\t\tPages: " + media.getPages());
				System.out.println("\t\tCreated time: " + media.getTimeCreated());
				
				Clip clip = media.getClip();
				if (clip != null) {
					System.out.println("\t\tClip Details:");
					System.out.println("\t\t\tStart: " + clip.getStart());
					System.out.println("\t\t\tStop: " + clip.getStop());
					System.out.println("\t\t\tLength: " + clip.getLength());
				}
			}
		}
	}
}
