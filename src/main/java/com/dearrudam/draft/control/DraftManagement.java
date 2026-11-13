package com.dearrudam.draft.control;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.draft.entity.Draft;
import com.dearrudam.draft.entity.DraftRepository;
import com.dearrudam.user.control.InvalidCredentialsException;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class DraftManagement {

    @Inject
    DraftRepository drafts;

    @Inject
    UserRepository users;

    public List<Draft> list(String callerEmail) {
        return drafts.listByOwnerEmail(callerEmail);
    }

    public Draft get(Long id, String callerEmail) {
        return ownedDraft(id, callerEmail);
    }

    @Transactional
    public Draft create(String title, String description, String callerEmail) {
        var owner = users.findByEmail(callerEmail)
                .orElseThrow(InvalidCredentialsException::new);
        var draft = new Draft();
        draft.owner = owner;
        draft.title = title;
        draft.description = description;
        drafts.persist(draft);
        return draft;
    }

    @Transactional
    public Draft update(Long id, Optional<String> title, Optional<String> description, String callerEmail) {
        var draft = ownedDraft(id, callerEmail);
        title.ifPresent(t -> draft.title = t);
        description.ifPresent(d -> draft.description = d);
        return draft;
    }

    @Transactional
    public void delete(Long id, String callerEmail) {
        drafts.delete(ownedDraft(id, callerEmail));
    }

    private Draft ownedDraft(Long id, String callerEmail) {
        var draft = drafts.findById(id);
        if (draft == null) {
            throw new DraftNotFoundException();
        }
        if (!draft.owner.email.equals(callerEmail)) {
            throw new DraftAccessDeniedException();
        }
        return draft;
    }
}
