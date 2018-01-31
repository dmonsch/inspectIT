package rocks.inspectit.server.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import rocks.inspectit.server.externalservice.IExternalService;
import rocks.inspectit.shared.all.cmr.property.spring.PropertyUpdate;
import rocks.inspectit.shared.all.externalservice.ExternalServiceStatus;
import rocks.inspectit.shared.all.externalservice.ExternalServiceType;

/**
 * @author David Monschein
 *
 */
@Component
public class ElasticSearchDao implements IExternalService {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchDao.class);

	private static final int PING_FREQUENCY = 5000;

	@Value("${elasticsearch.active}")
	boolean active;

	@Value("${elasticsearch.keyspace}")
	String keyspaceName;

	@Value("${elasticsearch.hosts}")
	String hosts;

	@Value("${elasticsearch.port}")
	int port;

	@Value("${elasticsearch.user}")
	String user;

	@Value("${elasticsearch.passwd}")
	String password;

	@Value("${elasticsearch.ssl}")
	boolean useSSL;

	@Autowired
	@Resource(name = "scheduledExecutorService")
	private ScheduledExecutorService scheduledExecutorService;

	private volatile boolean isConnected = false;

	private List<ElasticSearchConnectionStateListener> listeners;

	private Future<?> connectionTask;

	private RestHighLevelClient restClient;
	private RestClient.FailureListener failureListener;
	private CredentialsProvider credentialsProvider;

	private BulkProcessor bulkProcessor;

	@PostConstruct
	void init() {
		this.listeners = new ArrayList<>();
		this.failureListener = new RestClient.FailureListener() {
			@Override
			public void onFailure(HttpHost host) {
				disconnect();
			}
		};
		this.credentialsProvider = new BasicCredentialsProvider();
	}

	/**
	 * Connects to the InfluxDB if the feature has been enabled.
	 */
	@PostConstruct
	@PropertyUpdate(properties = { "elasticsearch.hosts", "elasticsearch.port", "elasticsearch.user", "elasticsearch.passwd", "elasticsearch.keyspace", "elasticsearch.active", "elasticsearch.ssl" })
	public void propertiesUpdated() {
		if (connectionTask != null) {
			connectionTask.cancel(false);
			try {
				connectionTask.get();
			} catch (Exception e) {
				LOG.trace("connectionTask terminated", e);
			}
		}
		disconnect();
		if (active) {
			connectionTask = scheduledExecutorService.submit(this::connect);
		}
	}

	public void executeIndexRequest(IndexRequest req, ActionListener<IndexResponse> resp) {
		bulkProcessor.add(req);
	}

	public void deleteIndex(String indexName) {
		try {
			restClient.indices().deleteIndex(new DeleteIndexRequest(indexName));
		} catch (IOException e) {
			LOG.warn("Could not delete the existing index with name '" + indexName + "'.");
		} catch (ElasticsearchStatusException e) {
			LOG.warn("Could not delete the existing index with name '" + indexName + "'.");
		}
	}

	public void createMapping(String index, String... keyVals) {
		if ((keyVals.length % 2) != 0) {
			return;
		}
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder()
					.startObject()
					.startObject("mappings")
					.startObject(index)
					.startObject("properties");

			for (int i = 0; i < keyVals.length; i += 2) {
				builder.startObject(keyVals[i]).field("type", keyVals[i + 1]);
				builder.endObject();
			}

			String payload = builder.endObject()
					.endObject()
					.endObject()
					.endObject().string();

			HttpEntity entity = new NStringEntity(payload, ContentType.APPLICATION_JSON);
			restClient.getLowLevelClient().performRequest("PUT", index, Collections.emptyMap(), entity);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private synchronized void connect() {
		try {
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
			restClient = new RestHighLevelClient(RestClient.builder(new HttpHost(hosts, port, useSSL ? "https" : "http"), new HttpHost(hosts, port, useSSL ? "https" : "http"))
					.setFailureListener(failureListener).setHttpClientConfigCallback(new HttpClientConfigCallback() {
						@Override
						public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
							return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
						}
					}));
			bulkProcessor = BulkProcessor.builder(
					restClient::bulkAsync,
					new BulkProcessor.Listener() {
						@Override
						public void beforeBulk(long executionId,
								BulkRequest request) {
						}

						@Override
						public void afterBulk(long executionId,
								BulkRequest request,
								BulkResponse response) {
						}

						@Override
						public void afterBulk(long executionId,
								BulkRequest request,
								Throwable failure) {
						}
					})
					.setBulkActions(2000)
					.setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
					.setFlushInterval(TimeValue.timeValueSeconds(4))
					.setConcurrentRequests(1)
					.setBackoffPolicy(
							BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
					.build();
			MainResponse resp = restClient.info();
			isConnected = true;
			LOG.info("Connected to ElasticSearch! (Version: " + resp.getVersion() + ", Cluster ID:" + resp.getClusterUuid() + ")");
			listeners.forEach((l) -> l.connected(this));
		} catch (Exception e) {
			LOG.error("Error connecting to ElasticSearch!", e);
			isConnected = false;
			if (restClient != null) {
				try {
					restClient.close();
				} catch (IOException e1) {
					LOG.warn("Could not release all resources properly.", e1);
				}
			}
		}
	}

	synchronized void disconnect() {
		if (isConnected) {
			isConnected = false;
			try {
				// terminate running queries
			} finally {
				try {
					bulkProcessor.awaitClose(5000, TimeUnit.MILLISECONDS);
					restClient.close();
				} catch (IOException e) {
					LOG.warn("Could not release all resources properly.", e);
				} catch (InterruptedException e) {
					LOG.warn("Could not wait for close of bulk processor.", e);
				}
			}
			listeners.forEach((l) -> l.disconnected(this));
		}

	}

	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExternalServiceStatus getServiceStatus() {
		if (active) {
			try {
				if (isConnected && restClient.ping()) {
					return ExternalServiceStatus.CONNECTED;
				} else {
					return ExternalServiceStatus.DISCONNECTED;
				}
			} catch (IOException e) {
				return ExternalServiceStatus.DISCONNECTED;
			}
		} else {
			return ExternalServiceStatus.DISABLED;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExternalServiceType getServiceType() {
		return ExternalServiceType.ELASTICSEARCH;
	}

	/**
	 * @param connectionListener
	 */
	public void addConnectionStateListener(ElasticSearchConnectionStateListener connectionListener) {
		this.listeners.add(connectionListener);
	}

	protected void setScheduledExecutorService(ScheduledExecutorService serv) {
		this.scheduledExecutorService = serv;
	}

}
