package com.dearrudam.proposal.control;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.control.CfpNotFoundException;
import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.cfp.entity.CfpRepository;
import com.dearrudam.cfp.entity.CfpStatus;
import com.dearrudam.draft.control.DraftManagement;
import com.dearrudam.event.control.EventNotFoundException;
import com.dearrudam.event.entity.EventRepository;
import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.proposal.entity.ProposalRepository;
import com.dearrudam.proposal.entity.ProposalSpeaker;
import com.dearrudam.proposal.entity.ProposalSpeakerRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.proposal.entity.TalkLevel;
import com.dearrudam.user.control.InvalidCredentialsException;
import com.dearrudam.user.control.UserNotFoundException;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class ProposalManagement {

    @Inject
    ProposalRepository proposals;

    @Inject
    ProposalSpeakerRepository speakers;

    @Inject
    CfpRepository cfps;

    @Inject
    EventRepository events;

    @Inject
    UserRepository users;

    @Inject
    DraftManagement drafts;

    public List<Proposal> mine(String callerEmail) {
        return proposals.listByOwnerEmail(callerEmail);
    }

    public List<ProposalSpeaker> coSpeakers(Long proposalId) {
        return speakers.listByProposal(get(proposalId));
    }

    public Proposal get(Long id) {
        var proposal = proposals.findById(id);
        if (proposal == null) {
            throw new ProposalNotFoundException();
        }
        return proposal;
    }

    @Transactional
    public CreatedProposal create(Long eventId, String title, String abstractText, TalkLevel level,
            Integer durationMinutes, boolean saveAsDraft, String callerEmail) {
        var cfp = openCfp(eventId);
        var owner = users.findByEmail(callerEmail)
                .orElseThrow(InvalidCredentialsException::new);
        var proposal = new Proposal();
        proposal.title = title;
        proposal.abstractText = abstractText;
        proposal.level = level;
        proposal.durationMinutes = durationMinutes;
        proposal.status = ProposalStatus.SUBMITTED;
        proposal.cfp = cfp;
        proposal.owner = owner;
        proposals.persist(proposal);
        var draft = saveAsDraft
                ? drafts.create(title, abstractText, level, durationMinutes, callerEmail)
                : null;
        return new CreatedProposal(proposal, draft);
    }

    @Transactional
    public Proposal update(Long id, String title, String abstractText, TalkLevel level,
            Integer durationMinutes, String callerEmail) {
        var proposal = editableProposal(id, callerEmail);
        proposal.title = title;
        proposal.abstractText = abstractText;
        proposal.level = level;
        proposal.durationMinutes = durationMinutes;
        return proposal;
    }

    @Transactional
    public ProposalSpeaker addCoSpeaker(Long proposalId, Long userId, String callerEmail) {
        var proposal = editableProposal(proposalId, callerEmail);
        var user = users.findById(userId);
        if (user == null) {
            throw new UserNotFoundException();
        }
        if (user.id.equals(proposal.owner.id)) {
            throw new InvalidProposalStateException("The owner cannot be added as a co-speaker");
        }
        if (speakers.find(proposal, user).isPresent()) {
            throw new InvalidProposalStateException("User is already a co-speaker of this proposal");
        }
        var speaker = new ProposalSpeaker();
        speaker.proposal = proposal;
        speaker.user = user;
        speakers.persist(speaker);
        return speaker;
    }

    private Proposal editableProposal(Long id, String callerEmail) {
        var proposal = get(id);
        if (!proposal.owner.email.equals(callerEmail)) {
            throw new ProposalAccessDeniedException();
        }
        if (proposal.cfp == null || proposal.cfp.status != CfpStatus.OPEN) {
            throw new InvalidProposalStateException("Proposal can only be modified while the CFP is open");
        }
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
            throw new InvalidProposalStateException("CFP is not open for submissions");
        }
        return cfp;
    }
}
