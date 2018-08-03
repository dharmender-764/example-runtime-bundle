package com.ironmountain.rmaas.activiti.google.spanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class SpannerApiClient {

	private static final Logger logger = LoggerFactory.getLogger(SpannerApiClient.class);
	
	public static final String documentUpdateStateUrl = "/companies/{companyId}/documents/{documentGUID}/fields/state/{state}";

	private String cdvGatewayUrl;

	private String companyId;

	private String documentGuid;

	private String documentState;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	public SpannerApiClient(@Value("${cdv.gateway.url}") String cdvGatewayUrl, 
			@Value("${cdv.rmaas.document.companyid}") String companyId, 
			@Value("${cdv.rmaas.document.guid}") String documentGuid,
			@Value("${cdv.rmaas.document.newstate}") String documentState) {
		this.cdvGatewayUrl = cdvGatewayUrl;
		this.companyId = companyId;
		this.documentGuid = documentGuid;
		this.documentState = documentState;
	}

	public void updateDocumentState(String companyId, String documentGuid, String documentState) {
		
		try {
			companyId = StringUtils.isEmpty(companyId) ? this.companyId : companyId;
			documentGuid = StringUtils.isEmpty(documentGuid) ? this.documentGuid : documentGuid;
			documentState = StringUtils.isEmpty(documentState) ? this.documentState : documentState;
			logger.info("Updating document state with documentGuid: " + documentGuid + " and state: " + documentState);
			
			Map<String, String> uriVariables = new HashMap<String, String>();
			uriVariables.put("companyId", companyId);
			uriVariables.put("documentGUID", documentGuid);
			uriVariables.put("state", documentState);
			
			restTemplate.put(cdvGatewayUrl + documentUpdateStateUrl, null, uriVariables);
		
			logger.info("Update document state complete with documentGuid: " + documentGuid+ " and state: " + documentState);
		} catch (Exception e) {
			logger.error("Could not update document state with documentGuid: " + documentGuid + " and state: " + documentState, e);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Bean
	public RestTemplate getRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				List<MediaType> supportedMediaTypes = converter.getSupportedMediaTypes();
				List<MediaType> newSupportedMediaTypes = new ArrayList<MediaType>();
				newSupportedMediaTypes.addAll(supportedMediaTypes);
				newSupportedMediaTypes.add(MediaType.TEXT_PLAIN);
				((AbstractHttpMessageConverter<Object>) converter).setSupportedMediaTypes(newSupportedMediaTypes);
			}
		}
		return restTemplate;
	}
	
	public static void main(String[] args) {
		SpannerApiClient spannerApiClient = new SpannerApiClient("https://core-data-vault-api-dot-rmaas-dit-1.appspot.com", 
				"2001", "3eed05b7-a2ff-442e-92da-085c50d0c8b8", "METADATA_UPLOADED");
		spannerApiClient.updateDocumentState("2001", "3eed05b7-a2ff-442e-92da-085c50d0c8b8", "METADATA_UPLOADED");
	}

}
