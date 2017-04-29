package rocks.inspectit.agent.android.test;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

/**
 * @author David Monschein
 *
 */
public class FillWithRandomData {

	public static void main(String[] argv) {
		InfluxDB influxDB = InfluxDBFactory.connect("http://192.168.56.101:8086", "root", "root");

		influxDB.close();
	}

}
