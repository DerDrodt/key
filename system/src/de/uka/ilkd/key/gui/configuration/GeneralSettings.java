// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//
package de.uka.ilkd.key.gui.configuration;

import java.util.LinkedList;
import java.util.Properties;

import de.uka.ilkd.key.gui.GUIEvent;


/** This class encapsulates the information about the active
 * Heuristics and the maximum amount of heuristics steps before an
 * interactive step is required.
 */
public class GeneralSettings implements Settings {


    private static final String TACLET_FILTER = "[General]StupidMode";
    private static final String DND_DIRECTION_SENSITIVE_KEY 
        = "[General]DnDDirectionSensitive";
    private static final String ONE_STEP_SIMPLIFICATION_KEY 
    	= "[General]OneStepSimplification";    
    private static final String USE_JML_KEY = "[General]UseJML";
    private static final String USE_OCL_KEY = "[General]UseOCL";
    
    /** if true then JML/OCL specifications are globally disabled 
     * in this run of KeY, regardless of the regular settings 
     */
    public static boolean disableSpecs = false;
    
    /** minimize interaction is on by default */
    private boolean tacletFilter = true;

    /** suggestive var names are off by default */
    private boolean suggestiveVarNames = false;

    /** is drag and drop instantiation direction sensitive */
    private boolean dndDirectionSensitive = true;
    
    /** is one-step simplification enabled */
    private boolean oneStepSimplification = true;
    
    /** JML is active by default */
    private boolean useJML = true;
    
    /** OCL is not active by default */
    private boolean useOCL = false;

    private LinkedList<SettingsListener> listenerList = 
        new LinkedList<SettingsListener>();


    // getter
    public boolean tacletFilter() {
	return tacletFilter;
    }

    
    public boolean suggestiveVarNames() {
	return suggestiveVarNames;
    }
    

    public boolean isDndDirectionSensitive() {        
        return dndDirectionSensitive;
    }
    
    
    public boolean oneStepSimplification() {
	return oneStepSimplification;
    }
    
    
    public boolean useJML() {
        return useJML && !disableSpecs;
    }
    
    
    public boolean useOCL() {
        return useOCL && !disableSpecs;
    }
    

    // setter
    public void setTacletFilter(boolean b) {
        if (tacletFilter != b) {
          tacletFilter = b;
          fireSettingsChanged();
        }
    }
    
    
    public void setDnDDirectionSensitivity(boolean b) {
        if (dndDirectionSensitive != b) {
          dndDirectionSensitive = b;
          fireSettingsChanged();
        }
    }
    
    
    public void setOneStepSimplification(boolean b) {
	if (oneStepSimplification != b) {
	    oneStepSimplification = b;
	    fireSettingsChanged();
	}
    }

    
    public void setUseJML(boolean b) {
        if (useJML != b) {
            useJML = b;
          fireSettingsChanged();
        }
    }
    
    
    public void setUseOCL(boolean b) {
        if (useOCL != b) {
            useOCL = b;
          fireSettingsChanged();
        }
    }


    
    /** gets a Properties object and has to perform the necessary
     * steps in order to change this object in a way that it
     * represents the stored settings
     */
    public void readSettings(Properties props) {
	String val = props.getProperty(TACLET_FILTER);
	if (val != null) {
	    tacletFilter = Boolean.valueOf(val).booleanValue();
	}
    
        val = props.getProperty(DND_DIRECTION_SENSITIVE_KEY);
        if (val != null) {
            dndDirectionSensitive = Boolean.valueOf(val).booleanValue();
        }         
        
        val = props.getProperty(ONE_STEP_SIMPLIFICATION_KEY);
        if (val != null) {
            oneStepSimplification = Boolean.valueOf(val).booleanValue();
        }
        
        val = props.getProperty(USE_JML_KEY);
        if (val != null) {
            useJML = Boolean.valueOf(val).booleanValue();
        }         
        
        val = props.getProperty(USE_OCL_KEY);
        if (val != null) {
            useOCL = Boolean.valueOf(val).booleanValue();
        }                 
    }


    /** implements the method required by the Settings interface. The
     * settings are written to the given Properties object. Only entries of the form 
     * <key> = <value> (,<value>)* are allowed.
     * @param props the Properties object where to write the settings as (key, value) pair
     */
    public void writeSettings(Properties props) {
	props.setProperty(TACLET_FILTER, "" + tacletFilter);
        props.setProperty(DND_DIRECTION_SENSITIVE_KEY, "" + dndDirectionSensitive);
        props.setProperty(ONE_STEP_SIMPLIFICATION_KEY, "" + oneStepSimplification);        
        props.setProperty(USE_JML_KEY, "" + useJML);
        props.setProperty(USE_OCL_KEY, "" + useOCL);
    }

    /** sends the message that the state of this setting has been
     * changed to its registered listeners (not thread-safe)
     */
    protected void fireSettingsChanged() {
        for (SettingsListener aListenerList : listenerList) {
            aListenerList.settingsChanged(new GUIEvent(this));
        }
    }

    
    /** 
     * adds a listener to the settings object 
     * @param l the listener
     */
    public void addSettingsListener(SettingsListener l) {
	listenerList.add(l);
    }
}