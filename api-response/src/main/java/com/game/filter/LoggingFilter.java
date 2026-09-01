package com.game.filter;

import com.game.response.LogEvents;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Component
public class LoggingFilter extends OncePerRequestFilter {

	@Autowired
	ILoggingFilterService loggingFilterService;

	@Value("${log.events.url}")
	String url;

	private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		long startTime = System.currentTimeMillis();
		filterChain.doFilter(requestWrapper, responseWrapper);
		long timeTaken = System.currentTimeMillis() - startTime;

		String requestBody = getStringValue(requestWrapper.getContentAsByteArray(),
				request.getCharacterEncoding());
		String responseBody = "";
		try {
			responseBody = getStringValue(responseWrapper.getContentAsByteArray(),
					response.getCharacterEncoding());
		} catch (Exception ex) {
			ex.printStackTrace();
			responseBody = ex.getMessage();
		}
		logEvents(request, requestBody);

		LOGGER.info(
				"\nFINISHED PROCESSING : \nMETHOD={}; \nREQUESTURI={}; \nREQUEST PAYLOAD={}; \nRESPONSE CODE={}; \nRESPONSE={}; \nTIM TAKEN={}",
				request.getMethod(), request.getRequestURI(), requestBody, response.getStatus(), responseBody,
				timeTaken);
		responseWrapper.copyBodyToResponse();
	}


	private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
		try {
			return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void logEvents(HttpServletRequest request, String requestBody) {

		Map<String, String> values = loggingFilterService.getTokenData(request);
		//logEvents(url, request.getRequestURI(), requestBody, values);
	}

	public static void logEvents(String logEventUrl, String methodName, String requestBody, String userId, String casinoId, String tableId, String userType) {
		Map<String, String> values = new HashMap<>();
		values.put("userId", userId);
		values.put("casinoId", casinoId);
		values.put("tableId", tableId);
		values.put("userType", userType);

		//logEvents(logEventUrl, methodName, requestBody, values);
	}

	/*public static void logEvents(String logEventUrl, String methodName, String requestBody, Map<String, String> values) {

		try {
			RestTemplate restTemplate = new RestTemplate();
			if(requestBody != null && !requestBody.isEmpty()) {
				LogEvents logEvents = LogEvents.builder().
						eventName("Info")
						.message(requestBody)
						.methodName(methodName)
						.userId(values.get("userId"))
						.operatorId(values.get("casinoId"))
						.tableId(values.get("tableId"))
						.userType(values.get("userType"))
						.build();

				restTemplate.postForEntity(logEventUrl, logEvents, ResponseEntity.class);
			}
			
		}catch (Exception ignored) {
			ignored.printStackTrace();
		}
	}*/

}
