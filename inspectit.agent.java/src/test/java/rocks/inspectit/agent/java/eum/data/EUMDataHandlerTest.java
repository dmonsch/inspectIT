package rocks.inspectit.agent.java.eum.data;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import rocks.inspectit.agent.java.core.impl.CoreService;
import rocks.inspectit.shared.all.communication.data.EUMData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSession;
import rocks.inspectit.shared.all.testbase.TestBase;

public class EUMDataHandlerTest extends TestBase {

	private static final String BASEURL_DEMOVALUE = "http://www.plain.abc";
	private static final String SESSID_DEMOVALUE = "12345";
	private static final UserSession SESSION_DEMOVALUE = createEmptySession(SESSID_DEMOVALUE);
	private static final String DEMO_URL = "http://www.demo-inspectit.com";
	private static final long TIME_DEMOVALUE = System.currentTimeMillis();
	private static final EUMData EMPTY_EUMDATA = new EUMData();
	private static AjaxRequest testAjax;

	private static final Map<String, EUMData> expectedResultMapping;

	private ObjectMapper mapper;

	static {
		expectedResultMapping = new HashMap<String, EUMData>();
		ObjectMapper mapper = new ObjectMapper();

		// create first test
		EUMData expected = new EUMData();
		expected.setBaseUrl(BASEURL_DEMOVALUE);
		expected.setUserSession(SESSION_DEMOVALUE);

		AjaxRequest ajxReq = new AjaxRequest();
		ajxReq.setUrl(DEMO_URL);
		ajxReq.setStartTime(TIME_DEMOVALUE);
		ajxReq.setEndTime(TIME_DEMOVALUE);
		ajxReq.setMethod("GET");
		ajxReq.setBaseUrl(BASEURL_DEMOVALUE);
		ajxReq.setStatus(200);
		expected.addAjaxRequest(ajxReq);

		testAjax = ajxReq;

		try {
			expectedResultMapping.put(createBasicJson(true, mapper.writeValueAsString(ajxReq), SESSID_DEMOVALUE, "pageLoad"), expected);
			expectedResultMapping.put(createBasicJson(false, mapper.writeValueAsString(ajxReq), SESSID_DEMOVALUE, "pageLoad"), expected);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// create second test
		ResourceLoadRequest rlq = new ResourceLoadRequest();
		rlq.setEndTime(TIME_DEMOVALUE);
		rlq.setInitiatorType("INITTYPE");
		rlq.setInitiatorUrl(BASEURL_DEMOVALUE);
		rlq.setStartTime(TIME_DEMOVALUE);
		rlq.setTransferSize(256);
		rlq.setUrl(DEMO_URL);

		PageLoadRequest plrq = new PageLoadRequest();
		plrq.setConnectEndW(TIME_DEMOVALUE); // only some attributes
		plrq.setLoadEventEndW(TIME_DEMOVALUE);

		EUMData expected2 = new EUMData();
		expected2.setBaseUrl(BASEURL_DEMOVALUE);
		expected2.setUserSession(SESSION_DEMOVALUE);

		expected2.addAjaxRequest(ajxReq);
		expected2.addResourceLoadRequest(rlq);
		expected2.addPageLoadRequest(plrq);

		try {
			expectedResultMapping.put(
					createBasicJson(true, mapper.writeValueAsString(ajxReq) + "," + mapper.writeValueAsString(rlq) + "," + mapper.writeValueAsString(plrq), SESSID_DEMOVALUE, "pageLoad"), expected2);
			expectedResultMapping.put(
					createBasicJson(false, mapper.writeValueAsString(ajxReq) + "," + mapper.writeValueAsString(rlq) + "," + mapper.writeValueAsString(plrq), SESSID_DEMOVALUE, "pageLoad"), expected2);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Mock
	private Logger log;

	@InjectMocks
	private CoreService coreService;

	private DataHandler dHandler;

	@BeforeMethod
	public void initTests() {
		dHandler = new DataHandler(coreService);
		mapper = new ObjectMapper();
	}

	@Test
	public void invalidEUMData() {
		coreService.addEumData(null);
		assertThat("Testing with invalid data.", coreService.getEumData() == null);
	}

	@Test
	public void jsonTests() {
		for (String key : expectedResultMapping.keySet()) {
			dHandler.insertBeacon(key);
			assertThat("Testing against expected results.", expectedResultMapping.get(key).equals(coreService.getEumData()));
			assertThat("Testing expected results against random input.", !coreService.getEumData().equals(EMPTY_EUMDATA) && !expectedResultMapping.get(key).equals(EMPTY_EUMDATA));
		}
	}

	@Test
	public void sessionTest() throws JsonGenerationException, JsonMappingException, IOException {
		UserSession _new = new UserSession("Firefox", "iPhone", "de", "123456");
		// create the session
		dHandler.insertBeacon(createSessionInit(SESSION_DEMOVALUE));
		// send a beacon
		String testBeacon = createBasicJson(true, mapper.writeValueAsString(testAjax), _new.getSessionId(), "click");
		dHandler.insertBeacon(testBeacon);
		// check if session correct
		assertThat("User session correct.", coreService.getEumData().getUserSession().equals(_new));
		// send another beacon
		dHandler.insertBeacon(testBeacon);
		// check if session persists
		assertThat("User session persists.", coreService.getEumData().getUserSession().equals(_new));
	}

	@Test
	public void invalidDataTests() {
		dHandler.insertBeacon(null);
	}

	private static String createBasicJson(boolean withOuter, String contentValue, String sessValue, String specType) {
		String create = "{\"type\":\"userAction\",\"baseUrl\":\"" + BASEURL_DEMOVALUE + "\",\"sessionId\":\"" + sessValue + "\",\"specialType\":\"" + specType + "\",\"contents\":[" + contentValue
				+ "]}";
		if (withOuter) {
			return "[" + create + "]";
		}
		return create;
	}

	private static String createSessionInit(UserSession sess) {
		try {
			return "{\"type\":\"userSession\"," + new ObjectMapper().writeValueAsString(sess).substring(1);
		} catch (JsonGenerationException e) {
		} catch (JsonMappingException e) {
		} catch (IOException e) {
		}
		return null;
	}

	private static UserSession createEmptySession(String id) {
		// for creating a session when the client already has one but it disappeared on the server
		UserSession r = new UserSession();
		r.setSessionId(id);
		r.setBrowser("Firefox");
		r.setDevice("iOS");
		r.setLanguage("de");

		return r;
	}

}