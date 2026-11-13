package com.dearrudam.cfp.control;

public class CommitteeMemberAlreadyExistsException extends CfpException {

    public CommitteeMemberAlreadyExistsException() {
        super(409, "User is already a committee member");
    }
}
