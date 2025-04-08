package fun.moystudio.openlink.frpc;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Frpc interface.
 * @author Terry_MC
 */
public interface Frpc {
    /**
     * Return {@code false} by default.<br>
     * We recommend you not to override the download logic. OpenLink will automatically download frpc file and extract(if you return {@code true} in the method {@link #isArchive()}).<br>
     * If you really want to override the download logic, return {@code true} and implement {@link #downloadFrpcLogicOverride(Path)}.
     * @return whether you override the download logic.
     */
    default boolean isDownloadLogicOverridden() {
        return false;
    };
    /**
     * Do nothing by default.<br>
     * We recommend you not to override the download logic. OpenLink will automatically download frpc file and extract(if you return {@code true} in the method {@link #isArchive()}).<br>
     * If you really want to override the download logic, implement this method.
     * @param frpcDownloadDir the download directory of the frpc executable file.
     * @implNote you have to download frpc to {@code frpcDownloadDir} and .
     */
    default void downloadFrpcLogicOverride(Path frpcDownloadDir) {
    }
    /**
     * Return {@code false} by default.
     * @return whether the frpc file is an archive
     */
    default boolean isArchive() {
        return false;
    };
    /**
     * Return {@code null} by default.<br>
     * Get the url of the frpc file if there is an update.
     * @return the full url(with http:// or https://) of the new frpc file. If there is not an update, return {@code null}.
     */
    default String getUpdateFileUrl() {
        return null;
    };
    /**
     * Check for the frpc update.
     * @return whether there is a frpc update.
     */
    boolean checkUpdate();
    /**
     * Create the frpc process.
     * @return the frpc process.
     */
    Process createFrpcProcess(Path frpcExecutableFilePath);

    /**
     * Create the remote proxy(tunnel).
     * @param localPort the lan server port.
     * @param remotePort the remote port user decided to use(maybe {@code null} or blank).
     * @implNote you can ignore {@code remotePort} when you cannot use that port to create the remote proxy.
     */
    void createProxy(int localPort, @Nullable String remotePort);
}
