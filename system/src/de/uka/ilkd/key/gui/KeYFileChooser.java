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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class KeYFileChooser {

    private final JFileChooser fileChooser;

    private boolean saveDialog;

    public KeYFileChooser(String initDir) {
	fileChooser = new JFileChooser(new File(initDir)) {
                public void approveSelection() {
                    File file = getSelectedFile();
                    if (saveDialog && file.exists() &&
                            showOverwriteDialog(file) != JOptionPane.YES_OPTION) {
                        return;
                    }
                    super.approveSelection();
                }
            };
	fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
		public boolean accept(File f) {
		    return 
			    f.isDirectory() 
			|| f.toString().endsWith(".key") 
			|| f.toString().endsWith(".java")
			|| f.toString().endsWith(".proof");
		}
		
		public String getDescription() {
		    return "KeY and Java files";
		}
	    });
    }

    public void prepare() {
        File selFile = fileChooser.getSelectedFile();
        if ((selFile != null) && selFile.isFile()) { // present & not dir.
            String filename = selFile.getAbsolutePath();    
	    if (!filename.endsWith(".proof")) 
                fileChooser.setSelectedFile(new File(filename+".proof")); 
        }
    }

    public void setDialogTitle(String title) {
	if (title!=null) {
	    fileChooser.setDialogTitle (title);
	} else {
	    fileChooser.setDialogTitle ("Select file to load");
	}
    }

    private void setSaveDialog(boolean b) {
        saveDialog = b;
	fileChooser.setFileSelectionMode(b 
		                         ? JFileChooser.FILES_ONLY 
		                         : JFileChooser.FILES_AND_DIRECTORIES);        
    }

    public boolean showSaveDialog(Main main, String defaultName) {
	if(defaultName != null) {
	    File file = new File(fileChooser.getCurrentDirectory(), 
		    		 defaultName);
	    fileChooser.setSelectedFile(file);
	}
	
        setSaveDialog(true);
	int result = fileChooser.showSaveDialog(main);
	return (result == JFileChooser.APPROVE_OPTION);
    }

    public boolean showOpenDialog(Main main) {
        setSaveDialog(false);
	int result = fileChooser.showOpenDialog(main);
	return (result == JFileChooser.APPROVE_OPTION);
    }

    public File getSelectedFile() {
	return fileChooser.getSelectedFile();
    }

    public void selectFile(File f) {
	fileChooser.setSelectedFile(f);
    }

    private int showOverwriteDialog(File file) {
        return JOptionPane.showOptionDialog(fileChooser, "File " +
                file.getAbsolutePath() + " already exists. Overwrite?",
                "Save warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, null, null);
    }

}