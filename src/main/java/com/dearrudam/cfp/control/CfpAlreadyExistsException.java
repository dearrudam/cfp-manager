package com.dearrudam.cfp.control;

public class CfpAlreadyExistsException extends CfpException {

    public CfpAlreadyExistsException() {
        super(409, "CFP already exists for this event");
    }
}
