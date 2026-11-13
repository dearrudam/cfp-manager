package com.dearrudam.draft.control;

public class DraftNotFoundException extends DraftException {

    public DraftNotFoundException() {
        super(404, "Draft not found");
    }
}
