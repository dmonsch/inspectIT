package rocks.inspectit.server.elasticsearch;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rocks.inspectit.shared.all.communication.data.mobile.ActivityStart;
import rocks.inspectit.shared.all.communication.data.mobile.BatteryConsumption;
import rocks.inspectit.shared.all.communication.data.mobile.HttpNetworkRequest;
import rocks.inspectit.shared.all.communication.data.mobile.IAdditionalTagSchema;
import rocks.inspectit.shared.all.communication.data.mobile.SystemResourceUsage;
import rocks.inspectit.shared.all.communication.data.mobile.UncaughtException;
import rocks.inspectit.shared.all.util.Pair;

/**
 * @author David Monschein
 *
 */
@Component
public class SchemaAwareElasticSearchDao {

	private static final String GEOPOINT_FIELD = "coordinates";

	private static final Logger LOG = LoggerFactory.getLogger(SchemaAwareElasticSearchDao.class);

	@Autowired
	ElasticSearchDao elasticSearch;

	private AtomicBoolean isConnectedAndSchemaSetUp = new AtomicBoolean(false);

	private ElasticSearchConnectionStateListener connectionListener = new ElasticSearchConnectionStateListener() {
		@Override
		public void disconnected(ElasticSearchDao es) {
			isConnectedAndSchemaSetUp.set(false);
		}

		@Override
		public void connected(ElasticSearchDao es) {
			// init schema
			initIndex();
			isConnectedAndSchemaSetUp.set(true);
		}
	};

	private ActionListener<IndexResponse> indexResponseListener = new ActionListener<IndexResponse>() {
		@Override
		public void onResponse(IndexResponse response) {
			// dont care atm
		}

		@Override
		public void onFailure(Exception e) {
			LOG.warn("Could not index a document.", e);
		}
	};

	public void insertActivityStart(ActivityStart start) {
		if (isConnectedAndSchemaSetUp.get()) {
			IndexRequest ir = new IndexRequest(ElasticSearchSchema.ActivityStart.INDEX_NAME, ElasticSearchSchema.ActivityStart.INDEX_NAME);
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();

				builder.field(ElasticSearchSchema.ActivityStart.ACTIVITY_CLASS, start.getActivityClass());
				builder.field(ElasticSearchSchema.ActivityStart.ACTIVITY_NAME, start.getActivityName());
				builder.field(ElasticSearchSchema.ActivityStart.ACTIVITY_TIMESTAMP, start.getStartTimestamp());
				builder.field(ElasticSearchSchema.ActivityStart.TIMESTAMP, start.getTimeStamp().getTime());
				builder.field(ElasticSearchSchema.ActivityStart.SESSION_ID, start.getSessionId());

				enrichWithSessionData(start.getSessionTags(), builder);

				builder.endObject();

				ir.source(builder);

				elasticSearch.executeIndexRequest(ir, indexResponseListener);
			} catch (IOException e) {
				LOG.warn("Could not index a HttpNetworkRequest object.", e);
			}
		}
	}

	public void insertUncaughtException(UncaughtException crash) {
		if (isConnectedAndSchemaSetUp.get()) {
			IndexRequest ir = new IndexRequest(ElasticSearchSchema.UncaughtException.INDEX_NAME, ElasticSearchSchema.UncaughtException.INDEX_NAME);
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();

				builder.field(ElasticSearchSchema.UncaughtException.EXCEPTION_CLASS, crash.getExceptionClass());
				builder.field(ElasticSearchSchema.UncaughtException.EXCEPTION_MESSAGE, crash.getExceptionMessage());
				builder.field(ElasticSearchSchema.UncaughtException.TIMESTAMP, crash.getTimeStamp().getTime());
				builder.field(ElasticSearchSchema.UncaughtException.SESSION_ID, crash.getSessionId());

				enrichWithSessionData(crash.getSessionTags(), builder);

				builder.endObject();

				ir.source(builder);

				elasticSearch.executeIndexRequest(ir, indexResponseListener);
			} catch (IOException e) {
				LOG.warn("Could not index a HttpNetworkRequest object.", e);
			}
		}
	}

	public void insertBatteryConsumption(BatteryConsumption consmp) {
		if (isConnectedAndSchemaSetUp.get()) {
			IndexRequest ir = new IndexRequest(ElasticSearchSchema.BatteryConsumption.INDEX_NAME, ElasticSearchSchema.BatteryConsumption.INDEX_NAME);
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();

				builder.field(ElasticSearchSchema.BatteryConsumption.CONSUMPTION_INTERVAL, consmp.getTimeInterval());
				builder.field(ElasticSearchSchema.BatteryConsumption.CONSUMPTION_VALUE, consmp.getConsumptionPercent());
				builder.field(ElasticSearchSchema.BatteryConsumption.TIMESTAMP, consmp.getTimeStamp().getTime());
				builder.field(ElasticSearchSchema.BatteryConsumption.SESSION_ID, consmp.getSessionId());

				enrichWithSessionData(consmp.getSessionTags(), builder);

				builder.endObject();

				ir.source(builder);

				elasticSearch.executeIndexRequest(ir, indexResponseListener);
			} catch (IOException e) {
				LOG.warn("Could not index a HttpNetworkRequest object.", e);
			}
		}
	}

	public void insertHttpRequest(HttpNetworkRequest req) {
		if (isConnectedAndSchemaSetUp.get()) {
			IndexRequest ir = new IndexRequest(ElasticSearchSchema.HttpNetworkRequest.INDEX_NAME, ElasticSearchSchema.HttpNetworkRequest.INDEX_NAME);
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();

				builder.field(ElasticSearchSchema.HttpNetworkRequest.CONTENT_TYPE, req.getContentType());
				builder.field(ElasticSearchSchema.HttpNetworkRequest.DURATION, req.getDuration());
				builder.field(ElasticSearchSchema.HttpNetworkRequest.METHOD, req.getMethod());
				builder.field(ElasticSearchSchema.HttpNetworkRequest.RESPONSE_CODE, req.getResponseCode());
				builder.field(ElasticSearchSchema.HttpNetworkRequest.URL, req.getUrl());
				builder.field(ElasticSearchSchema.HttpNetworkRequest.TIMESTAMP, req.getTimeStamp().getTime());
				builder.field(ElasticSearchSchema.HttpNetworkRequest.SESSION_ID, req.getSessionId());

				enrichWithSessionData(req.getSessionTags(), builder);

				builder.endObject();

				ir.source(builder);

				elasticSearch.executeIndexRequest(ir, indexResponseListener);
			} catch (IOException e) {
				LOG.warn("Could not index a HttpNetworkRequest object.", e);
			}
		}
	}

	public void insertResourceUsage(SystemResourceUsage usg) {
		if (isConnectedAndSchemaSetUp.get()) {
			IndexRequest ir = new IndexRequest(ElasticSearchSchema.ResourceUsage.INDEX_NAME, ElasticSearchSchema.ResourceUsage.INDEX_NAME);
			try {
				XContentBuilder builder = XContentFactory.jsonBuilder();
				builder.startObject();

				builder.field(ElasticSearchSchema.ResourceUsage.CPU_USAGE, usg.getCpuUsage());
				builder.field(ElasticSearchSchema.ResourceUsage.MEMROY_USAGE, usg.getMemoryUsage());
				builder.field(ElasticSearchSchema.ResourceUsage.TIMESTAMP, usg.getTimeStamp().getTime());
				builder.field(ElasticSearchSchema.ResourceUsage.SESSION_ID, usg.getSessionId());

				enrichWithSessionData(usg.getSessionTags(), builder);

				builder.endObject();

				ir.source(builder);

				elasticSearch.executeIndexRequest(ir, indexResponseListener);
			} catch (IOException e) {
				LOG.warn("Could not index a HttpNetworkRequest object.", e);
			}
		}
	}

	public boolean isConnected() {
		return elasticSearch.isConnected();
	}

	@PostConstruct
	protected void init() {
		synchronized (elasticSearch) {
			elasticSearch.addConnectionStateListener(connectionListener);
			if (isConnected()) {
				// init schema
				initIndex();
				isConnectedAndSchemaSetUp.set(true);
			}
		}
	}

	private void initIndex() {
		// TODO remove the deletes in production
		elasticSearch.deleteIndex(ElasticSearchSchema.HttpNetworkRequest.INDEX_NAME);
		elasticSearch.deleteIndex(ElasticSearchSchema.ResourceUsage.INDEX_NAME);
		elasticSearch.deleteIndex(ElasticSearchSchema.BatteryConsumption.INDEX_NAME);
		elasticSearch.deleteIndex(ElasticSearchSchema.ActivityStart.INDEX_NAME);
		elasticSearch.deleteIndex(ElasticSearchSchema.UncaughtException.INDEX_NAME);

		elasticSearch.createMapping(ElasticSearchSchema.HttpNetworkRequest.INDEX_NAME, GEOPOINT_FIELD, "geo_point", ElasticSearchSchema.MobileIndex.TIMESTAMP, "date");
		elasticSearch.createMapping(ElasticSearchSchema.ResourceUsage.INDEX_NAME, GEOPOINT_FIELD, "geo_point", ElasticSearchSchema.MobileIndex.TIMESTAMP, "date");
		elasticSearch.createMapping(ElasticSearchSchema.BatteryConsumption.INDEX_NAME, GEOPOINT_FIELD, "geo_point", ElasticSearchSchema.MobileIndex.TIMESTAMP, "date");
		elasticSearch.createMapping(ElasticSearchSchema.ActivityStart.INDEX_NAME, GEOPOINT_FIELD, "geo_point", ElasticSearchSchema.MobileIndex.TIMESTAMP, "date");
		elasticSearch.createMapping(ElasticSearchSchema.UncaughtException.INDEX_NAME, GEOPOINT_FIELD, "geo_point", ElasticSearchSchema.MobileIndex.TIMESTAMP, "date");
	}

	private void enrichWithSessionData(List<Pair<String, String>> tags, XContentBuilder insert) throws IOException {
		boolean findLon = false, findLat = false;
		double lon = 0, lat = 0;
		if (tags != null) {
			for (Pair<String, String> tag : tags) {
				String key = tag.getFirst();
				if (key.equals(IAdditionalTagSchema.ANDROID_VERSION)) {
					insert.field(ElasticSearchSchema.MobileIndex.ANDROID_VERSION, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.APP_VERSION)) {
					insert.field(ElasticSearchSchema.MobileIndex.APP_VERSION, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.APP_NAME)) {
					insert.field(ElasticSearchSchema.MobileIndex.APP_NAME, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.DEVICE_NAME)) {
					insert.field(ElasticSearchSchema.MobileIndex.DEVICE_NAME, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.DEVICE_LANG)) {
					insert.field(ElasticSearchSchema.MobileIndex.DEVICE_LANG, tag.getSecond());
				} else if (key.equals(IAdditionalTagSchema.DEVICE_LON)) {
					lon = Double.parseDouble(tag.getSecond());
					findLon = true;
				} else if (key.equals(IAdditionalTagSchema.DEVICE_LAT)) {
					lat = Double.parseDouble(tag.getSecond());
					findLat = true;
				}
			}
		}

		if (findLat && findLon) {
			// geohash
			insert.field(GEOPOINT_FIELD, new GeoPoint(lat, lon));
		}
	}

}
