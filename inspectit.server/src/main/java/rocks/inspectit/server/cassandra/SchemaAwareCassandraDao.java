package rocks.inspectit.server.cassandra;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.LocalDate;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.EUMSpan;
import rocks.inspectit.shared.all.communication.data.eum.JSDomEvent;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest.NavigationTimings;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.UserSessionInfo;
import rocks.inspectit.shared.all.communication.data.mobile.ActivityStart;
import rocks.inspectit.shared.all.communication.data.mobile.UncaughtException;
import rocks.inspectit.shared.all.communication.data.mobile.BatteryConsumption;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;
import rocks.inspectit.shared.all.communication.data.mobile.IAdditionalTagSchema;
import rocks.inspectit.shared.all.communication.data.mobile.SystemResourceUsage;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author Jonas Kunz
 *
 */
@Component
public class SchemaAwareCassandraDao {

	private static final Logger LOG = LoggerFactory.getLogger(SchemaAwareCassandraDao.class);

	@Autowired
	CassandraDao cassandra;

	private PreparedStatement insertResourceLoad;
	private PreparedStatement insertAjax;
	private PreparedStatement insertPageLoad;
	private PreparedStatement insertRootDomEvent;

	private PreparedStatement insertAppCrash;
	private PreparedStatement insertBatteryConsumption;
	private PreparedStatement insertActivityStart;
	private PreparedStatement insertHttpRequest;
	private PreparedStatement insertResourceUsage;

	private AtomicBoolean isConnectedAndSchemaSetUp = new AtomicBoolean(false);

	private CassandraConnectionStateListener connectionListener = new CassandraConnectionStateListener() {
		@Override
		public void disconnected(CassandraDao cassandra) {
			isConnectedAndSchemaSetUp.set(false);
		}

		@Override
		public void connected(CassandraDao cassandra) {
			initSchema();
		}
	};

	@PostConstruct
	private void init() {
		synchronized (cassandra) {
			cassandra.addConnectionStateListener(connectionListener);
			if (isConnected()) {
				initSchema();
			}
		}
	}

	public boolean isConnected() {
		return cassandra.isConnected();
	}

	public void insertAjax(Optional<UserSessionInfo> sessionInfo, AjaxRequest ajax) {
		if (isConnectedAndSchemaSetUp.get()) {
			EUMSpan span = ajax.getOwningSpan();
			BoundStatement insert = insertAjax.bind()
					.setDate(CassandraSchema.AjaxRequests.DAY, LocalDate.fromMillisSinceEpoch(span.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.AjaxRequests.TIME, span.getTimeStamp())
					.setLong(CassandraSchema.AjaxRequests.TRACE_ID, span.getSpanIdent().getTraceId())
					.setLong(CassandraSchema.AjaxRequests.SPAN_ID, span.getSpanIdent().getId())
					.setLong(CassandraSchema.AjaxRequests.SESSION_ID, span.getSessionId())
					.setLong(CassandraSchema.AjaxRequests.TAB_ID, span.getTabId())
					.setDouble(CassandraSchema.AjaxRequests.DURATION, span.getDuration())
					.setString(CassandraSchema.AjaxRequests.URL, ajax.getUrl())
					.setString(CassandraSchema.AjaxRequests.BASE_URL, ajax.getBaseUrl())
					.setInt(CassandraSchema.AjaxRequests.STATUS, ajax.getStatus());
			sessionInfo.ifPresent((s) -> addSessionInfoToInsert(s, insert));
			listenForErrors(cassandra.execute(insert));
		}
	}

	public void insertResourceload(Optional<UserSessionInfo> sessionInfo, ResourceLoadRequest resource) {
		if (isConnectedAndSchemaSetUp.get()) {
			EUMSpan span = resource.getOwningSpan();
			BoundStatement insert = insertResourceLoad.bind()
					.setDate(CassandraSchema.ResourceLoadRequests.DAY, LocalDate.fromMillisSinceEpoch(span.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.ResourceLoadRequests.TIME, span.getTimeStamp())
					.setLong(CassandraSchema.ResourceLoadRequests.TRACE_ID, span.getSpanIdent().getTraceId())
					.setLong(CassandraSchema.ResourceLoadRequests.SPAN_ID, span.getSpanIdent().getId())
					.setLong(CassandraSchema.ResourceLoadRequests.SESSION_ID, span.getSessionId())
					.setLong(CassandraSchema.ResourceLoadRequests.TAB_ID, span.getTabId())
					.setDouble(CassandraSchema.ResourceLoadRequests.DURATION, span.getDuration())
					.setString(CassandraSchema.ResourceLoadRequests.URL, resource.getUrl()).setString(CassandraSchema.ResourceLoadRequests.BASE_URL, resource.getBaseUrl())
					.setString(CassandraSchema.ResourceLoadRequests.INITIATOR_TYPE, resource.getInitiatorType())
					.setLong(CassandraSchema.ResourceLoadRequests.TRANSFER_SIZE, resource.getTransferSize());
			sessionInfo.ifPresent((s) -> addSessionInfoToInsert(s, insert));
			listenForErrors(cassandra.execute(insert));
		}
	}



	public void insertRootDomEvent(Optional<UserSessionInfo> sessionInfo, JSDomEvent action) {
		if (isConnectedAndSchemaSetUp.get()) {
			EUMSpan span = action.getOwningSpan();
			BoundStatement insert = insertRootDomEvent.bind()
					.setDate(CassandraSchema.RootDomEvents.DAY, LocalDate.fromMillisSinceEpoch(span.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.RootDomEvents.TIME, span.getTimeStamp())
					.setLong(CassandraSchema.RootDomEvents.TRACE_ID, span.getSpanIdent().getTraceId())
					.setLong(CassandraSchema.RootDomEvents.SPAN_ID, span.getSpanIdent().getId())
					.setLong(CassandraSchema.RootDomEvents.SESSION_ID, span.getSessionId())
					.setLong(CassandraSchema.RootDomEvents.TAB_ID, span.getTabId())
					.setBool(CassandraSchema.RootDomEvents.RELEVANT_THROUGH_SELECTOR, action.isRelevantThroughSelector())
					.setString(CassandraSchema.RootDomEvents.BASE_URL, action.getBaseUrl())
					.setString(CassandraSchema.RootDomEvents.ACTION_TYPE, action.getEventType())
					.setMap(CassandraSchema.RootDomEvents.ELEMENT_INFO, action.getElementInfo());
			sessionInfo.ifPresent((s) -> addSessionInfoToInsert(s, insert));
			listenForErrors(cassandra.execute(insert));
		}
	}

	@SuppressWarnings("PMD.NullAssignment")
	public void insertPageload(Optional<UserSessionInfo> sessionInfo, PageLoadRequest load) {
		if (isConnectedAndSchemaSetUp.get()) {
			EUMSpan span = load.getOwningSpan();
			final NavigationTimings nt = load.getNavigationTimings();
			Function<Function<NavigationTimings, Double>, Timestamp> asTimestamp = (getter) -> (getter.apply(nt) != 0d ? new Timestamp(Math.round(getter.apply(nt))) : null);
			BoundStatement insert = insertPageLoad.bind()
					.setDate(CassandraSchema.PageLoadRequests.DAY, LocalDate.fromMillisSinceEpoch(span.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.PageLoadRequests.TIME, span.getTimeStamp())
					.setLong(CassandraSchema.PageLoadRequests.TRACE_ID, span.getSpanIdent().getTraceId())
					.setLong(CassandraSchema.PageLoadRequests.SPAN_ID, span.getSpanIdent().getId())
					.setLong(CassandraSchema.PageLoadRequests.SESSION_ID, span.getSessionId())
					.setLong(CassandraSchema.PageLoadRequests.TAB_ID, span.getTabId())
					.setDouble(CassandraSchema.PageLoadRequests.DURATION, span.getDuration())
					.setString(CassandraSchema.PageLoadRequests.URL, load.getUrl())
					.setInt(CassandraSchema.PageLoadRequests.RESOURCE_COUNT, load.getResourceCount());
			if (nt != null) {
				insert.setTimestamp(CassandraSchema.PageLoadRequests.FIRST_PAINT, asTimestamp.apply(NavigationTimings::getFirstPaint))
				.setTimestamp(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_START, asTimestamp.apply(NavigationTimings::getUnloadEventStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_END, asTimestamp.apply(NavigationTimings::getUnloadEventEnd))
				.setTimestamp(CassandraSchema.PageLoadRequests.REDIRECT_START, asTimestamp.apply(NavigationTimings::getRedirectStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.REDIRECT_END, asTimestamp.apply(NavigationTimings::getRedirectEnd))
				.setTimestamp(CassandraSchema.PageLoadRequests.FETCH_START, asTimestamp.apply(NavigationTimings::getFetchStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_START, asTimestamp.apply(NavigationTimings::getDomainLookupStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_END, asTimestamp.apply(NavigationTimings::getDomainLookupEnd))
				.setTimestamp(CassandraSchema.PageLoadRequests.CONNECT_START, asTimestamp.apply(NavigationTimings::getConnectStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.CONNECT_END, asTimestamp.apply(NavigationTimings::getConnectEnd))
				.setTimestamp(CassandraSchema.PageLoadRequests.SECURE_CONNECTION_START, asTimestamp.apply(NavigationTimings::getSecureConnectionStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.REQUEST_START, asTimestamp.apply(NavigationTimings::getRequestStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.RESPONSE_START, asTimestamp.apply(NavigationTimings::getResponseStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.RESPONSE_END, asTimestamp.apply(NavigationTimings::getResponseEnd))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOM_LOADING, asTimestamp.apply(NavigationTimings::getDomLoading))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOM_INTERACTIVE, asTimestamp.apply(NavigationTimings::getDomInteractive))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_START, asTimestamp.apply(NavigationTimings::getDomContentLoadedEventStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_END, asTimestamp.apply(NavigationTimings::getDomContentLoadedEventEnd))
				.setTimestamp(CassandraSchema.PageLoadRequests.DOM_COMPLETE, asTimestamp.apply(NavigationTimings::getDomComplete))
				.setTimestamp(CassandraSchema.PageLoadRequests.LOAD_EVENT_START, asTimestamp.apply(NavigationTimings::getLoadEventStart))
				.setTimestamp(CassandraSchema.PageLoadRequests.LOAD_EVENT_END, asTimestamp.apply(NavigationTimings::getLoadEventEnd));
				if (nt.getSpeedIndex() != 0d) {
					insert.setDouble(CassandraSchema.PageLoadRequests.SPEEDINDEX, nt.getSpeedIndex());
				}
			}
			sessionInfo.ifPresent((s) -> addSessionInfoToInsert(s, insert));
			listenForErrors(cassandra.execute(insert));
		}
	}

	public void insertAppCrash(UncaughtException crash) {
		if (isConnectedAndSchemaSetUp.get()) {
			BoundStatement insert = insertAppCrash.bind()
					.setDate(CassandraSchema.AppCrash.DAY, LocalDate.fromMillisSinceEpoch(crash.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.AppCrash.TIME, crash.getTimeStamp())
					.setString(CassandraSchema.AppCrash.SESSION_ID, crash.getSessionId())
					.setString(CassandraSchema.AppCrash.EXCEPTION_CLASS, crash.getExceptionClass())
					.setString(CassandraSchema.AppCrash.EXCEPTION_MESSAGE, crash.getExceptionMessage());
			addSessionTagsToInsert(crash.getSessionTags(), insert);

			listenForErrors(cassandra.execute(insert));
		}
	}

	public void insertHttpRequest(HttpNetworkRequest req) {
		if (isConnectedAndSchemaSetUp.get()) {
			BoundStatement insert = insertHttpRequest.bind()
					.setDate(CassandraSchema.HttpNetworkRequest.DAY, LocalDate.fromMillisSinceEpoch(req.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.HttpNetworkRequest.TIME, req.getTimeStamp())
					.setString(CassandraSchema.HttpNetworkRequest.SESSION_ID, req.getSessionId())
					.setString(CassandraSchema.HttpNetworkRequest.CONTENT_TYPE, req.getContentType())
					.setString(CassandraSchema.HttpNetworkRequest.METHOD, req.getMethod())
					.setLong(CassandraSchema.HttpNetworkRequest.DURATION, req.getDuration())
					.setInt(CassandraSchema.HttpNetworkRequest.RESPONSE_CODE, req.getResponseCode())
					.setString(CassandraSchema.HttpNetworkRequest.URL, req.getUrl());
			addSessionTagsToInsert(req.getSessionTags(), insert);

			listenForErrors(cassandra.execute(insert));
		}
	}

	public void insertActivityStart(ActivityStart st) {
		if (isConnectedAndSchemaSetUp.get()) {
			BoundStatement insert = insertActivityStart.bind()
					.setDate(CassandraSchema.ActivityStart.DAY, LocalDate.fromMillisSinceEpoch(st.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.ActivityStart.TIME, st.getTimeStamp())
					.setString(CassandraSchema.ActivityStart.SESSION_ID, st.getSessionId())
					.setString(CassandraSchema.ActivityStart.ACTIVITY_CLASS, st.getActivityClass())
					.setString(CassandraSchema.ActivityStart.ACTIVITY_NAME, st.getActivityName())
					.setTimestamp(CassandraSchema.ActivityStart.ACTIVITY_TIMESTAMP, new Timestamp(st.getStartTimestamp()));
			addSessionTagsToInsert(st.getSessionTags(), insert);

			listenForErrors(cassandra.execute(insert));
		}
	}

	public void insertResourceUsage(SystemResourceUsage usg) {
		if (isConnectedAndSchemaSetUp.get()) {
			BoundStatement insert = insertResourceUsage.bind()
					.setDate(CassandraSchema.ResourceUsage.DAY, LocalDate.fromMillisSinceEpoch(usg.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.ResourceUsage.TIME, usg.getTimeStamp())
					.setString(CassandraSchema.ResourceUsage.SESSION_ID, usg.getSessionId())
					.setFloat(CassandraSchema.ResourceUsage.CPU_USAGE, usg.getCpuUsage())
					.setFloat(CassandraSchema.ResourceUsage.MEMROY_USAGE, usg.getMemoryUsage());
			addSessionTagsToInsert(usg.getSessionTags(), insert);

			listenForErrors(cassandra.execute(insert));
		}
	}

	public void insertBatteryConsumption(BatteryConsumption con) {
		if (isConnectedAndSchemaSetUp.get()) {
			BoundStatement insert = insertBatteryConsumption.bind()
					.setDate(CassandraSchema.BatteryConsumption.DAY, LocalDate.fromMillisSinceEpoch(con.getTimeStamp().getTime()))
					.setTimestamp(CassandraSchema.BatteryConsumption.TIME, con.getTimeStamp())
					.setString(CassandraSchema.BatteryConsumption.SESSION_ID, con.getSessionId())
					.setFloat(CassandraSchema.BatteryConsumption.CONSUMPTION_VALUE, con.getConsumptionPercent())
					.setLong(CassandraSchema.BatteryConsumption.CONSUMPTION_INTERVAL, con.getTimeInterval());
			addSessionTagsToInsert(con.getSessionTags(), insert);

			listenForErrors(cassandra.execute(insert));
		}
	}

	private void addSessionTagsToInsert(List<Pair<String, String>> tags, BoundStatement insert) {
		if (tags != null) {
			for (Pair<String, String> tag : tags) {
				String key = tag.getFirst();
				if (key.equals(IAdditionalTagSchema.ANDROID_VERSION)) {
					insert.setString(CassandraSchema.MobileTable.ANDROID_VERSION, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.APP_VERSION)) {
					insert.setString(CassandraSchema.MobileTable.APP_VERSION, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.APP_NAME)) {
					insert.setString(CassandraSchema.MobileTable.APP_NAME, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.DEVICE_NAME)) {
					insert.setString(CassandraSchema.MobileTable.DEVICE_NAME, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.DEVICE_LANG)) {
					insert.setString(CassandraSchema.MobileTable.DEVICE_LANG, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.DEVICE_LON)) {
					insert.setDouble(CassandraSchema.MobileTable.POS_LON, Double.parseDouble(tag.getSecond()));
				} else if (key.equals(IAdditionalTagSchema.DEVICE_LAT)) {
					insert.setDouble(CassandraSchema.MobileTable.POS_LAT, Double.parseDouble(tag.getSecond()));
				}
			}
		}
	}

	private void addSessionInfoToInsert(UserSessionInfo sessionInfo, BoundStatement insert) {
		insert
		.setString(CassandraSchema.EumTable.BROWSER, sessionInfo.getBrowser())
		.setString(CassandraSchema.EumTable.DEVICE, sessionInfo.getDevice())
		.setString(CassandraSchema.EumTable.LANGUAGE, sessionInfo.getLanguage());
	}


	private void listenForErrors(ListenableFuture<ResultSet> future) {
		future.addListener(() -> {
			try {
				future.get();
			} catch (ExecutionException e) {
				LOG.error("Error executing query!", e.getCause());
			} catch (InterruptedException e) {
				LOG.error("Query interrupted!", e.getCause());
			}
		}, MoreExecutors.directExecutor());
	}

	private void initSchema() {
		try {
			initEUMTables().get();
			prepareEUMStatements().get();
			isConnectedAndSchemaSetUp.set(true);
		} catch (Exception e) {
			LOG.error("Error setting up schema", e);
			isConnectedAndSchemaSetUp.set(false);
		}
	}

	private Future<?> initEUMTables() {
		ListenableFuture<ResultSet> ajaxFut = cassandra.execute(
				SchemaBuilder.createTable(CassandraSchema.AjaxRequests.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.AjaxRequests.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.AjaxRequests.TIME, DataType.timestamp())
				.addClusteringColumn(CassandraSchema.AjaxRequests.SPAN_ID, DataType.bigint())
				.addColumn(CassandraSchema.AjaxRequests.TRACE_ID, DataType.bigint())
				.addColumn(CassandraSchema.AjaxRequests.SESSION_ID, DataType.bigint())
				.addColumn(CassandraSchema.AjaxRequests.TAB_ID, DataType.bigint())
				.addColumn(CassandraSchema.AjaxRequests.BROWSER, DataType.text())
				.addColumn(CassandraSchema.AjaxRequests.DEVICE, DataType.text())
				.addColumn(CassandraSchema.AjaxRequests.LANGUAGE, DataType.text())
				.addColumn(CassandraSchema.AjaxRequests.DURATION, DataType.cdouble())
				.addColumn(CassandraSchema.AjaxRequests.URL, DataType.text())
				.addColumn(CassandraSchema.AjaxRequests.BASE_URL, DataType.text())
				.addColumn(CassandraSchema.AjaxRequests.STATUS, DataType.cint()));
		ListenableFuture<ResultSet> resourceFut = cassandra.execute(
				SchemaBuilder.createTable(CassandraSchema.ResourceLoadRequests.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.ResourceLoadRequests.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.ResourceLoadRequests.TIME, DataType.timestamp())
				.addClusteringColumn(CassandraSchema.ResourceLoadRequests.SPAN_ID, DataType.bigint())
				.addColumn(CassandraSchema.ResourceLoadRequests.TRACE_ID, DataType.bigint())
				.addColumn(CassandraSchema.ResourceLoadRequests.SESSION_ID, DataType.bigint())
				.addColumn(CassandraSchema.ResourceLoadRequests.TAB_ID, DataType.bigint())
				.addColumn(CassandraSchema.ResourceLoadRequests.BROWSER, DataType.text())
				.addColumn(CassandraSchema.ResourceLoadRequests.DEVICE, DataType.text())
				.addColumn(CassandraSchema.ResourceLoadRequests.LANGUAGE, DataType.text())
				.addColumn(CassandraSchema.ResourceLoadRequests.DURATION, DataType.cdouble())
				.addColumn(CassandraSchema.ResourceLoadRequests.URL, DataType.text())
				.addColumn(CassandraSchema.ResourceLoadRequests.BASE_URL, DataType.text())
				.addColumn(CassandraSchema.ResourceLoadRequests.TRANSFER_SIZE, DataType.bigint())
				.addColumn(CassandraSchema.ResourceLoadRequests.INITIATOR_TYPE, DataType.text()));
		ListenableFuture<ResultSet> pageloadFut = cassandra.execute(
				SchemaBuilder.createTable(CassandraSchema.PageLoadRequests.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.PageLoadRequests.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.PageLoadRequests.TIME, DataType.timestamp())
				.addClusteringColumn(CassandraSchema.PageLoadRequests.SPAN_ID, DataType.bigint())
				.addColumn(CassandraSchema.PageLoadRequests.TRACE_ID, DataType.bigint())
				.addColumn(CassandraSchema.PageLoadRequests.SESSION_ID, DataType.bigint())
				.addColumn(CassandraSchema.PageLoadRequests.TAB_ID, DataType.bigint())
				.addColumn(CassandraSchema.PageLoadRequests.BROWSER, DataType.text())
				.addColumn(CassandraSchema.PageLoadRequests.DEVICE, DataType.text())
				.addColumn(CassandraSchema.PageLoadRequests.LANGUAGE, DataType.text())
				.addColumn(CassandraSchema.PageLoadRequests.DURATION, DataType.cdouble())
				.addColumn(CassandraSchema.PageLoadRequests.URL, DataType.text())
				.addColumn(CassandraSchema.PageLoadRequests.RESOURCE_COUNT, DataType.cint())
				.addColumn(CassandraSchema.PageLoadRequests.SPEEDINDEX, DataType.cdouble())
				.addColumn(CassandraSchema.PageLoadRequests.FIRST_PAINT, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_END, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.REDIRECT_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.REDIRECT_END, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.FETCH_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_END, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.CONNECT_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.CONNECT_END, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.SECURE_CONNECTION_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.REQUEST_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.RESPONSE_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.RESPONSE_END, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOM_LOADING, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOM_INTERACTIVE, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_END, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.DOM_COMPLETE, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.LOAD_EVENT_START, DataType.timestamp())
				.addColumn(CassandraSchema.PageLoadRequests.LOAD_EVENT_END, DataType.timestamp()));
		ListenableFuture<ResultSet> domEventsFut = cassandra.execute(
				SchemaBuilder.createTable(CassandraSchema.RootDomEvents.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.RootDomEvents.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.RootDomEvents.TIME, DataType.timestamp())
				.addClusteringColumn(CassandraSchema.RootDomEvents.SPAN_ID, DataType.bigint())
				.addColumn(CassandraSchema.RootDomEvents.TRACE_ID, DataType.bigint())
				.addColumn(CassandraSchema.RootDomEvents.SESSION_ID, DataType.bigint())
				.addColumn(CassandraSchema.RootDomEvents.TAB_ID, DataType.bigint())
				.addColumn(CassandraSchema.RootDomEvents.BROWSER, DataType.text())
				.addColumn(CassandraSchema.RootDomEvents.DEVICE, DataType.text())
				.addColumn(CassandraSchema.RootDomEvents.LANGUAGE, DataType.text())
				.addColumn(CassandraSchema.RootDomEvents.RELEVANT_THROUGH_SELECTOR, DataType.cboolean())
				.addColumn(CassandraSchema.RootDomEvents.BASE_URL, DataType.text())
				.addColumn(CassandraSchema.RootDomEvents.ACTION_TYPE, DataType.text())
				.addColumn(CassandraSchema.RootDomEvents.ELEMENT_INFO, DataType.map(DataType.text(), DataType.text())));

		// mobile
		ListenableFuture<ResultSet> appCrashFut = cassandra.execute(SchemaBuilder.createTable(CassandraSchema.AppCrash.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.AppCrash.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.AppCrash.TIME, DataType.timestamp())
				.addColumn(CassandraSchema.AppCrash.SESSION_ID, DataType.text())
				.addColumn(CassandraSchema.AppCrash.ANDROID_VERSION, DataType.text())
				.addColumn(CassandraSchema.AppCrash.APP_NAME, DataType.text())
				.addColumn(CassandraSchema.AppCrash.APP_VERSION, DataType.text())
				.addColumn(CassandraSchema.AppCrash.DEVICE_NAME, DataType.text())
				.addColumn(CassandraSchema.AppCrash.DEVICE_LANG, DataType.text())
				.addColumn(CassandraSchema.AppCrash.POS_LON, DataType.cdouble())
				.addColumn(CassandraSchema.AppCrash.POS_LAT, DataType.cdouble())
				.addColumn(CassandraSchema.AppCrash.EXCEPTION_CLASS, DataType.text())
				.addColumn(CassandraSchema.AppCrash.EXCEPTION_MESSAGE, DataType.text()));

		ListenableFuture<ResultSet> httpReqFuture = cassandra.execute(SchemaBuilder.createTable(CassandraSchema.HttpNetworkRequest.TABLE_NAME).ifNotExists()
				.addPartitionKey(CassandraSchema.HttpNetworkRequest.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.HttpNetworkRequest.TIME, DataType.timestamp())
				.addColumn(CassandraSchema.HttpNetworkRequest.SESSION_ID, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.ANDROID_VERSION, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.APP_NAME, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.APP_VERSION, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.DEVICE_NAME, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.DEVICE_LANG, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.POS_LON, DataType.cdouble())
				.addColumn(CassandraSchema.HttpNetworkRequest.POS_LAT, DataType.cdouble())
				.addColumn(CassandraSchema.HttpNetworkRequest.CONTENT_TYPE, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.DURATION, DataType.bigint())
				.addColumn(CassandraSchema.HttpNetworkRequest.METHOD, DataType.text())
				.addColumn(CassandraSchema.HttpNetworkRequest.RESPONSE_CODE, DataType.cint())
				.addColumn(CassandraSchema.HttpNetworkRequest.URL, DataType.text()));

		ListenableFuture<ResultSet> actStartFuture = cassandra.execute(SchemaBuilder.createTable(CassandraSchema.ActivityStart.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.ActivityStart.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.ActivityStart.TIME, DataType.timestamp())
				.addColumn(CassandraSchema.ActivityStart.SESSION_ID, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.ANDROID_VERSION, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.APP_NAME, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.APP_VERSION, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.DEVICE_NAME, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.DEVICE_LANG, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.POS_LON, DataType.cdouble())
				.addColumn(CassandraSchema.ActivityStart.POS_LAT, DataType.cdouble())
				.addColumn(CassandraSchema.ActivityStart.ACTIVITY_CLASS, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.ACTIVITY_NAME, DataType.text())
				.addColumn(CassandraSchema.ActivityStart.ACTIVITY_TIMESTAMP, DataType.timestamp()));

		ListenableFuture<ResultSet> consumptionFuture = cassandra.execute(SchemaBuilder.createTable(CassandraSchema.BatteryConsumption.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.BatteryConsumption.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.BatteryConsumption.TIME, DataType.timestamp())
				.addColumn(CassandraSchema.BatteryConsumption.SESSION_ID, DataType.text())
				.addColumn(CassandraSchema.BatteryConsumption.ANDROID_VERSION, DataType.text())
				.addColumn(CassandraSchema.BatteryConsumption.APP_NAME, DataType.text())
				.addColumn(CassandraSchema.BatteryConsumption.APP_VERSION, DataType.text())
				.addColumn(CassandraSchema.BatteryConsumption.DEVICE_NAME, DataType.text())
				.addColumn(CassandraSchema.BatteryConsumption.DEVICE_LANG, DataType.text())
				.addColumn(CassandraSchema.BatteryConsumption.POS_LON, DataType.cdouble())
				.addColumn(CassandraSchema.BatteryConsumption.POS_LAT, DataType.cdouble())
				.addColumn(CassandraSchema.BatteryConsumption.CONSUMPTION_INTERVAL, DataType.bigint())
				.addColumn(CassandraSchema.BatteryConsumption.CONSUMPTION_VALUE, DataType.cfloat()));

		ListenableFuture<ResultSet> resourceUsageFut = cassandra.execute(SchemaBuilder.createTable(CassandraSchema.ResourceUsage.TABLE_NAME)
				.ifNotExists()
				.addPartitionKey(CassandraSchema.ResourceUsage.DAY, DataType.date())
				.addClusteringColumn(CassandraSchema.ResourceUsage.TIME, DataType.timestamp())
				.addColumn(CassandraSchema.ResourceUsage.ANDROID_VERSION, DataType.text())
				.addColumn(CassandraSchema.ResourceUsage.APP_NAME, DataType.text())
				.addColumn(CassandraSchema.ResourceUsage.APP_VERSION, DataType.text())
				.addColumn(CassandraSchema.ResourceUsage.DEVICE_NAME, DataType.text())
				.addColumn(CassandraSchema.ResourceUsage.DEVICE_LANG, DataType.text())
				.addColumn(CassandraSchema.ResourceUsage.POS_LON, DataType.cdouble())
				.addColumn(CassandraSchema.ResourceUsage.POS_LAT, DataType.cdouble())
				.addColumn(CassandraSchema.ResourceUsage.SESSION_ID, DataType.text())
				.addColumn(CassandraSchema.ResourceUsage.CPU_USAGE, DataType.cfloat())
				.addColumn(CassandraSchema.ResourceUsage.MEMROY_USAGE, DataType.cfloat()));

		@SuppressWarnings("unchecked")
		ListenableFuture<List<ResultSet>> allFutures = Futures.allAsList(ajaxFut, pageloadFut, resourceFut, domEventsFut, appCrashFut, httpReqFuture, actStartFuture, consumptionFuture,
				resourceUsageFut);
		return allFutures;
	}

	private Future<?> prepareEUMStatements() {
		ListenableFuture<PreparedStatement> ajaxFut = cassandra.prepare(
				QueryBuilder.insertInto(CassandraSchema.AjaxRequests.TABLE_NAME)
				.value(CassandraSchema.AjaxRequests.DAY, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.DAY))
				.value(CassandraSchema.AjaxRequests.TIME, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.TIME))
				.value(CassandraSchema.AjaxRequests.TRACE_ID, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.TRACE_ID))
				.value(CassandraSchema.AjaxRequests.SPAN_ID, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.SPAN_ID))
				.value(CassandraSchema.AjaxRequests.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.SESSION_ID))
				.value(CassandraSchema.AjaxRequests.TAB_ID, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.TAB_ID))
				.value(CassandraSchema.AjaxRequests.BROWSER, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.BROWSER))
				.value(CassandraSchema.AjaxRequests.DEVICE, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.DEVICE))
				.value(CassandraSchema.AjaxRequests.LANGUAGE, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.LANGUAGE))
				.value(CassandraSchema.AjaxRequests.DURATION, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.DURATION))
				.value(CassandraSchema.AjaxRequests.URL, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.URL))
				.value(CassandraSchema.AjaxRequests.BASE_URL, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.BASE_URL))
				.value(CassandraSchema.AjaxRequests.STATUS, QueryBuilder.bindMarker(CassandraSchema.AjaxRequests.STATUS)));
		ListenableFuture<PreparedStatement> resourceFut = cassandra.prepare(QueryBuilder.insertInto(CassandraSchema.ResourceLoadRequests.TABLE_NAME)
				.value(CassandraSchema.ResourceLoadRequests.DAY, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.DAY))
				.value(CassandraSchema.ResourceLoadRequests.TIME, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.TIME))
				.value(CassandraSchema.ResourceLoadRequests.TRACE_ID, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.TRACE_ID))
				.value(CassandraSchema.ResourceLoadRequests.SPAN_ID, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.SPAN_ID))
				.value(CassandraSchema.ResourceLoadRequests.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.SESSION_ID))
				.value(CassandraSchema.ResourceLoadRequests.TAB_ID, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.TAB_ID))
				.value(CassandraSchema.ResourceLoadRequests.BROWSER, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.BROWSER))
				.value(CassandraSchema.ResourceLoadRequests.DEVICE, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.DEVICE))
				.value(CassandraSchema.ResourceLoadRequests.LANGUAGE, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.LANGUAGE))
				.value(CassandraSchema.ResourceLoadRequests.DURATION, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.DURATION))
				.value(CassandraSchema.ResourceLoadRequests.URL, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.URL))
				.value(CassandraSchema.ResourceLoadRequests.BASE_URL, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.BASE_URL))
				.value(CassandraSchema.ResourceLoadRequests.TRANSFER_SIZE, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.TRANSFER_SIZE))
				.value(CassandraSchema.ResourceLoadRequests.INITIATOR_TYPE, QueryBuilder.bindMarker(CassandraSchema.ResourceLoadRequests.INITIATOR_TYPE)));
		ListenableFuture<PreparedStatement> domEventsFut =  cassandra.prepare(
				QueryBuilder.insertInto(CassandraSchema.RootDomEvents.TABLE_NAME)
				.value(CassandraSchema.RootDomEvents.DAY, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.DAY))
				.value(CassandraSchema.RootDomEvents.TIME, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.TIME))
				.value(CassandraSchema.RootDomEvents.TRACE_ID, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.TRACE_ID))
				.value(CassandraSchema.RootDomEvents.SPAN_ID, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.SPAN_ID))
				.value(CassandraSchema.RootDomEvents.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.SESSION_ID))
				.value(CassandraSchema.RootDomEvents.TAB_ID, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.TAB_ID))
				.value(CassandraSchema.RootDomEvents.BROWSER, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.BROWSER))
				.value(CassandraSchema.RootDomEvents.DEVICE, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.DEVICE))
				.value(CassandraSchema.RootDomEvents.LANGUAGE, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.LANGUAGE))
				.value(CassandraSchema.RootDomEvents.RELEVANT_THROUGH_SELECTOR, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.RELEVANT_THROUGH_SELECTOR))
				.value(CassandraSchema.RootDomEvents.BASE_URL, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.BASE_URL))
				.value(CassandraSchema.RootDomEvents.ACTION_TYPE, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.ACTION_TYPE))
				.value(CassandraSchema.RootDomEvents.ELEMENT_INFO, QueryBuilder.bindMarker(CassandraSchema.RootDomEvents.ELEMENT_INFO)));
		ListenableFuture<PreparedStatement> pageloadFut = cassandra.prepare(
				QueryBuilder.insertInto(CassandraSchema.PageLoadRequests.TABLE_NAME)
				.value(CassandraSchema.PageLoadRequests.DAY, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DAY))
				.value(CassandraSchema.PageLoadRequests.TIME, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.TIME))
				.value(CassandraSchema.PageLoadRequests.TRACE_ID, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.TRACE_ID))
				.value(CassandraSchema.PageLoadRequests.SPAN_ID, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.SPAN_ID))
				.value(CassandraSchema.PageLoadRequests.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.SESSION_ID))
				.value(CassandraSchema.PageLoadRequests.TAB_ID, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.TAB_ID))
				.value(CassandraSchema.PageLoadRequests.BROWSER, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.BROWSER))
				.value(CassandraSchema.PageLoadRequests.DEVICE, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DEVICE))
				.value(CassandraSchema.PageLoadRequests.LANGUAGE, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.LANGUAGE))
				.value(CassandraSchema.PageLoadRequests.DURATION, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DURATION))
				.value(CassandraSchema.PageLoadRequests.URL, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.URL))
				.value(CassandraSchema.PageLoadRequests.RESOURCE_COUNT, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.RESOURCE_COUNT))
				.value(CassandraSchema.PageLoadRequests.SPEEDINDEX, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.SPEEDINDEX))
				.value(CassandraSchema.PageLoadRequests.FIRST_PAINT, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.FIRST_PAINT))
				.value(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_START))
				.value(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.UNLOAD_EVENT_END))
				.value(CassandraSchema.PageLoadRequests.REDIRECT_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.REDIRECT_START))
				.value(CassandraSchema.PageLoadRequests.REDIRECT_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.REDIRECT_END))
				.value(CassandraSchema.PageLoadRequests.FETCH_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.FETCH_START))
				.value(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_START))
				.value(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOMAIN_LOOKUP_END))
				.value(CassandraSchema.PageLoadRequests.CONNECT_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.CONNECT_START))
				.value(CassandraSchema.PageLoadRequests.CONNECT_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.CONNECT_END))
				.value(CassandraSchema.PageLoadRequests.SECURE_CONNECTION_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.SECURE_CONNECTION_START))
				.value(CassandraSchema.PageLoadRequests.REQUEST_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.REQUEST_START))
				.value(CassandraSchema.PageLoadRequests.RESPONSE_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.RESPONSE_START))
				.value(CassandraSchema.PageLoadRequests.RESPONSE_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.RESPONSE_END))
				.value(CassandraSchema.PageLoadRequests.DOM_LOADING, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOM_LOADING))
				.value(CassandraSchema.PageLoadRequests.DOM_INTERACTIVE, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOM_INTERACTIVE))
				.value(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_START))
				.value(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOM_CONTENT_LOADED_EVENT_END))
				.value(CassandraSchema.PageLoadRequests.DOM_COMPLETE, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.DOM_COMPLETE))
				.value(CassandraSchema.PageLoadRequests.LOAD_EVENT_START, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.LOAD_EVENT_START))
				.value(CassandraSchema.PageLoadRequests.LOAD_EVENT_END, QueryBuilder.bindMarker(CassandraSchema.PageLoadRequests.LOAD_EVENT_END)));

		// mobile prepared statements
		// 1. app crash
		ListenableFuture<PreparedStatement> appCrashFut = cassandra
				.prepare(QueryBuilder.insertInto(CassandraSchema.AppCrash.TABLE_NAME)
						.value(CassandraSchema.AppCrash.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.AppCrash.SESSION_ID))
						.value(CassandraSchema.AppCrash.ANDROID_VERSION, QueryBuilder.bindMarker(CassandraSchema.AppCrash.ANDROID_VERSION))
						.value(CassandraSchema.AppCrash.APP_NAME, QueryBuilder.bindMarker(CassandraSchema.AppCrash.APP_NAME))
						.value(CassandraSchema.AppCrash.APP_VERSION, QueryBuilder.bindMarker(CassandraSchema.AppCrash.APP_VERSION))
						.value(CassandraSchema.AppCrash.DEVICE_NAME, QueryBuilder.bindMarker(CassandraSchema.AppCrash.DEVICE_NAME))
						.value(CassandraSchema.AppCrash.DEVICE_LANG, QueryBuilder.bindMarker(CassandraSchema.AppCrash.DEVICE_LANG))
						.value(CassandraSchema.AppCrash.POS_LON, QueryBuilder.bindMarker(CassandraSchema.AppCrash.POS_LON))
						.value(CassandraSchema.AppCrash.POS_LAT, QueryBuilder.bindMarker(CassandraSchema.AppCrash.POS_LAT))
						.value(CassandraSchema.AppCrash.DAY, QueryBuilder.bindMarker(CassandraSchema.AppCrash.DAY))
						.value(CassandraSchema.AppCrash.TIME, QueryBuilder.bindMarker(CassandraSchema.AppCrash.TIME))
						.value(CassandraSchema.AppCrash.EXCEPTION_CLASS, QueryBuilder.bindMarker(CassandraSchema.AppCrash.EXCEPTION_CLASS))
						.value(CassandraSchema.AppCrash.EXCEPTION_MESSAGE,
								QueryBuilder.bindMarker(CassandraSchema.AppCrash.EXCEPTION_MESSAGE)));

		// 2. network request
		ListenableFuture<PreparedStatement> httpReqFut = cassandra.prepare(QueryBuilder.insertInto(CassandraSchema.HttpNetworkRequest.TABLE_NAME)
				.value(CassandraSchema.HttpNetworkRequest.TIME, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.TIME))
				.value(CassandraSchema.HttpNetworkRequest.DAY, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.DAY))
				.value(CassandraSchema.HttpNetworkRequest.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.SESSION_ID))
				.value(CassandraSchema.HttpNetworkRequest.ANDROID_VERSION, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.ANDROID_VERSION))
				.value(CassandraSchema.HttpNetworkRequest.APP_NAME, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.APP_NAME))
				.value(CassandraSchema.HttpNetworkRequest.APP_VERSION, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.APP_VERSION))
				.value(CassandraSchema.HttpNetworkRequest.DEVICE_NAME, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.DEVICE_NAME))
				.value(CassandraSchema.HttpNetworkRequest.DEVICE_LANG, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.DEVICE_LANG))
				.value(CassandraSchema.HttpNetworkRequest.POS_LON, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.POS_LON))
				.value(CassandraSchema.HttpNetworkRequest.POS_LAT, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.POS_LAT))
				.value(CassandraSchema.HttpNetworkRequest.RESPONSE_CODE, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.RESPONSE_CODE))
				.value(CassandraSchema.HttpNetworkRequest.METHOD, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.METHOD))
				.value(CassandraSchema.HttpNetworkRequest.CONTENT_TYPE, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.CONTENT_TYPE))
				.value(CassandraSchema.HttpNetworkRequest.DURATION, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.DURATION))
				.value(CassandraSchema.HttpNetworkRequest.URL, QueryBuilder.bindMarker(CassandraSchema.HttpNetworkRequest.URL)));

		// 3. activity start
		ListenableFuture<PreparedStatement> activityFut = cassandra.prepare(QueryBuilder.insertInto(CassandraSchema.ActivityStart.TABLE_NAME)
				.value(CassandraSchema.ActivityStart.TIME, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.TIME))
				.value(CassandraSchema.ActivityStart.DAY, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.DAY))
				.value(CassandraSchema.ActivityStart.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.SESSION_ID))
				.value(CassandraSchema.ActivityStart.ANDROID_VERSION, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.ANDROID_VERSION))
				.value(CassandraSchema.ActivityStart.APP_NAME, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.APP_NAME))
				.value(CassandraSchema.ActivityStart.APP_VERSION, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.APP_VERSION))
				.value(CassandraSchema.ActivityStart.DEVICE_NAME, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.DEVICE_NAME))
				.value(CassandraSchema.ActivityStart.DEVICE_LANG, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.DEVICE_LANG))
				.value(CassandraSchema.ActivityStart.POS_LON, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.POS_LON))
				.value(CassandraSchema.ActivityStart.POS_LAT, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.POS_LAT))
				.value(CassandraSchema.ActivityStart.ACTIVITY_CLASS, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.ACTIVITY_CLASS))
				.value(CassandraSchema.ActivityStart.ACTIVITY_NAME, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.ACTIVITY_NAME))
				.value(CassandraSchema.ActivityStart.ACTIVITY_TIMESTAMP, QueryBuilder.bindMarker(CassandraSchema.ActivityStart.ACTIVITY_TIMESTAMP)));

		// 4. consumption
		ListenableFuture<PreparedStatement> consumptionFut = cassandra.prepare(QueryBuilder.insertInto(CassandraSchema.BatteryConsumption.TABLE_NAME)
				.value(CassandraSchema.BatteryConsumption.TIME, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.TIME))
				.value(CassandraSchema.BatteryConsumption.DAY, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.DAY))
				.value(CassandraSchema.BatteryConsumption.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.SESSION_ID))
				.value(CassandraSchema.BatteryConsumption.ANDROID_VERSION, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.ANDROID_VERSION))
				.value(CassandraSchema.BatteryConsumption.APP_NAME, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.APP_NAME))
				.value(CassandraSchema.BatteryConsumption.APP_VERSION, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.APP_VERSION))
				.value(CassandraSchema.BatteryConsumption.DEVICE_NAME, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.DEVICE_NAME))
				.value(CassandraSchema.BatteryConsumption.DEVICE_LANG, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.DEVICE_LANG))
				.value(CassandraSchema.BatteryConsumption.POS_LON, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.POS_LON))
				.value(CassandraSchema.BatteryConsumption.POS_LAT, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.POS_LAT))
				.value(CassandraSchema.BatteryConsumption.CONSUMPTION_VALUE, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.CONSUMPTION_VALUE))
				.value(CassandraSchema.BatteryConsumption.CONSUMPTION_INTERVAL, QueryBuilder.bindMarker(CassandraSchema.BatteryConsumption.CONSUMPTION_INTERVAL)));

		// 5. resource usage
		ListenableFuture<PreparedStatement> resourceUsageFut = cassandra.prepare(QueryBuilder.insertInto(CassandraSchema.ResourceUsage.TABLE_NAME)
				.value(CassandraSchema.ResourceUsage.TIME, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.TIME))
				.value(CassandraSchema.ResourceUsage.DAY, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.DAY))
				.value(CassandraSchema.ResourceUsage.SESSION_ID, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.SESSION_ID))
				.value(CassandraSchema.ResourceUsage.ANDROID_VERSION, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.ANDROID_VERSION))
				.value(CassandraSchema.ResourceUsage.APP_NAME, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.APP_NAME))
				.value(CassandraSchema.ResourceUsage.APP_VERSION, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.APP_VERSION))
				.value(CassandraSchema.ResourceUsage.DEVICE_NAME, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.DEVICE_NAME))
				.value(CassandraSchema.ResourceUsage.DEVICE_LANG, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.DEVICE_LANG))
				.value(CassandraSchema.ResourceUsage.POS_LON, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.POS_LON))
				.value(CassandraSchema.ResourceUsage.POS_LAT, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.POS_LAT))
				.value(CassandraSchema.ResourceUsage.CPU_USAGE, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.CPU_USAGE))
				.value(CassandraSchema.ResourceUsage.MEMROY_USAGE, QueryBuilder.bindMarker(CassandraSchema.ResourceUsage.MEMROY_USAGE)));

		@SuppressWarnings("unchecked")
		ListenableFuture<List<PreparedStatement>> allFutures = Futures.allAsList(resourceFut, ajaxFut, pageloadFut, domEventsFut, appCrashFut, consumptionFut, activityFut, httpReqFut,
				resourceUsageFut);
		allFutures.addListener(() -> {
			try {
				insertAjax = ajaxFut.get();
				insertRootDomEvent = domEventsFut.get();
				insertPageLoad = pageloadFut.get();
				insertResourceLoad = resourceFut.get();
				insertAppCrash = appCrashFut.get();
				insertBatteryConsumption = consumptionFut.get();
				insertActivityStart = activityFut.get();
				insertHttpRequest = httpReqFut.get();
				insertResourceUsage = resourceUsageFut.get();
			} catch (Exception e) {
				LOG.error("Error preparing statements: ", e);
			}
		}, MoreExecutors.directExecutor());
		return allFutures;
	}


}
