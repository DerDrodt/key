package de.uka.ilkd.key.rule;

import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.TermServices;
import de.uka.ilkd.key.proof.Goal;

import javax.annotation.Nullable;

/**
 * Buit-in rule interface. As applications of this rule kind may not be successful in each case one
 * has to ensure that the goal split is done only iff the application was successful.
 */
public interface BuiltInRule extends Rule {

    /**
     * returns true iff a rule is applicable at the given position. This does not necessarily mean
     * that a rule application will change the goal (this decision is made due to performance
     * reasons)
     */
    boolean isApplicable(Goal goal, PosInOccurrence pio);

    boolean isApplicableOnSubTerms();

    IBuiltInRuleApp createApp(PosInOccurrence pos, TermServices services);

    @Nullable
    @Override
    default String getOrigin() {
        return "defined in Java: " + getClass().getName();
    }
}