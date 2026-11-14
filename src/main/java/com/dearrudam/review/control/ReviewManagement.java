package com.dearrudam.review.control;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.dearrudam.cfp.control.CfpNotFoundException;
import com.dearrudam.cfp.entity.Cfp;
import com.dearrudam.cfp.entity.CfpCommitteeRepository;
import com.dearrudam.cfp.entity.CfpRepository;
import com.dearrudam.event.control.EventNotFoundException;
import com.dearrudam.event.entity.EventRepository;
import com.dearrudam.proposal.control.ProposalNotFoundException;
import com.dearrudam.proposal.entity.Proposal;
import com.dearrudam.proposal.entity.ProposalRepository;
import com.dearrudam.proposal.entity.ProposalStatus;
import com.dearrudam.review.entity.Review;
import com.dearrudam.review.entity.ReviewRepository;
import com.dearrudam.user.entity.User;
import com.dearrudam.user.entity.UserRepository;

@ApplicationScoped
public class ReviewManagement {

    @Inject
    ReviewRepository reviews;

    @Inject
    ProposalRepository proposals;

    @Inject
    CfpRepository cfps;

    @Inject
    CfpCommitteeRepository committee;

    @Inject
    EventRepository events;

    @Inject
    UserRepository users;

    @Transactional
    public Review submit(Long proposalId, Integer score, String comment, String callerEmail) {
        var proposal = proposal(proposalId);
        var reviewer = committeeMember(proposal.cfp, callerEmail,
                "Only committee members can submit reviews");
        requireEval(proposal);
        var review = reviews.find(proposal, reviewer).orElseGet(() -> {
            var created = new Review();
            created.proposal = proposal;
            created.reviewer = reviewer;
            return created;
        });
        review.score = score;
        review.comment = comment;
        reviews.persist(review);
        return review;
    }

    public List<Review> list(Long proposalId, String callerEmail) {
        var proposal = proposal(proposalId);
        if (!isCommitteeMember(proposal.cfp, callerEmail) && !isEventOwner(proposal, callerEmail)) {
            throw new ReviewAccessDeniedException(
                    "Only committee members or the event owner can view reviews");
        }
        return reviews.listByProposal(proposal);
    }

    public List<Proposal> queue(Long eventId, String callerEmail) {
        var event = events.findById(eventId);
        if (event == null) {
            throw new EventNotFoundException();
        }
        var cfp = cfps.find("event", event).firstResultOptional()
                .orElseThrow(CfpNotFoundException::new);
        committeeMember(cfp, callerEmail, "Only committee members can view the review queue");
        return proposals.list("cfp = ?1 and status = ?2", cfp, ProposalStatus.EVAL);
    }

    Proposal proposal(Long id) {
        var proposal = proposals.findById(id);
        if (proposal == null) {
            throw new ProposalNotFoundException();
        }
        return proposal;
    }

    User committeeMember(Cfp cfp, String callerEmail, String deniedMessage) {
        return users.findByEmail(callerEmail)
                .filter(user -> cfp != null && committee.find(cfp, user).isPresent())
                .orElseThrow(() -> new ReviewAccessDeniedException(deniedMessage));
    }

    boolean isCommitteeMember(Cfp cfp, String callerEmail) {
        return cfp != null && users.findByEmail(callerEmail)
                .map(user -> committee.find(cfp, user).isPresent())
                .orElse(false);
    }

    boolean isEventOwner(Proposal proposal, String callerEmail) {
        return proposal.cfp != null && proposal.cfp.event.owner.email.equals(callerEmail);
    }

    void requireEval(Proposal proposal) {
        if (proposal.status != ProposalStatus.EVAL) {
            throw new InvalidReviewStateException("Proposal can only be reviewed while in EVAL");
        }
    }
}
