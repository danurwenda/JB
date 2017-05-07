/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.corak.canting;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import java.io.FileNotFoundException;
import javax.media.j3d.BranchGroup;

/**
 *
 * @author RAPID02
 */
public class OBJLoader {

    public static BranchGroup createBranchGroup(String targetPath) {
        ObjectFile f = new ObjectFile();
        Scene s;
        try {
            s = f.load(targetPath);
        } catch (FileNotFoundException | IncorrectFormatException | ParsingErrorException ex) {
            return null;
        }
        return s.getSceneGroup();
    }
}
