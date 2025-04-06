package fun.moystudio.openlink.frpc;

import org.jetbrains.annotations.Nullable;

/**
 * Frpc interface.
 * @author Terry_MC
 */
public interface Frpc {
    /**
     * Get the url of the frpc file if there is an update.
     * @return Returns the full url(with http:// or https://) of the new frpc file. If there is not an update, return {@code null}.
     */
    String getUpdateFileUrl();
    /**
     * Check for the frpc update.
     * @return Returns {@code true} if there is a frpc update.
     */
    boolean checkUpdate();
    /**
     * Create the frpc process.
     * @return Returns the frpc process.
     */
    Process createFrpcProcess();

    /**
     * Create the remote proxy(tunnel).
     * @param localPort the lan server port.
     * @param remotePort the remote port user decided to use(maybe {@code null} or blank).
     * @implNote you can ignore {@code remotePort} when you cannot use that port to create the remote proxy.
     */
    void createProxy(int localPort, @Nullable String remotePort);
}
