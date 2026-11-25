package com.dearrudam.proposal.control;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.control.CfpNotFoundException;
import com.dearrudam.cfp.control.InvalidCfpStateException;
import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.cfp.entity.CfpRepository;
import com.dearrudam.cfp.entity.CfpStatus;
import com.dearrudam.event.control.EventNotFoundException;
import com.dearrudam.event.entity.EventRepository;
import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.proposal.entity.ProposalRepository;
import com.dearrudam.user.control.InvalidCredentialsException;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class ProposalSubmission {

    @Inject
    ProposalRepository proposals;

    @Inject
    CfpRepository cfps;

    @Inject
    EventRepository events;

    @Inject
    UserRepository users;

    public List<Proposal> list(String callerEmail) {
        return proposals.listBySpeakerEmail(callerEmail);
    }

    public Proposal get(Long id, String callerEmail) {
        return ownedProposal(id, callerEmail);
    }

    @Transactional
    public Proposal submit(Long eventId, String title, String description, String callerEmail) {
        var cfp = openCfp(eventId);
        var speaker = users.findByEmail(callerEmail)
                .orElseThrow(InvalidCredentialsException::new);
        var proposal = new Proposal();
        proposal.cfp = cfp;
        proposal.speaker = speaker;
        proposal.title = title;
        proposal.description = description;
        proposals.persist(proposal);
        return proposal;
    }

    private Cfp openCfp(Long eventId) {
        var event = events.findById(eventId);
        if (event == null) {
            throw new EventNotFoundException();
        }
        var cfp = cfps.find("event", event).firstResultOptional()
                .orElseThrow(CfpNotFoundException::new);
        if (cfp.status != CfpStatus.OPEN) {
            throw new InvalidCfpStateException("CFP is not open");
        }
        return cfp;
    }

    private Proposal ownedProposal(Long id, String callerEmail) {
        var proposal = proposals.findById(id);
        if (proposal == null) {
            throw new ProposalNotFoundException();
        }
        if (!proposal.speaker.email.equals(callerEmail)) {
            throw new ProposalAccessDeniedException();
        }
        return proposal;
    }
}
