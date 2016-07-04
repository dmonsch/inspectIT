package rocks.inspectit.server.processor.impl;

import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;

import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import rocks.inspectit.server.processor.AbstractCmrDataProcessor;
import rocks.inspectit.server.tsdb.InfluxDBService;
import rocks.inspectit.shared.all.communication.DefaultData;
import rocks.inspectit.shared.all.communication.data.EUMData;
import rocks.inspectit.shared.all.communication.data.eum.AjaxRequest;
import rocks.inspectit.shared.all.communication.data.eum.PageLoadRequest;
import rocks.inspectit.shared.all.communication.data.eum.ResourceLoadRequest;
import rocks.inspectit.shared.all.spring.logger.Log;

/**
 * @author David Monschein
 *
 */
public class EUMDataCmrProcessor extends AbstractCmrDataProcessor {

	/**
	 * Logger for the class.
	 */
	@Log
	Logger log;

	/**
	 * Instance of the {@link InfluxDBService}.
	 */
	@Autowired
	InfluxDBService influxDb;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processData(DefaultData defaultData, EntityManager entityManager) {
		EUMData data = (EUMData) defaultData;
		// write it into influxdb
		for (PageLoadRequest plReq : data.getPageLoadRequests()) {
			Point point = Point.measurement("request_pageload").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
					.tag("browser", data.getUserSession().getBrowser())
					.tag("device", data.getUserSession().getDevice())
					.tag("language", data.getUserSession().getLanguage())
					.tag("url", plReq.getUrl())
					.tag("sessId", data.getUserSession().getSessionId())
					.addField("browser", data.getUserSession().getBrowser())
					.addField("os", data.getUserSession().getDevice())
					.addField("language", data.getUserSession().getLanguage())
					.addField("url", plReq.getUrl())
					.addField("navigationStart", plReq.getNavigationStartW())
					.addField("connectEnd", plReq.getConnectEndW())
					.addField("connectStart", plReq.getConnectStartW())
					.addField("domContentLoadedEventStart", plReq.getDomContentLoadedEventStartW())
					.addField("domContentLoadedEventEnd", plReq.getDomContentLoadedEventEndW())
					.addField("domInteractive", plReq.getDomInteractiveW())
					.addField("domLoading", plReq.getDomLoadingW())
					.addField("domainLookupStart", plReq.getDomainLookupStartW()).addField("domainLookupEnd", plReq.getDomainLookupEndW()).addField("fetchStart", plReq.getFetchStartW())
					.addField("loadEventStart", plReq.getLoadEventStartW()).addField("loadEventEnd", plReq.getLoadEventEndW()).addField("redirectStart", plReq.getRedirectStartW())
					.addField("redirectEnd", plReq.getRedirectEndW()).addField("requestStart", plReq.getRequestStartW()).addField("responseStart", plReq.getResponseStartW())
					.addField("requestEnd", plReq.getResponseEndW()).addField("unloadEventStart", plReq.getUnloadEventStartW()).addField("unloadEventEnd", plReq.getUnloadEventEndW())
					.addField("speedindex", plReq.getSpeedindex())
					.addField("firstpaint", plReq.getFirstpaint())
					.build();

			influxDb.insert(point);
		}

		for (ResourceLoadRequest rlReq : data.getResourceLoadRequests()) {
			Point point = Point.measurement("request_resourceload").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
					.tag("browser", data.getUserSession().getBrowser())
					.tag("device", data.getUserSession().getDevice())
					.tag("language", data.getUserSession().getLanguage())
					.tag("url", rlReq.getUrl())
					.tag("sessId", data.getUserSession().getSessionId())
					.addField("browser", data.getUserSession().getBrowser())
					.addField("os", data.getUserSession().getDevice())
					.addField("language", data.getUserSession().getLanguage())
					.addField("url", rlReq.getUrl())
					.addField("initiatorType", rlReq.getInitiatorType())
					.addField("endTime", rlReq.getEndTime())
					.addField("name", rlReq.getUrl())
					.addField("startTime", rlReq.getStartTime()).build();

			influxDb.insert(point);
		}

		for (AjaxRequest ajaxReq : data.getAjaxRequests()) {
			Point point = Point.measurement("request_ajax").time(data.getTimeStamp().getTime(), TimeUnit.MILLISECONDS).tag("platform_ident", String.valueOf(data.getPlatformIdent()))
					.tag("browser", data.getUserSession().getBrowser())
					.tag("device", data.getUserSession().getDevice())
					.tag("language", data.getUserSession().getLanguage())
					.tag("url", ajaxReq.getUrl())
					.tag("sessId", data.getUserSession().getSessionId())
					.addField("baseurl", ajaxReq.getBaseUrl())
					.addField("browser", data.getUserSession().getBrowser())
					.addField("os", data.getUserSession().getDevice())
					.addField("language", data.getUserSession().getLanguage())
					.addField("url", ajaxReq.getUrl())
					.addField("status", ajaxReq.getStatus())
					.addField("method", ajaxReq.getMethod()).addField("endTime", ajaxReq.getEndTime()).addField("startTime", ajaxReq.getStartTime()).build();

			influxDb.insert(point);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canBeProcessed(DefaultData defaultData) {
		return defaultData instanceof EUMData;
	}

}
