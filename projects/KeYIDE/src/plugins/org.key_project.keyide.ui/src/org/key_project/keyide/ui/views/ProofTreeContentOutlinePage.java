package org.key_project.keyide.ui.views;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.key_project.keyide.ui.providers.LazyProofTreeContentProvider;
import org.key_project.keyide.ui.providers.ProofTreeLabelProvider;

import de.uka.ilkd.key.proof.Proof;
import de.uka.ilkd.key.symbolic_execution.util.KeYEnvironment;
import de.uka.ilkd.key.ui.CustomConsoleUserInterface;

/**
 * A class to display the correct Outline for the current {@link Proof}
 * 
 * @author Christoph Schneider, Niklas Bunzel, Stefan K�sdorf, Marco Drebing
 */
public class ProofTreeContentOutlinePage extends ContentOutlinePage {
   private Proof proof;
   
   private KeYEnvironment<CustomConsoleUserInterface> environment;
   
   /**
    * Constructor.
    * @param proof The {@link Proof} for this Outline.
    */
   public ProofTreeContentOutlinePage(Proof proof, KeYEnvironment<CustomConsoleUserInterface> environment) {
      this.proof = proof;
      this.environment = environment;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected int getTreeStyle() {
      return super.getTreeStyle() | SWT.VIRTUAL;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void createControl(Composite parent) {
      super.createControl(parent);
      getTreeViewer().setUseHashlookup(true);
      getTreeViewer().setContentProvider(new LazyProofTreeContentProvider(getTreeViewer(), environment, proof));
      getTreeViewer().setLabelProvider(new ProofTreeLabelProvider(getTreeViewer(), environment, proof));
      getTreeViewer().setInput(proof);
      
      MenuManager menuManager = new MenuManager("Outline popup", "org.key_project.keyide.ui.view.outline.popup");
      Menu menu = menuManager.createContextMenu(getTreeViewer().getControl());
      getTreeViewer().getControl().setMenu(menu);
      getSite().registerContextMenu ("org.key_project.keyide.ui.view.outline.popup", menuManager, getTreeViewer());
   }
}