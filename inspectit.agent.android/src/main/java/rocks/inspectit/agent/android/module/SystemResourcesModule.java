package rocks.inspectit.agent.android.module;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug.MemoryInfo;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.shared.all.communication.data.mobile.SystemResourceUsageResponse;

/**
 * @author David Monschein
 *
 */
public class SystemResourcesModule extends AbstractMonitoringModule {

	private ActivityManager manager;
	private int pid;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initModule(Context ctx) {
		manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		pid = android.os.Process.myPid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void shutdownModule() {
	}

	@ExecutionProperty(interval = 30000L)
	public void collectMeasurements() {
		float cpu = collectCpuUsage();
		float memory = collectMemoryUsage();

		SystemResourceUsageResponse resp = new SystemResourceUsageResponse();
		resp.setCpuUsage(cpu);
		resp.setMemoryUsage(memory);

		this.pushData(resp);
	}

	/**
	 *
	 */
	private float collectMemoryUsage() {
		MemoryInfo[] info = manager.getProcessMemoryInfo(new int[] { pid });
		if (info.length == 1) {
			MemoryInfo own = info[0];
			return own.getTotalPss(); // in kB
		}
		return -1.0f;
	}

	/**
	 *
	 */
	private float collectCpuUsage() {
		return readCpuUsage(pid);
	}

	private float readCpuUsage(int pid) {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/" + pid + "/stat", "r");
			RandomAccessFile reader2 = new RandomAccessFile("/proc/uptime", "r");

			String load = reader.readLine();
			String uptime = reader2.readLine();

			// 1169 1169 0 0 -1 4219200 5546 0 0 0 62 238 0 0 20 0 12 0 378466 931201024 8592
			// 4294967295 3077640192 3077645692 3220668688 3220665936 3076902886 0 4612 0 38136
			// 4294967295 0 0 17 0 0 0 0 0 0 3077651968 3077652464 3083341824 3220675647 3220675723
			// 3220675723 3220676580 0

			String[] loadTokens = load.split(" ");
			String[] uptimeTokens = uptime.split(" ");

			double secondsUptime = Double.parseDouble(uptimeTokens[0]);

			double utime = Double.parseDouble(loadTokens[13]);
			double stime = Double.parseDouble(loadTokens[14]);
			double cutime = Double.parseDouble(loadTokens[15]);
			double cstime = Double.parseDouble(loadTokens[16]);

			double starttime = Double.parseDouble(loadTokens[21]);

			double hertz = 100.0d;

			double total_time = utime + stime;
			total_time += cutime + cstime;
			double seconds = secondsUptime - (starttime / hertz);

			double cpu_usage = 100 * ((total_time / hertz) / seconds);

			reader.close();
			reader2.close();

			return (float) cpu_usage;

		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return 0;
	}

}
