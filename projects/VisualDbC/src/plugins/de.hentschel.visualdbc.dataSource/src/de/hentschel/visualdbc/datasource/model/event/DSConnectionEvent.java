/*******************************************************************************
 * Copyright (c) 2011 Martin Hentschel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Hentschel - initial API and implementation
 *******************************************************************************/

package de.hentschel.visualdbc.datasource.model.event;

import java.util.EventObject;

import de.hentschel.visualdbc.datasource.model.IDSConnection;

/**
 * An event thrown from an {@link IDSConnection}.
 * @author Martin Hentschel
 */
public class DSConnectionEvent extends EventObject {
   /**
    * Generated UID.
    */
   private static final long serialVersionUID = -2049733225307895584L;

   /**
    * Constructor.
    * @param source The {@link IDSConnection} that has thrown this event.
    */
   public DSConnectionEvent(IDSConnection source) {
      super(source);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public IDSConnection getSource() {
      return (IDSConnection)super.getSource();
   }
}