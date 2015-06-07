package arkhados.settings.server;

/**
 *
 * @author Teemu
 */
public class SettingsGeneral {

    private int port;
    private float physicsTicksPerSecond;
    private float defaultSyncFrequency;

    public int getPort() {
        return port;
    }

    public float getPhysicsTicksPerSecond() {
        return physicsTicksPerSecond;
    }

    public float getDefaultSyncFrequency() {
        return defaultSyncFrequency;
    }
}
