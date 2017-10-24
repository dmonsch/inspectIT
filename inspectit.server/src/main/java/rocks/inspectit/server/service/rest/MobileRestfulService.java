package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.util.AgentStatusDataProvider;
import rocks.inspectit.shared.all.cmr.service.IAgentService;
import rocks.inspectit.shared.all.cmr.service.IKeepAliveService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCallbackData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreationResponse;
import rocks.inspectit.shared.all.exception.BusinessException;
import rocks.inspectit.shared.all.instrumentation.config.impl.AgentConfig;

/**
 * @author David Monschein
 *
 */
@Controller
@RequestMapping(value = "/mobile")
public class MobileRestfulService {

	private ObjectMapper mapper;

	private Map<String, AgentConfig> sessionStorage;

	@Autowired
	private DefaultDataDao defaultDataDao;

	@Autowired
	private IAgentService agentService;

	@Autowired
	private IKeepAliveService keepAliveService;

	@Autowired
	private AgentStatusDataProvider agentStatusDataProvider;

	public MobileRestfulService() {
		mapper = new ObjectMapper();
		sessionStorage = new HashMap<>();
	}

	@RequestMapping(method = POST, value = "session")
	@ResponseBody
	public String createSession(@RequestBody String json) {
		try {
			MobileCallbackData data = mapper.readValue(json, MobileCallbackData.class);
			if ((data.getChildData().size() == 1)) {
				DefaultData singleElement = data.getChildData().get(0);
				if (singleElement instanceof SessionCreation) {
					SessionCreation request = (SessionCreation) singleElement;

					String sessionId = createSessionIdEntry();

					// establish agent in service
					AgentConfig establishedConfig = establishAgent(request.getAppName(), request.getDeviceId());
					agentStatusDataProvider.registerKeepAliveTimeout(establishedConfig.getPlatformId());
					sessionStorage.put(sessionId, establishedConfig);

					// insert into influx
					defaultDataDao.saveAll(Lists.newArrayList(request));

					SessionCreationResponse resp = new SessionCreationResponse();
					resp.setSessionId(sessionId);
					return mapper.writeValueAsString(resp);
				}
			}

		} catch (IOException e) {
			return "";
		}

		return "";
	}

	@RequestMapping(method = POST, value = "beacon")
	@ResponseBody
	public String getBeacon(@RequestBody String jsonBeacon) {
		try {
			MobileCallbackData data = mapper.readValue(jsonBeacon, MobileCallbackData.class);
			if (sessionStorage.containsKey(data.getSessionId())) {
				AgentConfig belongingSession = sessionStorage.get(data.getSessionId());
				keepAliveService.sendKeepAlive(belongingSession.getPlatformId());

				List<DefaultData> collectedItems = new ArrayList<DefaultData>();

				for (MobileDefaultData defData : data.getChildData()) {
					collectedItems.add(defData);
				}
				for (MobileSpan span : data.getChildSpans()) {
					span.setPlatformIdent(belongingSession.getPlatformId());
					collectedItems.add(span);
				}

				defaultDataDao.saveAll(collectedItems);
			}
		} catch (IOException e) {
			return "";
		}
		return "";
	}

	private AgentConfig establishAgent(String appName, String deviceId) {
		try {
			return agentService.register(Collections.singletonList("0.0.0.0"), deviceId + "-" + appName, "1.0");
		} catch (BusinessException e) {
			return null;
		}
	}

	private String createSessionIdEntry() {
		return UUID.randomUUID().toString();
	}
}

