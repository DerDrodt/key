package de.uka.ilkd.key.java.expression.operator;

import org.key_project.util.ExtList;

import de.uka.ilkd.key.java.Expression;
import de.uka.ilkd.key.java.PrettyPrinter;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.TypeConverter;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.expression.Operator;
import de.uka.ilkd.key.java.reference.ExecutionContext;
import de.uka.ilkd.key.java.visitor.Visitor;

/**
 * Unsigned shift right.
 *
 */

public class UnsignedShiftRight extends Operator {

    /**
     * Unsigned shift right.
     */

    public UnsignedShiftRight() {}

    /**
     * Unsigned shift right.
     *
     * @param lhs an expression.
     * @param rhs an expression.
     */

    public UnsignedShiftRight(Expression lhs, Expression rhs) {
        super(lhs, rhs);
    }

    /**
     * Constructor for the transformation of COMPOST ASTs to KeY. The first occurrence of an
     * Expression in the given list is taken as the left hand side of the expression, the second
     * occurrence is taken as the right hand side of the expression.
     *
     * @param children the children of this AST element as KeY classes.
     */
    public UnsignedShiftRight(ExtList children) {
        super(children);
    }


    /**
     * Get arity.
     *
     * @return the int value.
     */

    public int getArity() {
        return 2;
    }

    /**
     * Get precedence.
     *
     * @return the int value.
     */

    public int getPrecedence() {
        return 4;
    }

    /**
     * Get notation.
     *
     * @return the int value.
     */

    public int getNotation() {
        return INFIX;
    }

    /**
     * calls the corresponding method of a visitor in order to perform some action/transformation on
     * this element
     *
     * @param v the Visitor
     */
    public void visit(Visitor v) {
        v.performActionOnUnsignedShiftRight(this);
    }

    public void prettyPrint(PrettyPrinter p) throws java.io.IOException {
        p.printUnsignedShiftRight(this);
    }


    public KeYJavaType getKeYJavaType(Services javaServ, ExecutionContext ec) {
        final TypeConverter tc = javaServ.getTypeConverter();
        return tc.getPromotedType(tc.getKeYJavaType((Expression) getChildAt(0), ec));
    }
}
