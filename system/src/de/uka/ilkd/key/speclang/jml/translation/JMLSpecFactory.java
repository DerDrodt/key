// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.speclang.jml.translation;

import de.uka.ilkd.key.collection.*;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.Statement;
import de.uka.ilkd.key.java.StatementContainer;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.java.declaration.LocalVariableDeclaration;
import de.uka.ilkd.key.java.declaration.ParameterDeclaration;
import de.uka.ilkd.key.java.declaration.VariableSpecification;
import de.uka.ilkd.key.java.declaration.modifier.Private;
import de.uka.ilkd.key.java.declaration.modifier.Protected;
import de.uka.ilkd.key.java.declaration.modifier.Public;
import de.uka.ilkd.key.java.declaration.modifier.VisibilityModifier;
import de.uka.ilkd.key.java.statement.BranchStatement;
import de.uka.ilkd.key.java.statement.For;
import de.uka.ilkd.key.java.statement.LoopStatement;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.TermBuilder;
import de.uka.ilkd.key.logic.op.*;
import de.uka.ilkd.key.speclang.*;
import de.uka.ilkd.key.speclang.jml.pretranslation.*;
import de.uka.ilkd.key.speclang.translation.SLTranslationException;
import de.uka.ilkd.key.util.Pair;
import de.uka.ilkd.key.util.Triple;


/**
 * A factory for creating class invariants and operation contracts
 * from textual JML specifications. This is the public interface to the 
 * jml.translation package.
 */
public class JMLSpecFactory {
    
    private static final TermBuilder TB = TermBuilder.DF;
    
    private final Services services;
    private final JMLTranslator translator;
    
    private int invCounter;
    
    
    //-------------------------------------------------------------------------
    //constructors
    //-------------------------------------------------------------------------
  
    public JMLSpecFactory(Services services) {
        assert services != null;
        this.services = services;
        this.translator = new JMLTranslator(services);
    }
    
    
    
    //-------------------------------------------------------------------------
    //internal classes
    //-------------------------------------------------------------------------
    
    private class ContractClauses {
	public Term requires;
	public Term measuredBy;
	public Term assignable;
	public Term accessible;
	public Term ensures;
	public Term signals;
	public Term signalsOnly;
	public Term diverges;
	public Term saveFor;
	public Term declassify;
    }
    
    
    
    //-------------------------------------------------------------------------
    //internal methods
    //-------------------------------------------------------------------------
    
    private String getInvName() {
        return "JML class invariant nr " + invCounter++;
    }
    
    
    private String getContractName(ProgramMethod programMethod, 
	                           Behavior behavior) {
        return "JML " + behavior.toString() + "operation contract";
    }
    
    
    /**
     * Collects local variables of the passed statement that are visible for 
     * the passed loop. Returns null if the loop has not been found.
     */
    private ImmutableList<ProgramVariable> collectLocalVariables(
	    					StatementContainer sc, 
                                                LoopStatement loop){
        ImmutableList<ProgramVariable> result 
        	= ImmutableSLList.<ProgramVariable>nil();
        for(int i = 0, m = sc.getStatementCount(); i < m; i++) {
            Statement s = sc.getStatementAt(i);
            
            if(s instanceof For) {
        	ImmutableArray<VariableSpecification> avs 
        		= ((For)s).getVariablesInScope();
        	for(int j = 0, n = avs.size(); j < n; j++) {
        	    VariableSpecification vs = avs.get(j);
        	    ProgramVariable pv 
        	    	= (ProgramVariable) vs.getProgramVariable();
        	    result = result.prepend(pv);
        	}
            }

            if(s == loop) {
                return result;
            } else if(s instanceof LocalVariableDeclaration) {
                ImmutableArray<VariableSpecification> vars = 
                    ((LocalVariableDeclaration) s).getVariables();
                for(int j = 0, n = vars.size(); j < n; j++) {
                    ProgramVariable pv 
                        = (ProgramVariable) vars.get(j).getProgramVariable();
                    result = result.prepend(pv);
                }
            } else if(s instanceof StatementContainer) {
                ImmutableList< ProgramVariable > lpv 
                    = collectLocalVariables((StatementContainer) s, loop);
                if(lpv != null){ 
                    result = result.prepend(lpv);
                    return result;
                }
            } else if(s instanceof BranchStatement) {
                BranchStatement bs = (BranchStatement) s;
                for(int j = 0, n = bs.getBranchCount(); j < n; j++) {
                    ImmutableList< ProgramVariable > lpv 
                        = collectLocalVariables(bs.getBranchAt(j), loop);
                    if(lpv != null){ 
                        result = result.prepend(lpv);
                        return result;
                    }
                }
            }
        }
        return null;
    }
    
    
    private VisibilityModifier getVisibility(
	    				TextualJMLConstruct textualConstruct) {
	for(String mod : textualConstruct.getMods()) {
	    if(mod.equals("private")) {
		return new Private();
	    } else if(mod.equals("protected")) {
		return new Protected();
	    } else if(mod.equals("public")) {
		return new Public();
	    }
	}	
	return null;
    }
        
    
    /* Create variables for self, parameters, result, exception,
     * and the map for atPre-Functions
     */
    private ProgramVariableCollection createProgramVaribales(ProgramMethod pm) {
	ProgramVariableCollection progVar = new ProgramVariableCollection();
	progVar.selfVar 
        	= TB.selfVar(services, pm, pm.getContainerType(), false);
	progVar.paramVars 
        	= TB.paramVars(services, pm, false);
	progVar.resultVar = TB.resultVar(services, pm, false);
	progVar.excVar = TB.excVar(services, pm, false);
	progVar.heapAtPreVar 
        	= TB.heapAtPreVar(services, "heapAtPre", false);
	progVar.heapAtPre = TB.var(progVar.heapAtPreVar);
	return progVar;
    }

    
    private ContractClauses translateJMLClauses(ProgramMethod pm,
	    TextualJMLSpecCase textualSpecCase,
	    ProgramVariableCollection progVars, Behavior originalBehavior)
	    throws SLTranslationException {
	ContractClauses clauses = new ContractClauses();
	clauses.requires = translateRequires(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getRequires());
	clauses.measuredBy = translateMeasuredBy(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getMeasuredBy());
	clauses.assignable = translateAssignable(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getAssignable());
	clauses.accessible = translateAccessible(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getAccessible());
	clauses.ensures = translateEnsures(pm, progVars.selfVar,
	        progVars.paramVars, progVars.resultVar, progVars.excVar,
	        progVars.heapAtPre, originalBehavior, textualSpecCase
	                .getEnsures());
	clauses.signals = translateSignals(pm, progVars.selfVar,
	        progVars.paramVars, progVars.resultVar, progVars.excVar,
	        progVars.heapAtPre, originalBehavior, textualSpecCase
	                .getSignals());
	clauses.signalsOnly = translateSignalsOnly(pm, progVars.excVar,
	        originalBehavior, textualSpecCase.getSignalsOnly());
	clauses.diverges = translateDeverges(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getDiverges());
	clauses.saveFor = translateSaveFor(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getSaveFor());
	clauses.declassify = translateDeclassify(pm, progVars.selfVar,
	        progVars.paramVars, textualSpecCase.getDeclassify());
	return clauses;
    }
    
    
    private Term translateSaveFor(ProgramMethod pm, ProgramVariable selfVar,
            ImmutableList<ProgramVariable> paramVars,
            ImmutableList<PositionedString> saveFor) {
	// TODO Auto-generated method stub
	return null;
    }


    private Term translateDeclassify(ProgramMethod pm,
            ProgramVariable selfVar, ImmutableList<ProgramVariable> paramVars,
            ImmutableList<PositionedString> declassify) {
	// TODO Auto-generated method stub
	return null;
    }


    private Term translateDeverges(ProgramMethod pm, ProgramVariable selfVar,
            ImmutableList<ProgramVariable> paramVars,
            ImmutableList<PositionedString> originalDiverges)
            throws SLTranslationException {
	Term diverges = TB.ff();
        for(PositionedString expr : originalDiverges) {
            Term translated 
                = translator.translateExpression(
                    expr, 
                    pm.getContainerType(),
                    selfVar, 
                    paramVars, 
                    null, 
                    null,
                    null);
            diverges = TB.or(diverges, translated);        
        }
	return diverges;
    }



    private Term translateSignalsOnly(ProgramMethod pm, ProgramVariable excVar,
	    Behavior originalBehavior,
            ImmutableList<PositionedString> originalSignalsOnly)
            throws SLTranslationException {
	if (originalBehavior == Behavior.NORMAL_BEHAVIOR) {
	    assert originalSignalsOnly.isEmpty();
	    return TB.ff();
	} else {
	    Term signalsOnly = TB.tt();
	    for (PositionedString expr : originalSignalsOnly) {
		Term translated = translator.translateSignalsOnlyExpression(
		        expr, pm.getContainerType(), excVar);
		signalsOnly = TB.and(signalsOnly, translated);
	    }
	    return signalsOnly;
	}
    }


    private Term translateSignals(
	    	ProgramMethod pm,
	    	ProgramVariable selfVar,
	    	ImmutableList<ProgramVariable> paramVars,
	    	ProgramVariable resultVar,
	    	ProgramVariable excVar,
	    	Term heapAtPre,
	    	Behavior originalBehavior,
	    	ImmutableList<PositionedString> originalSignals)
            throws SLTranslationException {
	if (originalBehavior == Behavior.NORMAL_BEHAVIOR) {
	    assert originalSignals.isEmpty();
	    return TB.ff();
	} else {
	    Term signals = TB.tt();
	    for (PositionedString expr : originalSignals) {
		Term translated = translator.translateSignalsExpression(expr,
		        pm.getContainerType(), selfVar, paramVars, resultVar,
		        excVar, heapAtPre);
		signals = TB.and(signals, translated);
	    }
	    return signals;
	}
    }



    private Term translateEnsures(ProgramMethod pm, ProgramVariable selfVar,
            ImmutableList<ProgramVariable> paramVars,
            ProgramVariable resultVar, ProgramVariable excVar, Term heapAtPre,
            Behavior originalBehavior,
            ImmutableList<PositionedString> originalEnsures)
            throws SLTranslationException {
	if (originalBehavior == Behavior.EXCEPTIONAL_BEHAVIOR) {
	    assert originalEnsures.isEmpty();
	    return TB.ff();
	} else {
	    Term ensures = TB.tt();
	    for (PositionedString expr : originalEnsures) {
		Term translated = translator.translateExpression(expr, pm
		        .getContainerType(), selfVar, paramVars, resultVar,
		        excVar, heapAtPre);
		ensures = TB.and(ensures, translated);
	    }
	    return ensures;
	}
    }



    private Term translateAccessible(ProgramMethod pm, ProgramVariable selfVar,
            ImmutableList<ProgramVariable> paramVars,
            ImmutableList<PositionedString> originalAccessible)
            throws SLTranslationException {
	Term accessible;
        if(originalAccessible.isEmpty()) {
            accessible = null;
        } else {
            accessible = TB.empty(services);
            for(PositionedString expr : originalAccessible) {
        	Term translated 
        		= translator.translateAssignableExpression(
        				expr, 
        				pm.getContainerType(),
        				selfVar, 
        				paramVars);
        	accessible = TB.union(services, accessible, translated);
            }
        }
	return accessible;
    }
    
    
    private Term translateRequires(ProgramMethod pm, ProgramVariable selfVar,
	    ImmutableList<ProgramVariable> paramVars,
	    ImmutableList<PositionedString> originalRequires)
	    throws SLTranslationException {

	Term requires = TB.tt();
	for (PositionedString expr : originalRequires) {
	    Term translated = translator.translateExpression(expr, pm
		    .getContainerType(), selfVar, paramVars, null, null, null);
	    requires = TB.and(requires, translated);
	}
	return requires;
    }
    
    
    private Term translateMeasuredBy(ProgramMethod pm, ProgramVariable selfVar,
	    ImmutableList<ProgramVariable> paramVars,
	    ImmutableList<PositionedString> originalMeasuredBy)
	    throws SLTranslationException {

	Term measuredBy;
	if (originalMeasuredBy.isEmpty()) {
	    measuredBy = null;
	} else {
	    measuredBy = TB.zero(services);
	    for (PositionedString expr : originalMeasuredBy) {
		Term translated = translator.translateExpression(expr, pm
		        .getContainerType(), selfVar, paramVars, null, null,
		        null);
		measuredBy = TB.add(services, measuredBy, translated);
	    }
	}
	return measuredBy;
    }
    
    
    private Term translateAssignable(ProgramMethod pm, ProgramVariable selfVar,
	    ImmutableList<ProgramVariable> paramVars,
	    ImmutableList<PositionedString> originalAssignable)
	    throws SLTranslationException {

	Term assignable;
	if (originalAssignable.isEmpty()) {
	    assignable = TB.allLocs(services);
	} else {
	    assignable = TB.empty(services);
	    for (PositionedString expr : originalAssignable) {
		Term translated = translator.translateAssignableExpression(
		        expr, pm.getContainerType(), selfVar, paramVars);
		assignable = TB.union(services, assignable, translated);
	    }
	}
	return assignable;
    }
    
    
    private String generateName(ProgramMethod pm,
            TextualJMLSpecCase textualSpecCase, Behavior originalBehavior) {
	PositionedString customName = textualSpecCase.getName();
	String name = (customName.text.length() > 0 ? customName.text
	        : getContractName(pm, originalBehavior));
	return name;
    }


    private Term generatePostCondition(ProgramVariableCollection progVars,
            ContractClauses clauses, Behavior originalBehavior) {
	Term excNull = TB.equals(TB.var(progVars.excVar), TB.NULL(services));
	Term post1 = (originalBehavior == Behavior.NORMAL_BEHAVIOR ? clauses.ensures
	        : TB.imp(excNull, clauses.ensures));
	Term post2 = (originalBehavior == Behavior.EXCEPTIONAL_BEHAVIOR ? TB
	        .and(clauses.signals, clauses.signalsOnly) : TB.imp(TB
	        .not(excNull), TB.and(clauses.signals, clauses.signalsOnly)));
	Term post = TB.and(post1, post2);
	return post;
    }
    

    /**
     * Generate functional operation contracts.
     * 
     * @param name	base name of the contract (does not have to be unique)
     * @param pm	the ProgramMethod to which the contract belongs
     * @param progVars	pre-generated collection of variables for the receiver
     * 			object, operation parameters, operation result, thrown
     * 			exception and the pre-heap
     * @param clauses	pre-translated JML clauses
     * @param post	pre-generated post condition
     * @param result	immutable set of already generated operation contracts 
     * @return		operation contracts including new functional operation
     * 			contracts
     */
    private ImmutableSet<Contract> createFunctionalOperationContracts(String name,
            ProgramMethod pm, ProgramVariableCollection progVars,
            ContractClauses clauses, Term post, ImmutableSet<Contract> result) {
	if (clauses.diverges.equals(TB.ff())) {
	    FunctionalOperationContract contract
	    	= new FunctionalOperationContractImpl(
	    		name, pm, Modality.DIA, clauses.requires,
	    		clauses.measuredBy, post, clauses.assignable, progVars,
	    		false);
	    result = result.add(contract);
	} else if (clauses.diverges.equals(TB.tt())) {
	    FunctionalOperationContract contract
	    	= new FunctionalOperationContractImpl(
	    		name, pm, Modality.BOX, clauses.requires,
	    		clauses.measuredBy, post, clauses.assignable, progVars,
	    		false);
	    result = result.add(contract);
	} else {
	    FunctionalOperationContract contract1
	    	= new FunctionalOperationContractImpl(
	    		name, pm, Modality.DIA,
	    		TB.and(clauses.requires, TB.not(clauses.diverges)),
	    		clauses.measuredBy, post, clauses.assignable,
	    		progVars, false);
	    FunctionalOperationContract contract2
	    	= new FunctionalOperationContractImpl(name, pm, Modality.BOX,
	    		clauses.requires, clauses.measuredBy, post,
	    		clauses.assignable, progVars, false);
	    result = result.add(contract1).add(contract2);
	}
	return result;
    }


    /**
     * Generate dependency operation contract out of the JML accessible clause.
     * 
     * @param pm	the ProgramMethod to which the contract belongs
     * @param progVars	collection of variables for the receiver object,
     * 			operation parameters, operation result, thrown exception
     * 			and the pre-heap
     * @param clauses	pre-translated JML clauses
     * @param result	immutable set of already generated operation contracts
     * @return		operation contracts including a new dependency contract
     */
    private ImmutableSet<Contract> createDependencyOperationContract(
            ProgramMethod pm, ProgramVariableCollection progVars,
            ContractClauses clauses, ImmutableSet<Contract> result) {
	if (clauses.accessible != null) {
	    final Contract depContract
	    	= new DependencyContractImpl(
	    		"JML accessible clause", pm.getContainerType(), pm,
	    		clauses.requires, clauses.measuredBy,
	    		clauses.accessible, progVars.selfVar,
	    		progVars.paramVars);
	    result = result.add(depContract);
	}
	return result;
    }
    
    
    /**
     * Generate non-interference operation contract out of the JML
     * secure_for and declassify clauses.
     * 
     * @param pm	the ProgramMethod to which the contract belongs
     * @param progVars	collection of variables for the receiver object,
     * 			operation parameters, operation result, thrown exception
     * 			and the pre-heap
     * @param clauses	pre-translated JML clauses
     * @param result	immutable set of already generated operation contracts
     * @return		operation contracts including a new non-interference
     * 			contract
     */
    private ImmutableSet<Contract> createInformationFlowOperationContract(
            ProgramMethod pm, ProgramVariableCollection progVars,
            ContractClauses clauses, ImmutableSet<Contract> result) {
	final Contract ifContract
	    = new InformationFlowContractImpl(
	    	"Non-interference contract", pm.getContainerType(), pm,
	    	clauses.requires, clauses.measuredBy,
	    	clauses.accessible, clauses.assignable, clauses.saveFor,
	    	clauses.declassify, progVars.selfVar,
	    	progVars.paramVars);
	result = result.add(ifContract);

	return result;
    }

    
    
    //-------------------------------------------------------------------------
    //public interface
    //-------------------------------------------------------------------------
       
    public ClassInvariant createJMLClassInvariant(KeYJavaType kjt,
	    					  VisibilityModifier visibility,
                                                  PositionedString originalInv) 
            throws SLTranslationException {
        assert kjt != null;
        assert originalInv != null;
        
        //create variable for self
        ProgramVariable selfVar = TB.selfVar(services, kjt, false);
        
        //translate expression
        Term inv = translator.translateExpression(originalInv,
        					  kjt,
        					  selfVar,
        					  null,
        					  null,
        					  null,
        					  null);        
        //create invariant
        String name = getInvName();
        return new ClassInvariantImpl(name,
                                      name,
                                      kjt, 
                                      visibility,
                                      inv,
                                      selfVar);
    }
    
    
    public ClassInvariant createJMLClassInvariant(
                                        KeYJavaType kjt,
                                        TextualJMLClassInv textualInv) 
            throws SLTranslationException {
        return createJMLClassInvariant(kjt,
        			       getVisibility(textualInv),
        			       textualInv.getInv());
    }
    
    
    public ClassAxiom createJMLRepresents(KeYJavaType kjt,
	    				  VisibilityModifier visibility,
                                          PositionedString originalRep,
                                          boolean isStatic) 
            throws SLTranslationException {
        assert kjt != null;
        assert originalRep != null;
        
        //create variable for self
        final ProgramVariable selfVar 
        	= isStatic ? null : TB.selfVar(services, kjt, false);
        
        //translate expression
        final Pair<ObserverFunction,Term> rep 
        	= translator.translateRepresentsExpression(originalRep,
        					  	   kjt,
        					  	   selfVar);
        //create class axiom
        return new RepresentsAxiom("JML represents clause for " 
        	                   + rep.first.name().toString(),
        	                   rep.first,
        	                   kjt,        
        	                   visibility,
        	                   rep.second,
        	                   selfVar);
    }
   
    
    public ClassAxiom createJMLRepresents(KeYJavaType kjt, 
	    				  TextualJMLRepresents textualRep)
    	throws SLTranslationException {
	return createJMLRepresents(kjt, 
				   getVisibility(textualRep),
		                   textualRep.getRepresents(), 
		                   textualRep.getMods().contains("static"));
    }
    
    
    public Contract createJMLDependencyContract(KeYJavaType kjt, 
	    TextualJMLDepends textualDep) 
            throws SLTranslationException {
	PositionedString originalDep = textualDep.getDepends();
        assert kjt != null;
        assert originalDep != null;
        
        //create variable for self
        ProgramVariable selfVar = TB.selfVar(services, kjt, false);
        
        //translate expression
        Triple<ObserverFunction,Term,Term> dep 
        	= translator.translateDependsExpression(originalDep,
        					  	kjt,
        					  	selfVar);
        assert dep.first.arity() <= 2;
        
        //create dependency contract
        final ImmutableList<ProgramVariable> paramVars 
        	= TB.paramVars(services, dep.first, false);        
        return new DependencyContractImpl("JML depends clause",
        				  kjt,
        	                          dep.first,
        				  TB.inv(services, TB.var(selfVar)),
        	                          dep.third,       				  
        	                          dep.second,
        	                          selfVar,
        	                          paramVars);
    }
       
    
    /**
     * Creates operation contracts out of the passed JML specification.
     */
    public ImmutableSet<Contract> createJMLOperationContracts(ProgramMethod pm,
	    TextualJMLSpecCase textualSpecCase) throws SLTranslationException {
	assert pm != null;
	assert textualSpecCase != null;
	
	Behavior originalBehavior = textualSpecCase.getBehavior();
	String name = generateName(pm, textualSpecCase, originalBehavior);
	
	// prepare program variables, translate JML clauses and generate post
	// condition
	ProgramVariableCollection progVars = createProgramVaribales(pm);
	ContractClauses clauses = translateJMLClauses(pm, textualSpecCase,
	        progVars, originalBehavior);
	Term post = generatePostCondition(progVars, clauses, originalBehavior);

	// create contracts
	ImmutableSet<Contract> result = DefaultImmutableSet.<Contract> nil();
	result = createFunctionalOperationContracts(name, pm, progVars,
	        clauses, post, result);
	result = createDependencyOperationContract(pm, progVars, clauses,
		result);
	result = createInformationFlowOperationContract(pm, progVars, clauses,
	        result);

	return result;
    }


    public LoopInvariant createJMLLoopInvariant(
                            ProgramMethod pm,
                            LoopStatement loop,
                            ImmutableList<PositionedString> originalInvariant,
                            ImmutableList<PositionedString> originalAssignable,
                            PositionedString originalVariant) 
            throws SLTranslationException {                
        assert pm != null;
        assert loop != null;
        assert originalInvariant != null;
        assert originalAssignable != null;
        
        //create variables for self, parameters, other relevant local variables 
        //(disguised as parameters to the translator) and the map for 
        //atPre-Functions
        ProgramVariable selfVar 
        	= TB.selfVar(services, pm, pm.getContainerType(), false);
        ImmutableList<ProgramVariable> paramVars 
        	= ImmutableSLList.<ProgramVariable>nil();
        int numParams = pm.getParameterDeclarationCount();
        for(int i = numParams - 1; i >= 0; i--) {
            ParameterDeclaration pd = pm.getParameterDeclarationAt(i);
            paramVars = paramVars.prepend((ProgramVariable) 
                                          pd.getVariableSpecification()
                                             .getProgramVariable());
        }

        ImmutableList<ProgramVariable> localVars 
            = collectLocalVariables(pm.getBody(), loop);        
        paramVars = paramVars.append(localVars);
        Term heapAtPre = TB.var(TB.heapAtPreVar(services, "heapAtPre", false));
        
        //translate invariant
        Term invariant;
        if(originalInvariant.isEmpty()) {
            invariant = null;
        } else {
            invariant = TB.tt();
            for(PositionedString expr : originalInvariant) {
                Term translated 
                    = translator.translateExpression(
                                            expr, 
                                            pm.getContainerType(),
                                            selfVar, 
                                            paramVars, 
                                            null, 
                                            null,
                                            heapAtPre);
                invariant = TB.and(invariant, translated);
            }
        }

        //translate assignable
        Term assignable;
        if(originalAssignable.isEmpty()) {
            assignable = TB.allLocs(services);
        } else {
            assignable = TB.empty(services);
            for(PositionedString expr : originalAssignable) {
        	Term translated 
        	    = translator.translateAssignableExpression(
        		    expr, 
        		    pm.getContainerType(),
        		    selfVar, 
        		    paramVars);
        	assignable = TB.union(services, assignable, translated);        
            }
        }
        
        //translate variant
        Term variant;
        if(originalVariant == null) {
            variant = null;
        } else {
            Term translated 
                = translator.translateExpression(
                                        originalVariant,
                                        pm.getContainerType(),
                                        selfVar,
                                        paramVars,
                                        null,
                                        null,
                                        heapAtPre);
            variant = translated;
        }
        
        //create loop invariant annotation
        Term selfTerm = selfVar == null ? null : TB.var(selfVar);
        return new LoopInvariantImpl(loop,
                                     invariant,
                                     assignable,
                                     variant,
                                     selfTerm,
                                     heapAtPre);
    }
    
    
    public LoopInvariant createJMLLoopInvariant(
                                        ProgramMethod pm,
                                        LoopStatement loop,
                                        TextualJMLLoopSpec textualLoopSpec) 
            throws SLTranslationException {
        return createJMLLoopInvariant(pm,
                                      loop,
                                      textualLoopSpec.getInvariant(),
                                      textualLoopSpec.getAssignable(),
                                      textualLoopSpec.getVariant());
    }
    
}