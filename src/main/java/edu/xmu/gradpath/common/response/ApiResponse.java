package edu.xmu.gradpath.common.response;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 0;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static ApiResponse<Void> error(int code, String message) {
        ApiResponse<Void> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        return r;
    }
}
