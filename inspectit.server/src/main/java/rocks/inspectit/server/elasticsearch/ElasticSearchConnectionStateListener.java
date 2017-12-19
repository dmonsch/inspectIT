package rocks.inspectit.server.elasticsearch;

/**
 * @author David Monschein
 *
 */
public interface ElasticSearchConnectionStateListener {

	void disconnected(ElasticSearchDao dao);

	void connected(ElasticSearchDao dao);

}
