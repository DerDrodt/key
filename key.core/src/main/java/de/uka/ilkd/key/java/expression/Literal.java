package de.uka.ilkd.key.java.expression;

import org.key_project.util.ExtList;

import de.uka.ilkd.key.java.Expression;
import de.uka.ilkd.key.java.JavaProgramElement;
import de.uka.ilkd.key.java.PositionInfo;
import de.uka.ilkd.key.java.ProgramElement;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.SourceData;
import de.uka.ilkd.key.java.TerminalProgramElement;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.logic.Name;
import de.uka.ilkd.key.rule.MatchConditions;
import de.uka.ilkd.key.util.Debug;

/**
 * Literal.
 *
 * @author <TT>AutoDoc</TT>
 */

public abstract class Literal extends JavaProgramElement
        implements Expression, TerminalProgramElement {


    /**
     * Constructor for the transformation of COMPOST ASTs to KeY.
     *
     * @param children the children of this AST element as KeY classes. May contain: Comments
     */
    public Literal(ExtList children) {
        super(children);
    }

    /**
     * Literal
     */
    public Literal() {

    }

    /**
     * Literal with specific source code position.
     *
     * @param children the children of this AST element as KeY classes. May contain: Comments
     * @param pos The specific source code position.
     */
    public Literal(ExtList children, PositionInfo pos) {
        super(children, pos);
    }

    /**
     * Literal with specific source code position.
     *
     * @param pos The specific source code position.
     */
    public Literal(PositionInfo pos) {
        super(pos);
    }

    /**
     * retrieves the literal's type (as it is independant of the execution context, it is same as
     * using {@link #getKeYJavaType(Services)})
     *
     * @param javaServ the Services offering access to the Java model
     * @param ec the ExecutionContext in which the expression is evaluated
     * @return the literal's type
     */
    @Override
    public KeYJavaType getKeYJavaType(Services javaServ, ExecutionContext ec) {
        return getKeYJavaType(javaServ);
    }

    /**
     * retrieves the literal's type
     *
     * @param javaServ the Services offering access to the Java model
     * @return the literal's type
     */
    public abstract KeYJavaType getKeYJavaType(Services javaServ);


    @Override
    public MatchConditions match(SourceData source, MatchConditions matchCond) {
        final ProgramElement src = source.getSource();
        if (this.equals(src)) {
            source.next();
            return matchCond;
        } else {
            LOGGER.debug("Program match failed (pattern {}, source {})", this, src);
            return null;
        }
    }

    /*
     * Return the Name of the LDT, which this Literal belongs to.
     */
    public abstract Name getLDTName();

}
