package de.uka.ilkd.key.macros.scripts;

import de.uka.ilkd.key.java.Services;
import de.uka.ilkd.key.logic.Sequent;
import de.uka.ilkd.key.logic.Term;
import de.uka.ilkd.key.logic.sort.Sort;
import de.uka.ilkd.key.macros.scripts.meta.ValueInjector;
import de.uka.ilkd.key.parser.DefaultTermParser;
import de.uka.ilkd.key.parser.ParserException;
import de.uka.ilkd.key.pp.AbbrevMap;
import de.uka.ilkd.key.proof.Goal;
import de.uka.ilkd.key.proof.Node;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.settings.ProofSettings;
import org.key_project.util.collection.ImmutableList;

import java.io.File;
import java.io.StringReader;
import java.util.*;

/**
 * @author Alexander Weigl
 * @version 1 (28.03.17)
 */
public class EngineState {
    private final static DefaultTermParser PARSER = new DefaultTermParser();
    private final Map<String, Object> arbitraryVariables = new HashMap<>();
    private final Proof proof;
    private AbbrevMap abbrevMap = new AbbrevMap();
    /**
     * nullable
     */
    private Observer observer;
    private File baseFileName = new File(".");
    private ValueInjector valueInjector = ValueInjector.createDefault();
    private Goal goal;

    public EngineState(Proof proof) {
        this.proof = proof;
        valueInjector.addConverter(Term.class, (String s) -> toTerm(s, null));
        valueInjector.addConverter(Sort.class, this::toSort);
    }

    protected static Goal getGoal(ImmutableList<Goal> openGoals, Node node) {
        for (Goal goal : openGoals) {
            if (goal.node() == node) {
                return goal;
            }
        }
        return null;
    }

    public void setGoal(Goal g) {
        goal = g;
    }

    public Proof getProof() {
        return proof;
    }

    public Goal getFirstOpenGoal() throws ScriptException {
        if (goal != null) {
            return goal;
        }

        Node node = proof.root();

        if (node.isClosed()) {
            throw new ScriptException("The proof is closed already");
        }

        Goal g;
        Deque<Node> choices = new LinkedList<Node>();

        while (node != null) {
            assert !node.isClosed();
            int childCount = node.childrenCount();

            switch (childCount) {
                case 0:
                    g = getGoal(proof.openGoals(), node);
                    if (g.isAutomatic()) {
                        return g;
                    }
                    node = choices.pollLast();
                    break;

                case 1:
                    node = node.child(0);
                    break;

                default:
                    Node next = null;
                    for (int i = 0; i < childCount; i++) {
                        Node child = node.child(i);
                        if (!child.isClosed()) {
                            if (next == null) {
                                next = child;
                            } else {
                                choices.add(child);
                            }
                        }
                    }
                    assert next != null;
                    node = next;
                    break;
            }
        }
        assert false : "There must be an open goal at this point";
        return null;
    }

    public Term toTerm(String string, Sort sort)
            throws ParserException, ScriptException {
        StringReader reader = new StringReader(string);
        Services services = proof.getServices();
        Term formula = PARSER
                .parse(reader, sort, services, getFirstOpenGoal().getLocalNamespaces(),
                        abbrevMap);
        return formula;
    }

    public Sort toSort(String sortName)
            throws ParserException, ScriptException {
        return (getFirstOpenGoal() == null
                ? getProof().getServices().getNamespaces()
                : getFirstOpenGoal().getLocalNamespaces())
                .sorts().lookup(sortName);
    }

    public Sequent toSequent(String sequent)
            throws ParserException, ScriptException {
        StringReader reader = new StringReader(sequent);
        Services services = proof.getServices();

        Sequent seq = PARSER.parseSeq(reader, services,
                getFirstOpenGoal().getLocalNamespaces(), getAbbreviations());
        return seq;
    }

    public int getMaxAutomaticSteps() {
        if (proof != null) {
            return proof.getSettings().getStrategySettings().getMaxSteps();
        } else {
            return ProofSettings.DEFAULT_SETTINGS.getStrategySettings()
                    .getMaxSteps();
        }
    }

    public void setMaxAutomaticSteps(int steps) {
        if (proof != null) {
            proof.getSettings().getStrategySettings().setMaxSteps(steps);
        }
        ProofSettings.DEFAULT_SETTINGS.getStrategySettings().setMaxSteps(steps);
    }

    public Observer getObserver() {
        return observer;
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    public File getBaseFileName() {
        return baseFileName;
    }

    public void setBaseFileName(File baseFileName) {
        this.baseFileName = baseFileName;
    }

    public ValueInjector getValueInjector() {
        return valueInjector;
    }

    public AbbrevMap getAbbreviations() {
        return abbrevMap;
    }

    public void setGoal(Node node) {
        setGoal(getGoal(proof.openGoals(), node));
    }

}