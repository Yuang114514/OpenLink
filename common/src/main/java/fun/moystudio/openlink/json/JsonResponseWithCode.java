package fun.moystudio.openlink.json;

public class JsonResponseWithCode<T> extends JsonBaseResponse {
    public T data;
    public int code;
}
