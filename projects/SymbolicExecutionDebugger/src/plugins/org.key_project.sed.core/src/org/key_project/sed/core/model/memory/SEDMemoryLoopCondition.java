package org.key_project.sed.core.model.memory;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.key_project.sed.core.model.ISEDDebugNode;
import org.key_project.sed.core.model.ISEDDebugTarget;
import org.key_project.sed.core.model.ISEDLoopCondition;
import org.key_project.sed.core.model.ISEDThread;
import org.key_project.sed.core.model.impl.AbstractSEDLoopCondition;

/**
 * Implementation of {@link ISEDLoopCondition} that stores all
 * information in the memory.
 * @author Martin Hentschel
 */
public class SEDMemoryLoopCondition extends AbstractSEDLoopCondition implements ISEDMemoryStackFrameCompatibleDebugNode, ISEDMemoryDebugNode {
   /**
    * The contained child nodes.
    */
   private List<ISEDDebugNode> children = new LinkedList<ISEDDebugNode>();
   
   /**
    * Constructor.
    * @param target The {@link ISEDDebugTarget} in that this loop condition is contained.
    * @param parent The parent in that this node is contained as child.
    * @param thread The {@link ISEDThread} in that this loop condition is contained.
    */
   public SEDMemoryLoopCondition(ISEDDebugTarget target, 
                              ISEDDebugNode parent, 
                              ISEDThread thread) {
      super(target, parent, thread);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ISEDDebugNode[] getChildren() throws DebugException {
      return children.toArray(new ISEDDebugNode[children.size()]);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void addChild(ISEDDebugNode child) {
      if (child != null) {
         children.add(child);
      }
   }
   
   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setName(String name) {
      super.setName(name);
   }

   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setLineNumber(int lineNumber) {
      super.setLineNumber(lineNumber);
   }

   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setCharStart(int charStart) {
      super.setCharStart(charStart);
   }

   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setCharEnd(int charEnd) {
      super.setCharEnd(charEnd);
   }
   
   /**
    * <p>
    * {@inheritDoc}
    * </p>
    * <p>
    * Changed visibility to public.
    * </p>
    */
   @Override
   public void setSourceName(String sourceName) {
      super.setSourceName(sourceName);
   }
}