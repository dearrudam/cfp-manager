/// # Review
/// > Collect committee evaluations — a score and a comment — for proposals under review, and expose them to the committee and the event owner.
///
/// ## Boundary
/// - `submit-review` — create or update the caller's evaluation on a proposal under review
/// - `list-reviews` — list the evaluations recorded for a proposal
/// - `list-review-queue` — list the proposals awaiting review for a CFP
///
/// ## Requirements
///
/// ### R1: Submit review
/// - R1.1 — Quando um membro do comitê do CFP da proposta submete uma avaliação para uma proposta em EVAL, a capability deve registrar a avaliação com o score e o comentário informados.
/// - R1.2 — A capability deve manter no máximo uma avaliação por revisor para cada proposta.
/// - R1.3 — Quando o revisor já possui uma avaliação para a proposta e submete novamente, a capability deve atualizar a avaliação existente.
/// - R1.4 — Se o autor da submissão não for membro do comitê do CFP da proposta, então a capability deve rejeitar a submissão.
/// - R1.5 — Se a proposta não estiver em EVAL, então a capability deve rejeitar a submissão.
/// - R1.6 — Se o score estiver fora do intervalo de 1 a 6, então a capability deve rejeitar a submissão.
/// - R1.7 — Se o comentário estiver ausente ou vazio, então a capability deve rejeitar a submissão.
///
/// ### R2: List reviews
/// - R2.1 — Quando um membro do comitê do CFP da proposta lista as avaliações da proposta, a capability deve retornar todas as avaliações registradas.
/// - R2.2 — Quando o owner do evento ao qual o CFP pertence lista as avaliações da proposta, a capability deve retornar todas as avaliações registradas.
/// - R2.3 — Se o solicitante não for membro do comitê do CFP da proposta nem o owner do evento ao qual o CFP pertence, então a capability deve rejeitar a consulta.
///
/// ### R3: List review queue
/// - R3.1 — Quando um membro do comitê lista a fila de revisão de um CFP, a capability deve retornar as propostas daquele CFP que estejam em EVAL.
/// - R3.2 — Se o solicitante não for membro do comitê do CFP, então a capability deve rejeitar a consulta.
///
/// ## Entities
/// - Review
///
/// ## Decisions
/// - D1 — Scope limited to evaluations; the approve/reject decision is not part of this capability. _(why: escopo reduzido pelo usuário durante a declaração; rejected: review owning the EVAL → APPROVED | REJECTED transition)_
///
/// ## Out of scope
/// - Proposal decision — approving or rejecting a proposal and transitioning it out of EVAL.
/// - Reviewer assignment — any committee member may review any proposal in EVAL.
/// - Review deadlines and editing windows.
/// - Speaker visibility of reviews — evaluations are visible only to the committee and the event owner.
/// - Notifications on new or updated reviews.
package com.dearrudam.review;
