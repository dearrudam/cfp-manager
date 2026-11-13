package com.dearrudam.cfp.control;

public class CommitteeMemberNotFoundException extends CfpException {

    public CommitteeMemberNotFoundException() {
        super(404, "Committee member not found");
    }
}
