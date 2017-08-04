package com.timeyang.jkes.services.search.exception;

import com.timeyang.jkes.services.search.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.ResponseException;
import org.json.JSONObject;

/**
 * @author chaokunyang
 */
public class SearchResponseException extends SearchException {

    private ResponseException responseException;

    public SearchResponseException(String message, ResponseException responseException) {
        super(message, responseException);
        this.responseException = responseException;
    }

    public SearchResponseException(ResponseException responseException) {
        super(responseException);
        this.responseException = responseException;
    }

    public ResponseException getResponseException() {
        return responseException;
    }

    /**
     * Get error message.
     * <p>Strip info elasticsearch sensitive, generate well-formed message</p>
     * @return error message
     */
    public Object getErrorMessage() {
        if(responseException == null) return "";

        String message = responseException.getMessage();
        if(StringUtils.isBlank(message)) return "";

        String[] lines = message.split("\n");
        if(lines.length == 2)
            return JsonUtils.parseJsonToObject(lines[1], JSONObject.class);
        else
            return message;
    }
}
