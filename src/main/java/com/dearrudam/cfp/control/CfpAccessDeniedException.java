package com.dearrudam.cfp.control;

public class CfpAccessDeniedException extends CfpException {

    public CfpAccessDeniedException() {
        super(403, "Only the event owner can manage this CFP");
    }
}
