package com.dearrudam.draft.control;

public class DraftAccessDeniedException extends DraftException {

    public DraftAccessDeniedException() {
        super(403, "Only the draft owner can manage this draft");
    }
}
