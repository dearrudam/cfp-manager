package com.dearrudam.review;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Generated from the capability spec in [review] — do not edit.
/// Marks the boundary method or test that realizes the given requirement statements.
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReviewRequirement {

    /// One constant per statement id in the spec's `## Requirements`.
    enum Rn {
        /// Quando um membro do comitê do CFP da proposta submete uma avaliação para uma proposta em EVAL, a capability deve registrar a avaliação com o score e o comentário informados.
        R1_1("R1.1", "Quando um membro do comitê do CFP da proposta submete uma avaliação para uma proposta em EVAL, a capability deve registrar a avaliação com o score e o comentário informados."),
        /// A capability deve manter no máximo uma avaliação por revisor para cada proposta.
        R1_2("R1.2", "A capability deve manter no máximo uma avaliação por revisor para cada proposta."),
        /// Quando o revisor já possui uma avaliação para a proposta e submete novamente, a capability deve atualizar a avaliação existente.
        R1_3("R1.3", "Quando o revisor já possui uma avaliação para a proposta e submete novamente, a capability deve atualizar a avaliação existente."),
        /// Se o autor da submissão não for membro do comitê do CFP da proposta, então a capability deve rejeitar a submissão.
        R1_4("R1.4", "Se o autor da submissão não for membro do comitê do CFP da proposta, então a capability deve rejeitar a submissão."),
        /// Se a proposta não estiver em EVAL, então a capability deve rejeitar a submissão.
        R1_5("R1.5", "Se a proposta não estiver em EVAL, então a capability deve rejeitar a submissão."),
        /// Se o score estiver fora do intervalo de 1 a 6, então a capability deve rejeitar a submissão.
        R1_6("R1.6", "Se o score estiver fora do intervalo de 1 a 6, então a capability deve rejeitar a submissão."),
        /// Se o comentário estiver ausente ou vazio, então a capability deve rejeitar a submissão.
        R1_7("R1.7", "Se o comentário estiver ausente ou vazio, então a capability deve rejeitar a submissão."),
        /// Quando um membro do comitê do CFP da proposta lista as avaliações da proposta, a capability deve retornar todas as avaliações registradas.
        R2_1("R2.1", "Quando um membro do comitê do CFP da proposta lista as avaliações da proposta, a capability deve retornar todas as avaliações registradas."),
        /// Quando o owner do evento ao qual o CFP pertence lista as avaliações da proposta, a capability deve retornar todas as avaliações registradas.
        R2_2("R2.2", "Quando o owner do evento ao qual o CFP pertence lista as avaliações da proposta, a capability deve retornar todas as avaliações registradas."),
        /// Se o solicitante não for membro do comitê do CFP da proposta nem o owner do evento ao qual o CFP pertence, então a capability deve rejeitar a consulta.
        R2_3("R2.3", "Se o solicitante não for membro do comitê do CFP da proposta nem o owner do evento ao qual o CFP pertence, então a capability deve rejeitar a consulta."),
        /// Quando um membro do comitê lista a fila de revisão de um CFP, a capability deve retornar as propostas daquele CFP que estejam em EVAL.
        R3_1("R3.1", "Quando um membro do comitê lista a fila de revisão de um CFP, a capability deve retornar as propostas daquele CFP que estejam em EVAL."),
        /// Se o solicitante não for membro do comitê do CFP, então a capability deve rejeitar a consulta.
        R3_2("R3.2", "Se o solicitante não for membro do comitê do CFP, então a capability deve rejeitar a consulta.");

        private final String id;
        private final String statement;

        Rn(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String statement() {
            return statement;
        }

        @Override
        public String toString() {
            return id;
        }
    }

    Rn[] value();
}
