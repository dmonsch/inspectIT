package rocks.inspectit.shared.all.communication.data.mobile;

import java.sql.Timestamp;

/**
 * @author David Monschein
 *
 */
public class DefaultData {

	private Timestamp timeStamp;

	private long platformIdent;

	private long id;

	private long sensorTypeIdent;

	/**
	 * Gets {@link #timeStamp}.
	 *   
	 * @return {@link #timeStamp}  
	 */ 
	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	/**  
	 * Sets {@link #timeStamp}.  
	 *   
	 * @param timeStamp  
	 *            New value for {@link #timeStamp}  
	 */
	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets {@link #platformIdent}.
	 *   
	 * @return {@link #platformIdent}  
	 */ 
	public long getPlatformIdent() {
		return platformIdent;
	}

	/**  
	 * Sets {@link #platformIdent}.  
	 *   
	 * @param platformIdent  
	 *            New value for {@link #platformIdent}  
	 */
	public void setPlatformIdent(long platformIdent) {
		this.platformIdent = platformIdent;
	}

	/**
	 * Gets {@link #id}.
	 *   
	 * @return {@link #id}  
	 */ 
	public long getId() {
		return id;
	}

	/**  
	 * Sets {@link #id}.  
	 *   
	 * @param id  
	 *            New value for {@link #id}  
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Gets {@link #sensorTypeIdent}.
	 *   
	 * @return {@link #sensorTypeIdent}  
	 */ 
	public long getSensorTypeIdent() {
		return sensorTypeIdent;
	}

	/**  
	 * Sets {@link #sensorTypeIdent}.  
	 *   
	 * @param sensorTypeIdent  
	 *            New value for {@link #sensorTypeIdent}  
	 */
	public void setSensorTypeIdent(long sensorTypeIdent) {
		this.sensorTypeIdent = sensorTypeIdent;
	}

}
