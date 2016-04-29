package nd.esp.service.lifecycle.utils.openApi;
/**
 * OpenApi 协议对象
 * @author licong
 * @date 14-9-26
 */
public class OpenApiProtocol<T> {

    private int code;           //错误编码
    private String message;     //错误信息
    private T data;             //数据对象

    public OpenApiProtocol(){}

    public OpenApiProtocol(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}