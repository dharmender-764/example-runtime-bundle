package org.activiti.cloud.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
}
