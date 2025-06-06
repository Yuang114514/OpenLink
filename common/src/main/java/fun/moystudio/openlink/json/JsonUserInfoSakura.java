package fun.moystudio.openlink.json;

public class JsonUserInfoSakura extends JsonBaseResponseSakura{
    public String name, avatar, token, speed;
    public long id, tunnels, realname;
    public group group;
    public static class group {
        public String name;
        public long level,expires;
    }
}
