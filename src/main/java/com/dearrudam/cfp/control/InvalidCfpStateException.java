package com.dearrudam.cfp.control;

public class InvalidCfpStateException extends CfpException {

    public InvalidCfpStateException(String message) {
        super(409, message);
    }
}
