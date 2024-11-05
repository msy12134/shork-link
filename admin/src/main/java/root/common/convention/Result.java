package root.common.convention;
import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private String code;
    private String message;
    private T data;
    private String requestId;
    private String success;

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data=object;
        result.code="1";
        result.success="true";
        return result;
    }

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code="1";
        result.success="true";
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<T>();
        result.code="0";
        result.message=message;
        result.success="false";
        return result;
    }

    public static <T> Result<T> fail() {
        Result<T> result = new Result<T>();
        result.code="0";
        result.success="false";
        return result;
    }
}


