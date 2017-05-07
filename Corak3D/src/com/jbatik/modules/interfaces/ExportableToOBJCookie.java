/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jbatik.modules.interfaces;

import javax.media.j3d.BranchGroup;
import org.openide.nodes.Node.Cookie;

/**
 *
 * @author RAPID01
 */
public interface ExportableToOBJCookie extends Cookie {
    
    public BranchGroup getRootGroupForOBJ();
    
    public String getDefaultOBJFilenameSuffix();
    
    public String getProjectPathForOBJ();
    
    public void renderEnclosedOBJ();
    
    public void doneRenderEnclosedOBJ();
}
