package de.uka.ilkd.key.rule;

import java.util.List;

import de.uka.ilkd.key.collection.ImmutableList;
import de.uka.ilkd.key.collection.ImmutableSLList;
import de.uka.ilkd.key.collection.ImmutableSet;
import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.java.abstraction.KeYJavaType;
import de.uka.ilkd.key.logic.PosInOccurrence;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.op.ObserverFunction;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.speclang.Contract;

public class UseDependencyContractApp extends AbstractContractRuleApp {

    private final PosInOccurrence step;
	
	public UseDependencyContractApp(BuiltInRule builtInRule, PosInOccurrence pio) {
	    this(builtInRule, pio, null, null);
    }

	public UseDependencyContractApp(BuiltInRule builtInRule, PosInOccurrence pio,
			Contract instantiation, PosInOccurrence step) {
	    this(builtInRule, pio, instantiation, ImmutableSLList.<PosInOccurrence>nil(), step);
    }
	
    public UseDependencyContractApp(BuiltInRule rule,
            PosInOccurrence pio, Contract contract,
            ImmutableList<PosInOccurrence> ifInsts, PosInOccurrence step) {
	    super(rule, pio, ifInsts, contract);
	    this.step = step;

    }

	public UseDependencyContractApp replacePos(PosInOccurrence newPos) {
	    return new UseDependencyContractApp(rule(), newPos, instantiation, step);
    }

    public boolean isSufficientlyComplete() {
    	return super.complete() && instantiation != null && !ifInsts.isEmpty();    	
    }
    
    public boolean complete() {
    	return super.complete() && instantiation != null && step != null;
    }

	public UseDependencyContractApp computeStep(Sequent seq, Services services) {
		assert this.step == null;
		final List<PosInOccurrence> steps = 
				UseDependencyContractRule.
				 getSteps(this.posInOccurrence(), seq, services);                
		PosInOccurrence l_step = 
				UseDependencyContractRule.findStepInIfInsts(steps, this, services);
		assert l_step != null;/* 
				: "The strategy failed to properly "
				+ "instantiate the base heap!\n"
				+ "at: " + app.posInOccurrence().subTerm() + "\n"
				+ "ifInsts: " + app.ifInsts() + "\n"
				+ "steps: " + steps;*/
		return setStep(l_step);
	}
	
	
	public PosInOccurrence step(Sequent seq, Services services) {
			return step;
	}
	
	public UseDependencyContractApp setStep(PosInOccurrence p_step) {
	    assert this.step == null;
		return new UseDependencyContractApp(rule(), 
	    		posInOccurrence(), instantiation, ifInsts(), p_step);
    }

	@Override
    public UseDependencyContractApp setContract(Contract contract) {
	    return new UseDependencyContractApp(builtInRule, posInOccurrence(), contract, 
	    		ifInsts, step);
    }
	
    public UseDependencyContractRule rule() {
    	return (UseDependencyContractRule) super.rule();
    }

	public UseDependencyContractApp tryToInstantiate(Goal goal) {
    	if (complete()) {
    		return this;
    	}
    	UseDependencyContractApp app = this;

    	final Services services = goal.proof().getServices();

		app = tryToInstantiateContract(services);		
    	
    	if (!app.complete() && app.isSufficientlyComplete()) {
    		app = app.computeStep(goal.sequent(), services);
    	}
    	return app;
    }

	public UseDependencyContractApp tryToInstantiateContract(final Services services) {
	    final Term focus = posInOccurrence().subTerm();
    	final ObserverFunction target = (ObserverFunction) focus.op();
    
    	final Term selfTerm;
    	final KeYJavaType kjt;
    
    	if (target.isStatic()) {
    		selfTerm = null;
    		kjt = target.getContainerType();
    	} else {
    		selfTerm = focus.sub(1);
    		kjt = services.getJavaInfo().getKeYJavaType(
    		        selfTerm.sort());
    	}
    	ImmutableSet<Contract> contracts = UseDependencyContractRule.getApplicableContracts(
    	                services, kjt, target);

    	if (contracts.size() > 0) {
    		return setContract(contracts.iterator().next());
    	}
	    return this;
    }


	
}