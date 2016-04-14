package test.parser.parameter;

/**
 * Test bean class.
 * @author Alessandro
 * @version 1.0
 *
 */
public final class TestBeanClass {

    /**a path.*/
    private String path;

    /**is forced.*/
    private boolean forced;

    /**
     *
     * @return true if forced
     */
    public boolean isForced() {
        return this.forced;
    }

    /**
     *
     * @return path
     */
    public String getPath() {
        return this.path;
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
}
