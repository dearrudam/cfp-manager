package com.dearrudam.cfp.control;

public class CfpNotFoundException extends CfpException {

    public CfpNotFoundException() {
        super(404, "CFP not found");
    }
}
