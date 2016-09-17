package nd.esp.service.lifecycle.utils.openApi;

/**
 * OpenApi 错误返回码
 */
public enum OpenApiCode {

    SUCCESS(1),         // 成功
    PARAM_ERROR(40000), // 参数错误
    EXCEPTION(50000);   // 异常

    OpenApiCode(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }
}
