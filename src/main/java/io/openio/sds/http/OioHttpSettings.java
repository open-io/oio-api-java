package io.openio.sds.http;

/**
 * 
 * @author Christopher Dedeurwaerder
 * @author Florent Vennetier
 *
 */
public class OioHttpSettings {

	private Integer sendBufferSize = 131072;
	private Integer receiveBufferSize = 131072;
	private Boolean setSocketBufferSize = false;
	private Integer connectTimeout = 30000;
	private Integer readTimeout = 60000;
	private String userAgent = "oio-http";

	public OioHttpSettings() {
	}

	public Integer sendBufferSize() {
		return sendBufferSize;
	}

	public OioHttpSettings sendBufferSize(Integer sendBufferSize) {
		this.sendBufferSize = sendBufferSize;
		return this;
	}

	public Integer receiveBufferSize() {
		return receiveBufferSize;
	}

	public OioHttpSettings receiveBufferSize(Integer receiveBufferSize) {
		this.receiveBufferSize = receiveBufferSize;
		return this;
	}

    /**
     * Should the size of the socket buffers be explicitly set?
     * When true, explicitly set the send buffer size (resp. receive buffer
     * size) to {@link #sendBufferSize} (resp. {@link #receiveBufferSize}).
     * When false, let the kernel adjust the size automatically.
     *
     * @return true when the API should set the socket buffer sizes,
     *  false when it should let the kernel decide.
     */
    public Boolean setSocketBufferSize() {
        return this.setSocketBufferSize;
    }

    /**
     * Should the size of the socket buffers be explicitly set?
     * When true, explicitly set the send buffer size (resp. receive buffer
     * size) to {@link #sendBufferSize} (resp. {@link #receiveBufferSize}).
     * When false, let the kernel adjust the size automatically.
     *
     * @param setSocketBufferSize
     * @return this
     */
    public OioHttpSettings setSocketBufferSize(Boolean setSocketBufferSize) {
        this.setSocketBufferSize = setSocketBufferSize;
        return this;
    }

	public Integer connectTimeout() {
		return connectTimeout;
	}

	public OioHttpSettings connectTimeout(Integer connectTimeout) {
		this.connectTimeout = connectTimeout;
		return this;
	}

	public Integer readTimeout() {
		return readTimeout;
	}

	public OioHttpSettings readTimeout(Integer readTimeout) {
		this.readTimeout = readTimeout;
		return this;
	}

    public String userAgent() {
        return userAgent;
    }

    public OioHttpSettings userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }
}
