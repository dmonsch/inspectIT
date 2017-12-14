package rocks.inspectit.agent.android.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Pair;
import rocks.inspectit.agent.android.config.AgentConfiguration;
import rocks.inspectit.agent.android.util.CacheValue;
import rocks.inspectit.shared.all.communication.data.mobile.IAdditionalTagSchema;

/**
 * This class is a proxy for accessing Android device informations, because we
 * don't want all modules to collect data on their own.
 *
 * @author David Monschein
 */
public class AndroidDataCollector {
	/**
	 * {@link TelephonyManager} for retrieving information.
	 */
	private TelephonyManager telephonyManager;

	/**
	 * {@link LocationManager} for retrieving information.
	 */
	private LocationManager locationManager;

	/**
	 * {@link ConnectivityManager} for retrieving information.
	 */
	private ConnectivityManager connectivityManager;

	/**
	 * Context of the application which is needed to create the managers above.
	 */
	private Context context;

	// CACHE VALUES
	/**
	 * Cache value for the location.
	 */
	private CacheValue<Location> locationCache = new CacheValue<Location>(15000L);

	/**
	 * Cache value for the network information.
	 */
	private CacheValue<NetworkInfo> networkInfoCache = new CacheValue<NetworkInfo>(30000L);

	/**
	 * Cache value for the network carrier.
	 */
	private CacheValue<String> networkCarrierCache = new CacheValue<String>();

	/**
	 * Cache value for the provided id of the device.
	 */
	private CacheValue<String> deviceIdCache = new CacheValue<String>();

	/**
	 * Default instance creation.
	 */
	public AndroidDataCollector() {
	}

	/**
	 * Creates a data collector and needs a given context as parameter.
	 *
	 * @param ctx
	 *            context of the application
	 */
	protected void initDataCollector(final Context ctx) {
		context = ctx;

		locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}

	/**
	 * Gets the version name of the application.
	 *
	 * @return version name of the application or the value 'unknown' if the
	 *         version isn't set
	 */
	public String getVersionName() {
		PackageInfo pInfo = null;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return "unknown";
		}
	}

	/**
	 * Gets the name of the application.
	 *
	 * @return the name of the application
	 */
	public String resolveAppName() {
		final ApplicationInfo applicationInfo = context.getApplicationInfo();
		final int stringId = applicationInfo.labelRes;
		return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
	}

	/**
	 * Gets the last known location (Needs permissions for accessing location).
	 *
	 * @return the location of the device
	 */
	public Location getLastKnownLocation() {
		return getLastKnownLocation(false);
	}

	/**
	 * Gets the last known location and provides a parameter for forcing the
	 * collector to reload the value.
	 *
	 * @param force
	 *            if true, the current location will be reloaded and the value
	 *            from the cache won'T be used
	 * @return the last known location of the user
	 */
	public Location getLastKnownLocation(final boolean force) {
		if ((locationCache != null) && locationCache.valid() && !force) {
			return locationCache.value();
		}

		return locationCache.set(getLocation());
	}

	/**
	 * Gets network information of the device.
	 *
	 * @return network information of the device
	 */
	public NetworkInfo getNetworkInfo() {
		return getNetworkInfo(false);
	}

	/**
	 * Gets network information and provides a parameter for forcing the reload
	 * of the info.
	 *
	 * @param force
	 *            true if the collector should reload the network information
	 * @return current network informations from the device
	 */
	public NetworkInfo getNetworkInfo(final boolean force) {
		if ((networkInfoCache != null) && networkInfoCache.valid() && !force) {
			return networkInfoCache.value();
		}

		return networkInfoCache.set(getNetworkInfoInner());
	}

	/**
	 * Gets the network connection type which can be either "wifi" or a mobile connection type (e.g.
	 * LTE).
	 *
	 * @return network connection type
	 */
	public String getNetworkConnectionType() {
		NetworkInfo info = this.getNetworkInfoInner();
		if (info.isConnected()) {
			if (info.getType() == ConnectivityManager.TYPE_WIFI) {
				return "wifi";
			} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				return info.getSubtypeName();
			}
		}
		return null;
	}

	/**
	 * Gets an {@link Intent} which contains information about the battery status.
	 *
	 * @return information about the battery status
	 */
	public Intent getBatteryIntent() {
		IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		return context.registerReceiver(null, iFilter);
	}

	/**
	 * Gets the name of the mobile network carrier.
	 *
	 * @return name of the mobile network carrier
	 */
	public String getNetworkCarrierName() {
		if ((networkCarrierCache != null) && networkCarrierCache.valid()) {
			return networkCarrierCache.value();
		}

		return networkCarrierCache.set(telephonyManager.getNetworkOperatorName());
	}

	/**
	 * Gets the id of the device. See {@link TelephonyManager} for detailed information.
	 *
	 * @return id of the device with model of the device as prefix
	 */
	public String getDeviceId() {
		if ((deviceIdCache != null) && deviceIdCache.valid()) {
			return deviceIdCache.value();
		}

		return deviceIdCache.set(android.os.Build.MODEL + "-" + Settings.Secure.ANDROID_ID);
	}

	/**
	 * Collects tags (additional information) of the mobile device the agent is running on.
	 *
	 * @param config
	 *            agent configuration that decides which data is collected
	 * @return list of key-value pairs that contain the additional information
	 */
	public List<Pair<String, String>> collectStaticTags(AgentConfiguration config) {
		List<Pair<String, String>> list = new ArrayList<>();

		list.add(Pair.create(IAdditionalTagSchema.APP_VERSION, this.getVersionName()));
		list.add(Pair.create(IAdditionalTagSchema.APP_NAME, this.resolveAppName()));
		list.add(Pair.create(IAdditionalTagSchema.ANDROID_VERSION, android.os.Build.VERSION.RELEASE));
		list.add(Pair.create(IAdditionalTagSchema.ANDROID_SDK, String.valueOf(android.os.Build.VERSION.SDK_INT)));
		list.add(Pair.create(IAdditionalTagSchema.DEVICE_NAME, android.os.Build.MODEL));
		list.add(Pair.create(IAdditionalTagSchema.DEVICE_LANG, Locale.getDefault().getDisplayLanguage()));

		if (config.isCollectLocation()) {
			Location loc = this.getLastKnownLocation();
			list.add(Pair.create(IAdditionalTagSchema.DEVICE_LAT, String.valueOf(loc.getLatitude())));
			list.add(Pair.create(IAdditionalTagSchema.DEVICE_LON, String.valueOf(loc.getLongitude())));
		}

		// TODO maybe connection type

		return list;
	}

	/**
	 * Accesses the current location of the device when the application has the
	 * permissions to do so.
	 *
	 * @return the location of the device and null if we don't have enough
	 *         permissions
	 */
	private Location getLocation() {
		if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
			return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		return null;
	}

	/**
	 * Gets network informations and previously checks whether we have the
	 * permission for that.
	 *
	 * @return current network information for the device and null if we don't
	 *         have enough permissions
	 */
	private NetworkInfo getNetworkInfoInner() {
		if (checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
			return connectivityManager.getActiveNetworkInfo();
		}
		return null;
	}

	/**
	 * Checks whether the application has a specific permission.
	 *
	 * @param perm
	 *            name of the permission
	 * @return true if the application has the requested permission - false
	 *         otherwise
	 */
	private boolean checkPermission(final String perm) {
		return context.checkCallingOrSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
	}
}
