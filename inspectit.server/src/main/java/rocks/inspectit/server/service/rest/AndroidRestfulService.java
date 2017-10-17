package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
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
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultDataWrapper;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;
import rocks.inspectit.shared.all.util.Pair;
import rocks.inspectit.shared.android.mobile.MobileCallbackData;
import rocks.inspectit.shared.android.mobile.MobileDefaultData;
import rocks.inspectit.shared.android.mobile.SessionCreationRequest;
import rocks.inspectit.shared.android.mobile.SessionCreationResponse;

/**
 * @author David Monschein
 *
 */
@Controller
@RequestMapping(value = "/mobile")
public class AndroidRestfulService {

	private ObjectMapper mapper;

	private Map<String, Pair<String, String>> sessionStorage;

	@Autowired
	private DefaultDataDao defaultDataDao;

	public AndroidRestfulService() {
		mapper = new ObjectMapper();
		sessionStorage = new HashMap<>();
	}

	@RequestMapping(method = POST, value = "session")
	@ResponseBody
	public String createSession(@RequestBody String json) {
		try {
			MobileCallbackData data = mapper.readValue(json, MobileCallbackData.class);
			if ((data.getChildData().size() == 1)) {
				MobileDefaultData singleElement = data.getChildData().get(0);
				if (singleElement instanceof SessionCreationRequest) {
					SessionCreationRequest request = (SessionCreationRequest) singleElement;
					SessionCreation wrapped = new SessionCreation(request);

					String sessionId = createSessionIdEntry();
					sessionStorage.put(sessionId, new Pair<String, String>(request.getAppName(), request.getDeviceId()));

					// insert into influx
					defaultDataDao.saveAll(Lists.newArrayList(wrapped));

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
				Pair<String, String> belongingSession = sessionStorage.get(data.getSessionId());

				List<DefaultData> collectedItems = new ArrayList<DefaultData>();

				for (MobileDefaultData defData : data.getChildData()) {
					// process these points
					DefaultData conversion = MobileDefaultDataWrapper.wrap(defData);
					if (conversion != null) {
						collectedItems.add(conversion);
					}
				}

				defaultDataDao.saveAll(collectedItems);
			}
		} catch (IOException e) {
			return "";
		}
		return "";
	}

	private String createSessionIdEntry() {
		return UUID.randomUUID().toString();
	}
}

