package com.timeyang.jkes.core.http;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author chaokunyang
 */
@Builder
@ToString
@Getter
@Setter
public class Response {

    private int statusCode;

    private String content;
}
