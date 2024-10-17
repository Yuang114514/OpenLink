package fun.moystudio.openlink.json;

public class JsonNode {
    public boolean allowEc,enableDefaultTls,needRealname,fullyLoaded;
    public String comments,group,hostname,name,port,description,allowPort;
    public long classify,id,status;
    public double bandwidthMagnification,bandwidth,maxOnlineMagnification,unitcostEc;
    public static class protocol{
        public boolean tcp,udp,http,https,stcp,xtcp;
    }
    public protocol protocolSupport;
}
