package com.dearrudam.cfp.control;

import java.time.Instant;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.cfp.entity.CfpCommitteeMember;
import com.dearrudam.cfp.entity.CfpCommitteeRepository;
import com.dearrudam.cfp.entity.CfpRepository;
import com.dearrudam.cfp.entity.CfpStatus;
import com.dearrudam.event.control.EventNotFoundException;
import com.dearrudam.event.entity.Event;
import com.dearrudam.event.entity.EventRepository;
import com.dearrudam.proposal.control.ProposalManagement;
import com.dearrudam.user.control.UserNotFoundException;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class CfpManagement {

    @Inject
    CfpRepository cfps;

    @Inject
    CfpCommitteeRepository committee;

    @Inject
    EventRepository events;

    @Inject
    UserRepository users;

    @Inject
    ProposalManagement proposals;

    public Cfp get(Long eventId) {
        var event = events.findById(eventId);
        if (event == null) {
            throw new EventNotFoundException();
        }
        return cfps.find("event", event).firstResultOptional()
                .orElseThrow(CfpNotFoundException::new);
    }

    public List<CfpCommitteeMember> committee(Long eventId) {
        return committee.list("cfp = ?1 order by user.name", get(eventId));
    }

    @Transactional
    public Cfp create(Long eventId, String callerEmail) {
        var event = ownedEvent(eventId, callerEmail);
        if (cfps.find("event", event).firstResultOptional().isPresent()) {
            throw new CfpAlreadyExistsException();
        }
        var cfp = new Cfp();
        cfp.event = event;
        cfp.status = CfpStatus.CLOSED;
        cfps.persist(cfp);
        return cfp;
    }

    @Transactional
    public Cfp open(Long eventId, String callerEmail) {
        var cfp = ownedCfp(eventId, callerEmail);
        if (cfp.status == CfpStatus.OPEN) {
            throw new InvalidCfpStateException("CFP is already open");
        }
        if (committee.count("cfp", cfp) == 0) {
            throw new InvalidCfpStateException("CFP requires at least one committee member to open");
        }
        cfp.status = CfpStatus.OPEN;
        cfp.openedAt = Instant.now();
        cfp.closedAt = null;
        return cfp;
    }

    @Transactional
    public Cfp close(Long eventId, String callerEmail) {
        var cfp = ownedCfp(eventId, callerEmail);
        if (cfp.status == CfpStatus.CLOSED) {
            throw new InvalidCfpStateException("CFP is not open");
        }
        cfp.status = CfpStatus.CLOSED;
        cfp.closedAt = Instant.now();
        proposals.moveSubmittedToEval(cfp);
        return cfp;
    }

    @Transactional
    public CfpCommitteeMember addCommitteeMember(Long eventId, Long userId, String callerEmail) {
        var cfp = ownedCfp(eventId, callerEmail);
        var user = users.findById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (committee.find(cfp, user).isPresent()) {
            throw new CommitteeMemberAlreadyExistsException();
        }
        var member = new CfpCommitteeMember();
        member.cfp = cfp;
        member.user = user;
        committee.persist(member);
        return member;
    }

    @Transactional
    public void removeCommitteeMember(Long eventId, Long userId, String callerEmail) {
        var cfp = ownedCfp(eventId, callerEmail);
        var member = committee.findByUserId(cfp, userId)
                .orElseThrow(CommitteeMemberNotFoundException::new);
        if (committee.count("cfp", cfp) == 1) {
            throw new InvalidCfpStateException("CFP must have at least one committee member");
        }
        committee.delete(member);
    }

    private Cfp ownedCfp(Long eventId, String callerEmail) {
        var cfp = get(eventId);
        requireOwner(cfp.event, callerEmail);
        return cfp;
    }

    private Event ownedEvent(Long eventId, String callerEmail) {
        var event = events.findById(eventId);
        if (event == null) {
            throw new EventNotFoundException();
        }
        requireOwner(event, callerEmail);
        return event;
    }

    private void requireOwner(Event event, String callerEmail) {
        if (!event.owner.email.equals(callerEmail)) {
            throw new CfpAccessDeniedException();
        }
    }
}
