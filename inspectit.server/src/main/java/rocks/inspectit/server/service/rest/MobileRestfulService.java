package rocks.inspectit.server.service.rest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import rocks.inspectit.server.dao.DefaultDataDao;
import rocks.inspectit.server.dao.impl.BufferSpanDaoImpl;
import rocks.inspectit.server.dao.impl.DefaultDataDaoImpl;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCallbackData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileCmrSessionStorage;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.MobileSpan;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreation;
import rocks.inspectit.shared.all.communication.data.mobile.SessionCreationResponse;
import rocks.inspectit.shared.all.tracing.data.AbstractSpan;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
@Controller
@RequestMapping(value = "/mobile")
public class MobileRestfulService {

	/**
	 * Used JSON object mapper.
	 */
	private ObjectMapper mapper;

	/**
	 * Storage for managing sessions.
	 */
	private MobileCmrSessionStorage sessionStorage;

	/**
	 * {@link DefaultDataDaoImpl}.
	 */
	@Autowired
	private DefaultDataDao defaultDataDao;

	/**
	 * {@link BufferSpanDaoImpl}.
	 */
	@Autowired
	private BufferSpanDaoImpl spanDao;

	/**
	 * Scheduled executor used for scheduling the Span correlation due to asynchronous indexing and
	 * adding data to the buffer.
	 */
	@Qualifier("scheduledExecutorService")
	@Autowired
	private ScheduledExecutorService scheduledExecutor;

	/**
	 * Creates the mobile restful service and initializes all dependencies.
	 */
	public MobileRestfulService() {
		mapper = new ObjectMapper();
		sessionStorage = new MobileCmrSessionStorage();
	}

	/**
	 * Access point for establishing a session.
	 *
	 * @param json
	 *            the json which should contain an instance of {@link SessionCreation}.
	 * @return session id if the creation was successful, an empty string otherwise
	 */
	@RequestMapping(method = POST, value = "session")
	@ResponseBody
	public String createSession(@RequestBody String json) {
		try {
			MobileCallbackData data = mapper.readValue(json, MobileCallbackData.class);
			if ((data.getChildData().size() == 1)) {
				DefaultData singleElement = data.getChildData().get(0);
				if (singleElement instanceof SessionCreation) {
					SessionCreation request = (SessionCreation) singleElement;

					String sessionId = sessionStorage.createEntry();

					// infer session info
					sessionStorage.putTag(sessionId, "appName", request.getAppName());
					for (String additionalInformationKey : request.getAdditionalInformation().keySet()) {
						sessionStorage.putTag(sessionId, additionalInformationKey, request.getAdditionalInformation().get(additionalInformationKey));
					}

					// insert into influx
					request.setSessionTags(sessionStorage.getTags(sessionId));
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

	/**
	 * Access point for sending mobile monitoring beacons.
	 *
	 * @param jsonBeacon
	 *            json encoded beacon which contains monitoring data
	 * @return empty string
	 */
	@RequestMapping(method = POST, value = "beacon")
	@ResponseBody
	public String getBeacon(@RequestBody String jsonBeacon) {
		try {
			MobileCallbackData data = mapper.readValue(jsonBeacon, MobileCallbackData.class);
			if (sessionStorage.hasEntry(data.getSessionId())) {
				List<Pair<String, String>> belongingTags = sessionStorage.getTags(data.getSessionId());

				List<DefaultData> collectedItems = new ArrayList<DefaultData>();

				for (MobileDefaultData defData : data.getChildData()) {
					defData.setSessionTags(belongingTags);
					collectedItems.add(defData);
				}

				for (MobileSpan span : data.getChildSpans()) {
					span.collectChildTags();
					MobileSpanCorrelationTask task = new MobileSpanCorrelationTask(span);
					task.schedule(true);
				}

				defaultDataDao.saveAll(collectedItems);
			}
		} catch (IOException e) {
			return "";
		}
		return "";
	}

	private class MobileSpanCorrelationTask implements Runnable {

		/**
		 * Number of seconds to wait between trials of correlation.
		 */
		private static final int NUMBER_OF_SECONDS_BETWEEN_TRIALS = 3;

		/**
		 * The number of retrials until the correlation is aborted for this trace.
		 */
		private int retrialsLeft = 20;

		private MobileSpan span;

		public MobileSpanCorrelationTask(MobileSpan span) {
			this.span = span;
		}

		/**
		 * Schedules another trial for correlation.
		 *
		 * @param immediate
		 *            true if the attempt should be run immediately
		 */
		void schedule(boolean immediate) {
			if (retrialsLeft > 0) {
				retrialsLeft--;
				scheduledExecutor.schedule(this, immediate ? 0 : NUMBER_OF_SECONDS_BETWEEN_TRIALS, TimeUnit.SECONDS);
			}
		}

		boolean performCorrelation(MobileSpan sp) {
			for (AbstractSpan as : spanDao.getSpans(sp.getSpanIdent().getTraceId())) {
				if (as.getPlatformIdent() != 0) {
					sp.setPlatformIdent(as.getPlatformIdent());
					defaultDataDao.saveAll(Collections.singletonList(sp));
					return true;
				}
			}
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			boolean reschedule = true;
			try {
				if (performCorrelation(span)) {
					reschedule = false;
				}
			} finally {
				if (reschedule) {
					schedule(false); // retry later
				}
			}
		}
	}
}

