package playbook.encore.back.common.response;

public class Response {

    private String code;
    private String msg;
    private Object data;

    public void setResponse(ResponseCode responseCode) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMessage();
    }

    public void setResponse(ResponseCode responseCode, String param) {
        this.code = responseCode.getCode();
        this.msg = responseCode.getMessage(param);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
