package test.parser.parameter;

import giusa.software.parser.parameter.Option;
import giusa.software.parser.parameter.Parameter;
import giusa.software.parser.parameter.ParameterBean;

/**
 * Test bean class.
 * @author Alessandro
 * @version 1.0
 *
 */
public final class TestBeanClass implements ParameterBean {

    /**a path.*/
    private String path;

    /**device.*/
    private String device;

    /**is forced.*/
    private boolean forced;

    /**time out value.*/
    private int timeout;

    /**
     *
     * @return true if forced
     */
    @Option(name = "-f")
    public boolean isForced() {
        return this.forced;
    }

    /**
     *
     * @return path
     */
    @Parameter(name = "path", required = true)
    public String getPath() {
        return this.path;
    }

    /**
     * Device name.
     * @return device name
     */
    @Parameter(name = "device", required = false)
    //TODO if a parameter is not required it can not be over position!
    public String getDevice() {
        return this.device;
    }

    /**
     * Timeout value.
     * @return value of timeout
     */
    @Parameter(name = "timeout", position = 0, required = true)
    public int getTimeout() {
        return this.timeout;
    }

    /**
     *
     * @param isForced true if it should be forced
     */
    public void setForced(final boolean isForced) {
        this.forced = isForced;
    }

    /**
     *
     * @param thePath path
     */
    public void setPath(final String thePath) {
        this.path = thePath;
    }

    /**
     * Set the device name.
     * @param theDevice device name
     */
    public void setDevice(final String theDevice) {
        this.device = theDevice;
    }

    /**
     * Set the timeout.
     * @param theTimeout timeout
     */
    public void setTimeout(final int theTimeout) {
        this.timeout = theTimeout;
    }
}
