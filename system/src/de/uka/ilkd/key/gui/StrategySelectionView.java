// This file is part of KeY - Integrated Deductive Software Design
// Copyright (C) 2001-2011 Universitaet Karlsruhe, Germany
//                         Universitaet Koblenz-Landau, Germany
//                         Chalmers University of Technology, Sweden
//
// The KeY system is protected by the GNU General Public License. 
// See LICENSE.TXT for details.
//
//


package de.uka.ilkd.key.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uka.ilkd.key.gui.configuration.ProofSettings;
import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.strategy.Strategy;
import de.uka.ilkd.key.strategy.StrategyFactory;
import de.uka.ilkd.key.strategy.StrategyProperties;


public final class StrategySelectionView extends JPanel {
    
    private static final String JAVACARDDL_STRATEGY_NAME 
    	= "JavaCardDLStrategy";

    final JRadioButtonHashMap bSimpleJavaCardDLStrategy = new JRadioButtonHashMap(
        "Java DL", JAVACARDDL_STRATEGY_NAME, false, false);
   
    ButtonGroup stratGroup = new ButtonGroup();
    ButtonGroup splittingGroup = new ButtonGroup();
    ButtonGroup loopGroup = new ButtonGroup();
    ButtonGroup methodGroup = new ButtonGroup();
    ButtonGroup depGroup = new ButtonGroup();
    ButtonGroup nonLinArithGroup = new ButtonGroup();
    ButtonGroup quantifierGroup = new ButtonGroup();
    ButtonGroup stopModeGroup = new ButtonGroup();    
    ButtonGroup[] userTacletsGroup = new ButtonGroup[StrategyProperties.USER_TACLETS_NUM];
    {
        for (int i = 0; i < StrategyProperties.USER_TACLETS_NUM; ++i)
            userTacletsGroup[i] = new ButtonGroup ();
    }
    JRadioButtonHashMap rdBut9;
    JRadioButtonHashMap rdBut10;
    JRadioButtonHashMap rdBut11;
    JRadioButtonHashMap rdBut12;
    JRadioButtonHashMap rdBut13;
    JRadioButtonHashMap rdBut14;
    JRadioButtonHashMap rdBut17;
    JRadioButtonHashMap rdBut18;
    private JRadioButtonHashMap splittingNormal;
    private JRadioButtonHashMap splittingOff;
    private JRadioButtonHashMap splittingDelayed;
    private JRadioButtonHashMap depOn;
    private JRadioButtonHashMap depOff;
    private JRadioButtonHashMap nonLinArithNone;
    private JRadioButtonHashMap nonLinArithDefOps;
    private JRadioButtonHashMap nonLinArithCompletion;
    private JRadioButtonHashMap quantifierNone;
    private JRadioButtonHashMap quantifierNonSplitting;
    private JRadioButtonHashMap quantifierNonSplittingWithProgs;
    private JRadioButtonHashMap quantifierInstantiate;
    
    private KeYMediator mediator;
    
//    private TimeoutSpinner timeoutSpinner;
    private JButton defaultButton;
    
    
    JPanel javaDLOptionsPanel = new JPanel() {
        
        public void setEnabled(boolean enabled) {
             super.setEnabled(enabled);
             setChildrenEnabled(this, enabled);
        }

        private void setChildrenEnabled(Container container, boolean enabled) {
             for (int i=0; i<container.getComponentCount(); i++) {
                 Component comp = container.getComponent(i);
                 comp.setEnabled(enabled);
                 if (comp instanceof Container)
                     setChildrenEnabled((Container) comp, enabled);
             }
        }
    };
    
    JScrollPane javaDLOptionsScrollPane =
        new JScrollPane ( javaDLOptionsPanel,
                          ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
    
    
    Border loweredetched = 
//        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        BorderFactory.createEmptyBorder();
  
    
    
    public StrategySelectionView () {        
        layoutPane();       
        refresh(mediator == null ? null : mediator.getSelectedProof());
	setVisible( true );
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                maxSlider.refresh();
//                timeoutSpinner.refresh();
            }
        });
    }
    
    /** Build everything */    
    private void layoutPane() {        
        final JavaDLOptionListener javaDLOptionListener = new JavaDLOptionListener();
        bSimpleJavaCardDLStrategy.addPropertyChangeListener("enabled", 
                javaDLOptionListener);
        bSimpleJavaCardDLStrategy.addItemListener(javaDLOptionListener);        
        javaDLOptionsPanel.setEnabled(false);
        
        StratListener stratListener = new StratListener();
        OptListener optListener = new OptListener();
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        javaDLOptionsScrollPane
            .setBorder(BorderFactory.createTitledBorder("Java DL Options") );
        GridBagConstraints gbcpanel5 = new GridBagConstraints();
        final GridBagLayout javaDLOptionsLayout = new GridBagLayout ();
        javaDLOptionsPanel.setLayout(javaDLOptionsLayout);
                
        javaDLOptionsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        
//        javaDLOptionsPanel.setBorder(BorderFactory.createEmptyBorder());
        
        ////////////////////////////////////////////////////////////////////////

        int yCoord = 0;

        ////////////////////////////////////////////////////////////////////////
        ++yCoord;
        
        addJavaDLOption ( new JLabel ( "Stop at" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
        ++yCoord;

        rdBut17 = new JRadioButtonHashMap("Default", StrategyProperties.STOPMODE_DEFAULT, true, false);
	rdBut17.setToolTipText( "<html>Stop when (i) the maximum number of rule<br>" +
                                "applications is reached or (ii) no more rules are<br>"+
				"applicable on the proof tree.</html>");
        stopModeGroup.add(rdBut17);
        addJavaDLOption ( rdBut17, javaDLOptionsLayout, 2, yCoord, 2 );        

        rdBut18 = new JRadioButtonHashMap(
                "Unclosable", StrategyProperties.STOPMODE_NONCLOSE, false, false);
	rdBut18.setToolTipText( "<html>Stop as soon as the first not automatically<br>" +
                                "closable goal is encountered.</html>");
        stopModeGroup.add(rdBut18);
        addJavaDLOption ( rdBut18, javaDLOptionsLayout, 4, yCoord, 2 );        
       
        
        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );
        ////////////////////////////////////////////////////////////////////////

        
        addJavaDLOption ( new JLabel ( "Logical splitting" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
//        JPanel splittingPanel = new JPanel();
//        splittingPanel.setBorder(BorderFactory.createTitledBorder(
//            loweredetched, "Logical Splitting"));
//        javaDLOptionsPanel.add(splittingPanel);

        ++yCoord;
        
        splittingNormal = new JRadioButtonHashMap("Free",
                       StrategyProperties.SPLITTING_NORMAL, true, false);
        splittingGroup.add(splittingNormal);
        addJavaDLOption ( splittingNormal, javaDLOptionsLayout, 4, yCoord, 2 );     
        splittingNormal.setToolTipText("<html>" +
                                        "Split formulas (if-then-else expressions,<br>" +
                                        "disjunctions in the antecedent, conjunctions in<br>" +
                                        "the succedent) freely without restrictions." +
                                        "</html>");
        

        splittingDelayed = new JRadioButtonHashMap("Delayed",
                     StrategyProperties.SPLITTING_DELAYED, true, false);
        splittingDelayed.setToolTipText("<html>" +
                                        "Do not split formulas (if-then-else expressions,<br>" +
                                        "disjunctions in the antecedent, conjunctions in<br>" +
                                        "the succedent) as long as programs are present in<br>" +
                                        "the sequent.<br>" +
                                        "NB: This does not affect the splitting of formulas<br>" +
                                        "that themselves contain programs.<br>" +
                                        "NB2: Delaying splits often prevents KeY from finding<br>" +
                                        "short proofs, but in some cases it can significantly<br>" +
                                        "improve the performance." +
                                        "</html>");
        splittingGroup.add(splittingDelayed);
        addJavaDLOption ( splittingDelayed, javaDLOptionsLayout, 2, yCoord, 2 );

        splittingOff = new JRadioButtonHashMap("Off",
                     StrategyProperties.SPLITTING_OFF, true, false);
        splittingOff.setToolTipText("<html>" +
                                    "Do never split formulas (if-then-else expressions,<br>" +
                                    "disjunctions in the antecedent, conjunctions in<br>" +
                                    "the succedent).<br>" +
                                    "NB: This does not affect the splitting of formulas<br>" +
                                    "that contain programs.<br>" +
                                    "NB2: Without splitting, KeY is often unable to find<br>" +
                                    "proofs even for simple problems. This option can,<br>" +
                                    "nevertheless, be meaningful to keep the complexity<br>" +
                                    "of proofs small and support interactive proving." +
                                    "</html>");
        splittingGroup.add(splittingOff);
        addJavaDLOption ( splittingOff, javaDLOptionsLayout, 6, yCoord, 2 );

        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );
        
        ////////////////////////////////////////////////////////////////////////

        ++yCoord;
        
        addJavaDLOption ( new JLabel ( "Loop treatment" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
        ++yCoord;
        
        rdBut9 = new JRadioButtonHashMap("Expand", StrategyProperties.LOOP_EXPAND, true, false);
        loopGroup.add(rdBut9);
        addJavaDLOption ( rdBut9, javaDLOptionsLayout, 4, yCoord, 2 );        

        rdBut10 = new JRadioButtonHashMap("Invariant", StrategyProperties.LOOP_INVARIANT, false, false);
        loopGroup.add(rdBut10);
        addJavaDLOption ( rdBut10, javaDLOptionsLayout, 2, yCoord, 2 );        

        rdBut11 = new JRadioButtonHashMap("None", StrategyProperties.LOOP_NONE, false, false);
        loopGroup.add(rdBut11);
        addJavaDLOption ( rdBut11, javaDLOptionsLayout, 6, yCoord, 2 );        

        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );

        ////////////////////////////////////////////////////////////////////////

        ++yCoord;
        
        addJavaDLOption ( new JLabel ( "Method treatment" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
        ++yCoord;

        rdBut12 = new JRadioButtonHashMap("Expand", StrategyProperties.METHOD_EXPAND, true, false);
        methodGroup.add(rdBut12);
        addJavaDLOption ( rdBut12, javaDLOptionsLayout, 4, yCoord, 2 );        

        rdBut13 = new JRadioButtonHashMap(
                "Contract", StrategyProperties.METHOD_CONTRACT, false, false);
        methodGroup.add(rdBut13);
        addJavaDLOption ( rdBut13, javaDLOptionsLayout, 2, yCoord, 2 );        

        rdBut14 = new JRadioButtonHashMap("None",
                StrategyProperties.METHOD_NONE, false, false);
        methodGroup.add(rdBut14);
        addJavaDLOption ( rdBut14, javaDLOptionsLayout, 6, yCoord, 2 );        
        
        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );

        ////////////////////////////////////////////////////////////////////////

        ++yCoord;
        
        addJavaDLOption ( new JLabel ( "Dependency contracts" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
        ++yCoord;

        depOn = new JRadioButtonHashMap("On", 
                StrategyProperties.DEP_ON, false, false);
        depGroup.add(depOn);
        addJavaDLOption ( depOn, javaDLOptionsLayout, 2, yCoord, 2 );        
        
        depOff = new JRadioButtonHashMap("Off", 
                StrategyProperties.DEP_OFF, false, false);
//        queryProgramsToRight.setToolTipText ( "<html>Move all program blocks to the" +
//                                              " succedent.<br>" +
//                                              " This is necessary" +
//                                              " when query invocations<br>" +
//                                              " are" +
//                                              " supposed to be eliminated" +
//                                              " using<br>" +
//                                              " method contracts.</html>" );
        depGroup.add(depOff);
        addJavaDLOption ( depOff, javaDLOptionsLayout, 4, yCoord, 2 );

        
        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );

  
        ++yCoord;
        
        addJavaDLOption ( new JLabel ( "Arithmetic treatment" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
        ++yCoord;

        nonLinArithNone = new JRadioButtonHashMap("Basic", 
                StrategyProperties.NON_LIN_ARITH_NONE, true, false);
        nonLinArithNone.setToolTipText("<html>" +
                "Basic arithmetic support:" +
                "<ul>" +
                "<li>Simplification of polynomial expressions</li>" +
                "<li>Computation of Gr&ouml;bner Bases for polynomials in the antecedent</li>" +
                "<li>(Partial) Omega procedure for handling linear inequations</li>" +
                "</ul>" +
                "</html>");
        nonLinArithGroup.add(nonLinArithNone);
        addJavaDLOption ( nonLinArithNone, javaDLOptionsLayout, 2, yCoord, 2 );
                                          
        nonLinArithDefOps = new JRadioButtonHashMap("DefOps", 
                StrategyProperties.NON_LIN_ARITH_DEF_OPS, false, false);
        nonLinArithDefOps.setToolTipText ( "<html>" +
                "Automatically expand defined symbols like:" +
                "<ul>" +
                "<li><tt>/</tt>, <tt>%</tt>, <tt>jdiv</tt>, <tt>jmod</tt>, ...</li>" +
                "<li><tt>int_RANGE</tt>, <tt>short_MIN</tt>, ...</li>" +
                "<li><tt>inInt</tt>, <tt>inByte</tt>, ...</li>" +
                "<li><tt>addJint</tt>, <tt>mulJshort</tt>, ...</li>" +
                "</ul>" +
                "</html>" );
        nonLinArithGroup.add(nonLinArithDefOps);
        addJavaDLOption ( nonLinArithDefOps, javaDLOptionsLayout, 4, yCoord, 2 );

        nonLinArithCompletion = new JRadioButtonHashMap("Model Search", 
                StrategyProperties.NON_LIN_ARITH_COMPLETION, false, false);
        nonLinArithCompletion.setToolTipText ( "<html>" +
                "Support for non-linear inequations and model search.<br>" +
                "In addition, this performs:" +
                "<ul>" +
                "<li>Multiplication of inequations with each other</li>" +
                "<li>Systematic case distinctions (cuts)</li>" +
                "</ul>" +
                "This method is guaranteed to find counterexamples for<br>" +
                "invalid goals that only contain polynomial (in)equations.<br>" +
                "Such counterexamples turn up as trivially unprovable goals.<br>" +
                "It is also able to prove many more valid goals involving<br>" +
                "(in)equations, but will in general not terminate on such goals." +
                "</html>" );
        nonLinArithGroup.add(nonLinArithCompletion);
        addJavaDLOption ( nonLinArithCompletion, javaDLOptionsLayout, 6, yCoord, 2 );
        
        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );

        ////////////////////////////////////////////////////////////////////////

        ++yCoord;
        
        addJavaDLOption ( new JLabel ( "Quantifier treatment" ),
                    javaDLOptionsLayout, 1, yCoord, 7 );
        
        ++yCoord;
        
        quantifierNone = new JRadioButtonHashMap("None", 
                         StrategyProperties.QUANTIFIERS_NONE, true, false);
        quantifierNone.setToolTipText ( "<html>" +
            "Do not instantiate quantified formulas automatically" +
            "</html>" );
        quantifierGroup.add(quantifierNone);
        addJavaDLOption ( quantifierNone, javaDLOptionsLayout, 2, yCoord, 4 );

        quantifierNonSplitting = new JRadioButtonHashMap("No Splits", 
                             StrategyProperties.QUANTIFIERS_NON_SPLITTING, true, false);
        quantifierNonSplitting.setToolTipText ( "<html>" +
            "Instantiate quantified formulas automatically<br>" +
            "with terms that occur in a sequent, but only if<br>" +
            "this does not cause proof splitting. Further, quantified<br>" +
            "formulas that contain queries are not instantiated<br>" +
            "automatically." +
            "</html>" );
        quantifierGroup.add(quantifierNonSplitting);
        addJavaDLOption ( quantifierNonSplitting, javaDLOptionsLayout, 6, yCoord, 2 );

        ++yCoord;

        quantifierNonSplittingWithProgs
                 = new JRadioButtonHashMap("No Splits with Progs", 
            StrategyProperties.QUANTIFIERS_NON_SPLITTING_WITH_PROGS, true, false);
        quantifierNonSplittingWithProgs.setToolTipText ( "<html>" +
            "Instantiate quantified formulas automatically<br>" +
            "with terms that occur in a sequent, but if the<br>" +
            "sequent contains programs then only perform<br>" +
            "instantiations that do not cause proof splitting.<br>" +
            "Further, quantified formulas that contain queries<br>" +
            "are not instantiated automatically." +
            "</html>" );
        quantifierGroup.add(quantifierNonSplittingWithProgs);
        addJavaDLOption ( quantifierNonSplittingWithProgs, javaDLOptionsLayout, 2, yCoord, 4 );

        quantifierInstantiate = new JRadioButtonHashMap("Free", 
                              StrategyProperties.QUANTIFIERS_INSTANTIATE, true, false);
        quantifierInstantiate.setToolTipText ( "<html>" +
            "Instantiate quantified formulas automatically<br>" +
            "with terms that occur in a sequent, also if this<br>" +
            "might cause proof splitting." +
            "</html>" );
        quantifierGroup.add(quantifierInstantiate);
        addJavaDLOption ( quantifierInstantiate, javaDLOptionsLayout, 6, yCoord, 2 );

        ++yCoord;
        addJavaDLOptionSpace ( javaDLOptionsLayout, yCoord );

        ////////////////////////////////////////////////////////////////////////

        ++yCoord;

        final JLabel userTacletsLabel = new JLabel ( "User-specific taclets" );
        addJavaDLOption ( userTacletsLabel, javaDLOptionsLayout, 1, yCoord, 7 );
        userTacletsLabel.setToolTipText("<html>" +
                                        "These options define whether user- and problem-specific taclets<br>" +
                                        "are applied automatically by the strategy. Problem-specific taclets<br>" +
                                        "can be defined in the \\rules-section of a .key-problem file. For<br>" +
                                        "automatic application, the taclets have to contain a clause<br>" +
                                        "\\heuristics(userTaclets1), \\heuristics(userTaclets2), etc." +
                                        "</html>");
        
        for (int i = 1; i <= StrategyProperties.USER_TACLETS_NUM; ++i) {
            ++yCoord;
            addUserTacletsOptions ( javaDLOptionsLayout, optListener,
                                    userTacletsGroup[i-1], yCoord, i );
        }

        fixVerticalSpace ( javaDLOptionsScrollPane );

        ////////////////////////////////////////////////////////////////////////

        Box box = Box.createVerticalBox();
        stratGroup.add(bSimpleJavaCardDLStrategy);
        javaDLOptionsPanel.setEnabled(bSimpleJavaCardDLStrategy.isSelected());
        bSimpleJavaCardDLStrategy.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                javaDLOptionsPanel.setEnabled(bSimpleJavaCardDLStrategy.isSelected());
            }
        });
        
        JButton go = new JButton(Main.autoModeAction);

        JPanel timeout = createTimeoutSpinner();

        JPanel goPanel = new JPanel ();
        GridBagLayout goLayout = new GridBagLayout();
        goPanel.setLayout(goLayout);
        goPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        gbcpanel5.gridx = 1;
        gbcpanel5.gridy = 0;
        gbcpanel5.gridwidth = 1;
        gbcpanel5.gridheight = 1;
        gbcpanel5.fill = GridBagConstraints.NONE;
        gbcpanel5.weightx = 1;
        gbcpanel5.weighty = 0;
        gbcpanel5.anchor = GridBagConstraints.WEST;
        gbcpanel5.insets = new Insets (4, 4, 4, 4);
        goLayout.setConstraints(go, gbcpanel5);
        goPanel.add(go);

        gbcpanel5.gridx = 1;
        gbcpanel5.gridy = 1;
        gbcpanel5.gridwidth = 1;
        gbcpanel5.gridheight = 1;
        gbcpanel5.fill = GridBagConstraints.NONE;
        gbcpanel5.weightx = 1;
        gbcpanel5.weighty = 0;
        gbcpanel5.anchor = GridBagConstraints.WEST;
        gbcpanel5.insets = new Insets (0, 0, 0, 0);
        
        gbcpanel5.gridx = 2;
        gbcpanel5.gridy = 0;
        gbcpanel5.gridwidth = 1;
        gbcpanel5.gridheight = 1;
        gbcpanel5.fill = GridBagConstraints.NONE;
        gbcpanel5.weightx = 0;
        gbcpanel5.weighty = 0;
        gbcpanel5.anchor = GridBagConstraints.CENTER;
        gbcpanel5.insets = new Insets (0, 0, 0, 0);
        goLayout.setConstraints(timeout, gbcpanel5);
        goPanel.add(timeout);
        
        fixVerticalSpace ( goPanel );

        

        box.add(Box.createVerticalStrut(4));
        box.add(goPanel);       
        box.add(Box.createVerticalStrut(8));
//        box.add(numLabel);
//        box.add(Box.createVerticalStrut(4));
        maxSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(maxSlider);
        box.add(Box.createVerticalStrut(8));
        bSimpleJavaCardDLStrategy.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(bSimpleJavaCardDLStrategy);
        javaDLOptionsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(javaDLOptionsScrollPane);       
	
       
        // HACK but I really do not know enough about the GridBagLayout :-(
//        final JPanel verticalGlue = new JPanel();
//        verticalGlue.setPreferredSize(new Dimension(1, 1024));
//        box.add(verticalGlue);
        box.add(Box.createVerticalGlue());

        this.add(box);
        //  add the ActionListener to the Buttons and ActionCommands 
        //  for identifying Buttons
        bSimpleJavaCardDLStrategy.addActionListener(stratListener);
        rdBut9.addActionListener(optListener);	
        rdBut10.addActionListener(optListener);       
        rdBut11.addActionListener(optListener);    
        rdBut12.addActionListener(optListener);       
        rdBut13.addActionListener(optListener);     
        rdBut14.addActionListener(optListener);
        rdBut17.addActionListener(optListener);
        rdBut18.addActionListener(optListener);
        depOn.addActionListener(optListener);
        depOff.addActionListener(optListener);
        splittingNormal.addActionListener(optListener);
        splittingDelayed.addActionListener(optListener);
        splittingOff.addActionListener(optListener);
        nonLinArithNone.addActionListener(optListener);
        nonLinArithDefOps.addActionListener(optListener);
        nonLinArithCompletion.addActionListener(optListener);
        quantifierNone.addActionListener(optListener);
        quantifierNonSplitting.addActionListener(optListener);
        quantifierNonSplittingWithProgs.addActionListener(optListener);
        quantifierInstantiate.addActionListener(optListener);
    }

    private void addUserTacletsOptions(GridBagLayout javaDLOptionsLayout,
                                       OptListener optListener,
                                       ButtonGroup group,
                                       int yCoord, int num) {
        addJavaDLOption ( new JLabel ( "" + num + ":  " ),
                          javaDLOptionsLayout, 2, yCoord, 1 );
        
        final JRadioButtonHashMap userTacletsOff = new JRadioButtonHashMap("Off", 
                        StrategyProperties.USER_TACLETS_OFF + num, true, false);
        userTacletsOff.setToolTipText("Taclets of the rule set \"userTaclets" + num 
                                      + "\" are not applied automatically");
        group.add(userTacletsOff);
        userTacletsOff.addActionListener(optListener);
        addJavaDLOption ( userTacletsOff, javaDLOptionsLayout, 3, yCoord, 1 );
        
        final JRadioButtonHashMap userTacletsLow = new JRadioButtonHashMap("Low prior.", 
                        StrategyProperties.USER_TACLETS_LOW + num, true, false);
        userTacletsLow.setToolTipText("Taclets of the rule set \"userTaclets" + num 
                                      + "\" are applied automatically with low priority");
        group.add(userTacletsLow);
        userTacletsLow.addActionListener(optListener);
        addJavaDLOption ( userTacletsLow, javaDLOptionsLayout, 4, yCoord, 2 );
        
        final JRadioButtonHashMap userTacletsHigh = new JRadioButtonHashMap("High prior.", 
                         StrategyProperties.USER_TACLETS_HIGH + num, true, false);
        userTacletsHigh.setToolTipText("Taclets of the rule set \"userTaclets" + num 
                                       + "\" are applied automatically with high priority");
        group.add(userTacletsHigh);
        userTacletsHigh.addActionListener(optListener);
        addJavaDLOption ( userTacletsHigh, javaDLOptionsLayout, 6, yCoord, 2 );
    }

    private void addJavaDLOptionSpace(GridBagLayout javaDLOptionsLayout, int yCoord) {
        final GridBagConstraints con = new GridBagConstraints ();
        con.gridx = 0;
        con.gridy = yCoord;
        con.gridwidth = 9;
        con.gridheight = 1;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.weightx = 1;
        con.weighty = 0;
        con.anchor = GridBagConstraints.CENTER;
        final Component sep = new JLabel ();
        javaDLOptionsLayout.setConstraints ( sep, con );
        javaDLOptionsPanel.add ( sep );
        addJavaDLOption ( Box.createRigidArea ( new Dimension ( 4, 4 ) ),
                          javaDLOptionsLayout, 0, yCoord, 1 );
        addJavaDLOption ( Box.createRigidArea ( new Dimension ( 4, 4 ) ),
                          javaDLOptionsLayout, 1, yCoord, 1 );
    }

    private void addJavaDLOption(Component widget,
                                 GridBagLayout javaDLOptionsLayout, int gridx,
                                 int gridy, int width) {
        final GridBagConstraints con = new GridBagConstraints ();
        con.gridx = gridx;
        con.gridy = gridy;
        con.gridwidth = width;
        con.gridheight = 1;
        con.fill = GridBagConstraints.NONE;
        con.weightx = 0;
        con.weighty = 0;
        con.anchor = GridBagConstraints.WEST;
        javaDLOptionsLayout.setConstraints ( widget, con );
        javaDLOptionsPanel.add ( widget );
    }

    private void fixVerticalSpace(JComponent comp) {
        comp.setMaximumSize
        ( new Dimension ( Integer.MAX_VALUE,
                          comp.getPreferredSize ().height ) );
    }
    
    private JPanel createTimeoutSpinner() {
//        final JPanel timeoutPanel = new JPanel();
//        timeoutPanel.setLayout(new FlowLayout());
//        final JLabel timeoutLabel = new JLabel("Time limit (ms)"); 
//        timeoutPanel.add(timeoutLabel);
//        timeoutPanel.setToolTipText("Interrupt automatic rule application " +
//                        "after the specified period of time (-1 disables timeout).");
//        
//        timeoutSpinner = new TimeoutSpinner();       
//        
//        timeoutSpinner.addChangeListener(new ChangeListener() {
//
//            public void stateChanged(ChangeEvent e) {
//                if (e.getSource() == timeoutSpinner) {
//                    long timeout = ((Long)((JSpinner)e.getSource()).getValue()).longValue();
//                    mediator.setAutomaticApplicationTimeout(timeout);
//                }
//            }            
//        });
//        timeoutPanel.add(timeoutSpinner);
//        return timeoutPanel;
	final JPanel panel = new JPanel();
	defaultButton = new JButton("Defaults");
	defaultButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		mediator.getProof()
		        .getSettings()
		        .getStrategySettings()
		        .setMaxSteps(10000);
		updateStrategySettings(JAVACARDDL_STRATEGY_NAME,
			               new StrategyProperties());
		refresh(mediator.getSelectedProof());
	    }
	});
	panel.add(defaultButton);
	
        maxSlider.addChangeListener( new ChangeListener() {
            public void stateChanged ( ChangeEvent e ) {
        	refreshDefaultButton(); 
            }
        });
	
	return panel;
    }
    
    public void setMediator(KeYMediator mediator) {
        this.mediator = mediator;
        maxSlider.setMediator(mediator);
        
        mediator.addKeYSelectionListener(new SelectionChangedListener());
                
        final StrategyFactory defaultStrategyFactory = 
            mediator.getProfile().getDefaultStrategyFactory();
        
        getStrategyButton(defaultStrategyFactory.name().toString()).
            setSelected(true);
    }


    private final MaxRuleAppSlider maxSlider = new MaxRuleAppSlider(null);
    
    
    private final class JavaDLOptionListener implements PropertyChangeListener, 
    ItemListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Boolean) {
                javaDLOptionsPanel.setEnabled
                (bSimpleJavaCardDLStrategy.isSelected() && 
                        ((Boolean)evt.getNewValue()).booleanValue());
            } else {
                javaDLOptionsPanel.
                    setEnabled(bSimpleJavaCardDLStrategy.isSelected() && 
                            Boolean.getBoolean(""+evt.getNewValue()));
            }
        }
        public void itemStateChanged(ItemEvent e) {
            if (e.getItem() == bSimpleJavaCardDLStrategy) {
                javaDLOptionsPanel.setEnabled(bSimpleJavaCardDLStrategy.isSelected() &&
                        bSimpleJavaCardDLStrategy.isEnabled()); 
            }
        }
    }

//    class TimeoutSpinner extends JSpinner {        
//        public TimeoutSpinner() {
//            super(new SpinnerNumberModel
//                    (Long.valueOf(-1), Long.valueOf(-1), Long.valueOf(Long.MAX_VALUE), Long.valueOf(1))); 
//            if (this.getEditor() instanceof JSpinner.DefaultEditor) {
//                JFormattedTextField ftf = ((JSpinner.DefaultEditor)this.getEditor()).getTextField();
//                if (ftf != null) {
//                    ftf.setColumns(6); 
//                    ftf.setHorizontalAlignment(SwingConstants.RIGHT);
//                }
//            }            
//        }
//        
//        public void refresh() {
//            setValue(Long.valueOf(mediator.getAutomaticApplicationTimeout()));
//        }    
//    }

    /**
     * TODO: here should be a settings listener listening to strategy setting changes
     * Therefore we have to wait until ProofSettings have been refactored (see KeY-Wiki) 
     * 
     *(Currently changes made in the old slider are not reported to this view)
     */
    private final class SelectionChangedListener 
        implements KeYSelectionListener {
        
        public void selectedNodeChanged(KeYSelectionEvent e) {}

        public void selectedProofChanged(KeYSelectionEvent e) {
            refresh(e.getSource().getSelectedProof());            
        }        
    }

    
    /** performs a refresh of all elements in this tab */
    public void refresh(Proof proof) {
        if (proof == null) {            
            enableAll(false);                
        } else {
//            boolean methodExpandAllowed = false;
//            for(de.uka.ilkd.key.logic.Choice c 
//        	     : proof.env().getInitConfig().getActivatedChoices()) {
//        	if(c.name().toString().equals("methodExpand:allow")) {
//        	    methodExpandAllowed = true;
//        	    break;
//        	}
//            }
//            rdBut12.setEnabled(methodExpandAllowed);                        
//            if(!methodExpandAllowed && rdBut12.isSelected()) {
//        	rdBut13.doClick();
//            }
            
            String activeS = proof.getActiveStrategy().name().toString();
            JRadioButton bactive = JRadioButtonHashMap.getButton(activeS);
            bactive.setSelected(true);
            
            StrategyProperties p = proof.
                getSettings().getStrategySettings().
                    getActiveStrategyProperties();
            String activeSplittingOptions = p.getProperty(StrategyProperties.SPLITTING_OPTIONS_KEY);
            JRadioButton bSplittingActive = getStrategyOptionButton(activeSplittingOptions,
		    StrategyProperties.SPLITTING_OPTIONS_KEY);
            bSplittingActive.setSelected(true);
            String activeLoopOptions = p.getProperty(StrategyProperties.LOOP_OPTIONS_KEY);
            JRadioButton bLoopActive = getStrategyOptionButton(activeLoopOptions, 
                    StrategyProperties.LOOP_OPTIONS_KEY);
            bLoopActive.setSelected(true);
            String activeMethodOptions = p.getProperty(StrategyProperties.METHOD_OPTIONS_KEY);
            JRadioButton bMethodActive = getStrategyOptionButton(activeMethodOptions, 
                    StrategyProperties.METHOD_OPTIONS_KEY);
            bMethodActive.setSelected(true);   
            String activeDepOptions = p.getProperty(StrategyProperties.DEP_OPTIONS_KEY);
            JRadioButton bDepActive = getStrategyOptionButton(activeDepOptions, 
                    StrategyProperties.DEP_OPTIONS_KEY);
            bDepActive.setSelected(true);   
            String activeNonLinArithOptions = p.getProperty(StrategyProperties.NON_LIN_ARITH_OPTIONS_KEY);
            JRadioButton bNonLinArithActive = getStrategyOptionButton(activeNonLinArithOptions, 
                    StrategyProperties.NON_LIN_ARITH_OPTIONS_KEY);
            bNonLinArithActive.setSelected(true);   
            String quantifierOptions = p.getProperty(StrategyProperties.QUANTIFIERS_OPTIONS_KEY);
            JRadioButton bQuantifierActive = getStrategyOptionButton(quantifierOptions, 
                    StrategyProperties.QUANTIFIERS_OPTIONS_KEY);
            bQuantifierActive.setSelected(true);   
            String stopmodeOptions = p.getProperty(StrategyProperties.STOPMODE_OPTIONS_KEY);
            JRadioButton bStopModeActive = getStrategyOptionButton(stopmodeOptions, 
                    StrategyProperties.STOPMODE_OPTIONS_KEY);
            bStopModeActive.setSelected(true);        
         
            for (int i = 1; i <= StrategyProperties.USER_TACLETS_NUM; ++i) {
                String userTacletsOptions =
                    p.getProperty(StrategyProperties.USER_TACLETS_OPTIONS_KEY(i));
                JRadioButton userTacletsActive = getStrategyOptionButton(userTacletsOptions + i,
                    StrategyProperties.USER_TACLETS_OFF);
                userTacletsActive.setSelected(true);
            }
            
            maxSlider.refresh();
//            timeoutSpinner.refresh();
            enableAll(true);
            refreshDefaultButton();
        }
    }
    
    
    private void refreshDefaultButton() {
	defaultButton.setEnabled(mediator.getSelectedProof() != null
				 && (maxSlider.getPos() != 10000
		                    || !mediator.getSelectedProof()
		                                .getActiveStrategy()
		                 	        .name()
		                 	        .toString()
		                 	        .equals(JAVACARDDL_STRATEGY_NAME)
		                    || !getProperties().isDefault()));	
    }
    

    private JRadioButton getStrategyOptionButton(String activeOptions, String category) {
        JRadioButton bActive = JRadioButtonHashMap.getButton(
                activeOptions);
        if (bActive == null) {
            System.err.println("Unknown option '" + activeOptions + "' falling back to " + 
                    StrategyProperties.getDefaultProperty(category));
            bActive = JRadioButtonHashMap.
              getButton(StrategyProperties.getDefaultProperty(category));
        }
        return bActive;
    }

    private JRadioButton getStrategyButton(String name) {
        return JRadioButtonHashMap.getButton(name);      
    }
    
    /**
     * enables or disables all components
     * @param enable boolean saying whether to activate or
     * deactivate the components
     */
    private void enableAll(boolean enable) {             
        maxSlider.setEnabled(enable);     
//        timeoutSpinner.setEnabled(enable);
        defaultButton.setEnabled(enable);
        if (mediator != null) {                   
            final Iterator<StrategyFactory> supportedStrategies = 
               mediator.getProfile().supportedStrategies().iterator();
            while (supportedStrategies.hasNext()) {                  
                final StrategyFactory next = supportedStrategies.next();              
                getStrategyButton(next.name().toString()).setEnabled(enable);               
            }
        }
    }

    public Strategy getStrategy(String strategyName, 
	    			Proof proof,
	    			StrategyProperties properties) {
        if (mediator != null) {        
            final Iterator<StrategyFactory> supportedStrategies = 
               mediator.getProfile().supportedStrategies().iterator();
            while (supportedStrategies.hasNext()) {                
                final StrategyFactory s = supportedStrategies.next();
                if (strategyName.equals(s.name().toString())) {                    
                    return s.create(proof, properties);
                }
            }
            System.err.println("Selected Strategy '" + strategyName + "' not found falling back to "+ 
                mediator.getProfile().getDefaultStrategyFactory().name());
        }
        return mediator.getProfile().getDefaultStrategyFactory().create(proof, properties);
    }
  
    private String removeLast(String str, int num) {
        return str.substring ( 0, str.length () - num );
    }
    
    
    /**
     * @return the StrategyProperties
     */
    private StrategyProperties getProperties() {
        StrategyProperties p = new StrategyProperties();        
        p.setProperty( StrategyProperties.SPLITTING_OPTIONS_KEY, 
                       splittingGroup.getSelection().getActionCommand());
        p.setProperty( StrategyProperties.LOOP_OPTIONS_KEY, 
                       loopGroup.getSelection().getActionCommand());
        p.setProperty( StrategyProperties.METHOD_OPTIONS_KEY, 
                       methodGroup.getSelection().getActionCommand());
        p.setProperty( StrategyProperties.DEP_OPTIONS_KEY, 
                       depGroup.getSelection().getActionCommand());
        p.setProperty( StrategyProperties.NON_LIN_ARITH_OPTIONS_KEY, 
                       nonLinArithGroup.getSelection().getActionCommand());
        p.setProperty( StrategyProperties.QUANTIFIERS_OPTIONS_KEY, 
                       quantifierGroup.getSelection().getActionCommand());
        p.setProperty( StrategyProperties.STOPMODE_OPTIONS_KEY, 
                       stopModeGroup.getSelection().getActionCommand());
        
        for (int i = 1; i <= StrategyProperties.USER_TACLETS_NUM; ++i) {
            p.setProperty( StrategyProperties.USER_TACLETS_OPTIONS_KEY(i), 
                           removeLast(userTacletsGroup[i-1].getSelection()
                                      .getActionCommand(), 1));
        }
        
        return p;
    }

    
    private void updateStrategySettings(String strategyName,
	    				StrategyProperties p) {
        final Proof proof = mediator.getProof();
        final Strategy strategy = getStrategy(strategyName, proof, p);

        ProofSettings.DEFAULT_SETTINGS.getStrategySettings().
        setStrategy(strategy.name());
        ProofSettings.DEFAULT_SETTINGS.getStrategySettings().
            setActiveStrategyProperties(p);
        
        proof.getSettings().getStrategySettings().
        setStrategy(strategy.name());
        proof.getSettings().getStrategySettings().
            setActiveStrategyProperties(p);
        
        proof.setActiveStrategy(strategy);
        
        refresh(proof);
    }

    public class StratListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) {
            StrategyProperties props = getProperties();
            updateStrategySettings(e.getActionCommand(), props);
        }
    }

    public class OptListener implements ActionListener { 
        public void actionPerformed(ActionEvent e) { 	
            StrategyProperties props = getProperties();        	
            updateStrategySettings(
        	    mediator.getProof().getActiveStrategy().name().toString(),
                    props);
        }
    }
}