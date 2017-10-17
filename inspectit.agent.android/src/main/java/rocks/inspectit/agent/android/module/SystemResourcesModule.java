package rocks.inspectit.agent.android.module;

import java.io.IOException;
import java.io.RandomAccessFile;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug.MemoryInfo;
import rocks.inspectit.agent.android.module.util.ExecutionProperty;
import rocks.inspectit.shared.all.communication.data.mobile.SystemResourceUsage;

/**
 * @author David Monschein
 *
 */
public class SystemResourcesModule extends AbstractMonitoringModule {

	/**
	 * Activity manager which is needed for memory and cpu statistics.
	 */
	private ActivityManager manager;

	/**
	 * The PID of the monitored application.
	 */
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

	/**
	 * Collects the CPU and memory usage and sends them back to the CMR.
	 */
	@ExecutionProperty(interval = 30000L)
	public void collectMeasurements() {
		float cpu = collectCpuUsage();
		float memory = collectMemoryUsage();

		SystemResourceUsage resp = new SystemResourceUsage();
		resp.setCpuUsage(cpu);
		resp.setMemoryUsage(memory);

		this.pushData(resp);
	}

	/**
	 * Reads the memory usage of the monitored application.
	 *
	 * @return memory usage in kB and -1 if we can't resolve it
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
	 * Gets the cpu usage of the current application.
	 *
	 * @return cpu usage in percent (0 - 100)
	 */
	private float collectCpuUsage() {
		return readCpuUsage(pid);
	}

	/**
	 * Calculation of the cpu usage of a specified PID.
	 *
	 * @param pid
	 *            the process id
	 * @return the cpu usage in percent (0 - 100)
	 */
	private float readCpuUsage(int pid) {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/" + pid + "/stat", "r");
			RandomAccessFile reader2 = new RandomAccessFile("/proc/uptime", "r");

			String load = reader.readLine();
			String uptime = reader2.readLine();

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
