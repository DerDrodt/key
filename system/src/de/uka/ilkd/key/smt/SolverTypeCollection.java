// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//

package de.uka.ilkd.key.smt;

import java.util.Iterator;
import java.util.LinkedList;

public class SolverTypeCollection implements Iterable<SolverType> {
    public final static SolverTypeCollection EMPTY_COLLECTION = new SolverTypeCollection();

    private LinkedList<SolverType> types = new LinkedList<SolverType>();
    private String name = "";
    private int minUsableSolver = 1;

    /**
     * 
     * @param type
     *            at least on solver type must be passed.
     * @param types
     * @param minUsableSolvers
     *            specifies how many solvers at leas must be usable, so that
     *            <code>isUsable</code> returns true.
     */
    public SolverTypeCollection(String name, int minUsableSolvers,
	    SolverType type, SolverType... types) {
	this.types.add(type);
	this.name = name;
	this.minUsableSolver = minUsableSolvers;
	for (SolverType t : types) {
	    this.types.add(t);
	}
    }

    public SolverTypeCollection(String[] solverNames) {

    }

    private SolverTypeCollection() {

    }

    public LinkedList<SolverType> getTypes() {
	return types;
    }

    public boolean isUsable() {
	int usableCount = 0;
	for (SolverType type : types) {
	    if (type.isInstalled(false)) {
		usableCount++;
	    }
	}

	return usableCount >= minUsableSolver;
    }

    public String name() {
	return name;
    }

    public String toString() {
	String s = "";

	int i = 0;
	for (SolverType type : types) {
	    if (type.isInstalled(false)) {
		if (i > 0) {
		    s += ", ";
		}
		s += type.getName();
		i++;
	    }
	}
	if (s.isEmpty()) {
	    return "No solver available.";
	}
	return s;
    }

    @Override
    public Iterator<SolverType> iterator() {
	return types.iterator();
    }
}