package playbook.encore.back.common.response;

public class ResponseHandler {

    public static Response success() {
        Response response = new Response();
        response.setResponse(ResponseCode.SUCCESS);
        return response;
    }

    public static Response success(Object data) {
        Response response = new Response();
        response.setResponse(ResponseCode.SUCCESS);
        response.setData(data);
        return response;
    }

    public static Response success(Object data, String msg) {
        Response response = new Response();
        response.setResponse(ResponseCode.SUCCESS);
        response.setData(data);
        response.setMsg(msg);
        return response;
    }

    public static Response error(ResponseCode responseCode) {
        Response response = new Response();
        response.setResponse(responseCode);
        return response;
    }

    public static Response error(ResponseCode responseCode, String message) {
        Response response = new Response();
        response.setResponse(responseCode);
        response.setMsg(message);
        return response;
    }

    public static Response noData() {
        Response response = new Response();
        response.setResponse(ResponseCode.NO_DATA);
        return response;
    }

    public static Response existInfo() {
        Response response = new Response();
        response.setResponse(ResponseCode.EXIST_INFO);
        return response;
    }

    public static Response invalidParam(String param) {
        Response response = new Response();
        response.setResponse(ResponseCode.INVALID_PARAM, param);
        return response;
    }

    public static Response invalidParamLen(String param) {
        Response response = new Response();
        response.setResponse(ResponseCode.INVALID_PARAM_LEN, param);
        return response;
    }

    public static Response invalidParamPattern(String param) {
        Response response = new Response();
        response.setResponse(ResponseCode.INVALID_PARAM_PATTERN, param);
        return response;
    }

    public static Response invalidParamType(String param) {
        Response response = new Response();
        response.setResponse(ResponseCode.INVALID_PARAM_TYPE, param);
        return response;
    }

    public static Response notAuthenticated() {
        Response response = new Response();
        response.setResponse(ResponseCode.NOT_AUTHENTICATED);
        return response;
    }

    public static Response notAuthorized() {
        Response response = new Response();
        response.setResponse(ResponseCode.NOT_AUTHORIZED);
        return response;
    }

    public static Response noSession(String key) {
        Response response = new Response();
        response.setResponse(ResponseCode.NO_SESSION, key);
        return response;
    }

    public static Response failToUpdate() {
        Response response = new Response();
        response.setResponse(ResponseCode.FAIL_UPDATE);
        return response;
    }

    public static Response failToDelete() {
        Response response = new Response();
        response.setResponse(ResponseCode.FAIL_DELETE);
        return response;
    }

    public static Response failToInsert() {
        Response response = new Response();
        response.setResponse(ResponseCode.FAIL_INSERT);
        return response;
    }

    public static Response failToUpsert() {
        Response response = new Response();
        response.setResponse(ResponseCode.FAIL_UPSERT);
        return response;
    }

    public static Response failToProcess() {
        Response response = new Response();
        response.setResponse(ResponseCode.FAIL_PROCESS);
        return response;
    }

    public static Response unknownError() {
        Response response = new Response();
        response.setResponse(ResponseCode.UNKNOWN);
        return response;
    }

    public static Response notImplemented() {
        Response response = new Response();
        response.setResponse(ResponseCode.NOT_IMPLEMENTED);
        return response;
    }

    public static Response timeOut() {
        Response response = new Response();
        response.setResponse(ResponseCode.TIMEOUT);
        return response;
    }
}
