package fun.moystudio.openlink.frpc;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * Frpc interface.
 * @author Terry_MC
 */
public interface Frpc {
    /**
     * Return {@code false} by default.<br>
     * We recommend you not to override the download logic. OpenLink will automatically download frpc file and extract(if you return {@code true} in the method {@link #isArchive()}).<br>
     * If you really want to override the download logic, implement this method and return {@code true}.
     * @param frpcDownloadDir the download directory of the frpc executable file.
     * @implNote You have to download frpc to {@code frpcDownloadDir} and make sure there is a frpc executable file if you do not implement {@link #frpcDirPathOverride(Path)}(OpenLink will scan the directory to find an executable file).
     */
    default boolean downloadFrpcLogicOverride(Path frpcDownloadDir) {
        return false;
    }
    /**
     * Return {@code null} by default.<br>
     * You can override this method to use another frpc executable file.
     * @param frpcStorageDirPath the old path of the frpc storage directory.
     * @return the new path of the frpc storage directory.
     */
    default Path frpcDirPathOverride(Path frpcStorageDirPath) {
        return null;
    }
    /**
     * Return {@code false} by default.
     * @return whether the frpc file is an archive.
     */
    default boolean isArchive() {
        return false;
    }
    /**
     * Return {@code null} by default.<br>
     * Get the url list of the frpc file if there is an update. The order in the list determines the url's priority.
     * @return the full url(with http:// or https://) list of the new frpc file. The higher the position of the url in the list, the higher the priority. If there is not an update, return {@code null}.
     */
    default List<String> getUpdateFileUrls() {
        return null;
    }
    /**
     * @return whether there is a frpc update.
     */
    boolean isOutdated();
    /**
     * Create the frpc process.
     * @param frpcExecutableFilePath the path of the frpc executable file.
     * @param localPort the lan server port.
     * @param remotePort the remote port user decided to use(maybe {@code null} or blank).
     * @return the frpc process.
     */
    Process createFrpcProcess(Path frpcExecutableFilePath, int localPort, @Nullable String remotePort);
    /**
     * Create the remote proxy(tunnel).
     * @param localPort the lan server port.
     * @param remotePort the remote port user decided to use(maybe {@code null} or blank).
     * @implNote You can ignore {@code remotePort} when you cannot use that port to create the remote proxy.
     */
    void createProxy(int localPort, @Nullable String remotePort);
    /**
     * Get the frpc version.
     * @param frpcExecutableFilePath the path of the frpc executable file.
     * @return the version string of the frpc.
     */
    String getFrpcVersion(Path frpcExecutableFilePath);
    /**
     * Stop the frpc process.
     * @param frpcProcess the process of the frpc executable file.
     */
    default void stopFrpcProcess(@Nullable Process frpcProcess) {
        if(frpcProcess!=null){
            frpcProcess.destroy();
        }
    }
    /**
     * YOU HAVE TO IMPLEMENT THIS METHOD!
     * Get the instance of your Frpc implementation.
     * @return the instance of your Frpc implementation
     * @implNote there has to be only one instance of your Frpc implementation.
     */
    static Frpc getInstance() {
        return null;
    }

}
