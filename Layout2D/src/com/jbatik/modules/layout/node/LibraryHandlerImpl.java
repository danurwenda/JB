/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jbatik.modules.layout.node;

import com.jbatik.modules.layout.api.DefaultImageLibrary;
import com.jbatik.modules.layout.api.LayoutLibrary;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 * Acts as the broker of library directory of a Project. Provide access to image
 * files inside it.
 *
 * @author RAPID02
 */
@ProjectServiceProvider(
        service = LibraryHandler.class,
        projectType = "com-jbatik-project")
public class LibraryHandlerImpl implements LibraryHandler {

    /**
     * Cleaning reference.
     */
    private static final class ActiveRef<T> extends SoftReference<LayoutLibrary> implements Runnable {

        private final Map<T, ActiveRef<T>> holder;
        private final T key;

        public ActiveRef(LayoutLibrary o, Map<T, ActiveRef<T>> holder, T key) {
            super(o, Utilities.activeReferenceQueue());
            this.holder = holder;
            this.key = key;
        }

        @Override
        public void run() {
            synchronized (holder) {
                holder.remove(key);
            }
        }
    }
    //library cache
    private final Map<String, ActiveRef<String>> cache = new HashMap<>(128);
    //project that owns the library
    private final Project p;

    public LibraryHandlerImpl(Project p) {
        this.p = p;
    }

    /**
     * TODO : handle by extension? mime? and create a suitable ImageLibrary
     * impl.
     *
     * Sementara hanya handle PNG files.
     *
     * @param name
     * @return
     */
    private LayoutLibrary loadImage(String name) {

        LayoutLibrary result = null;
        FileObject libDirFO = LayoutFileUtil.getLibrariesFolder(p, false);
        //to get the full image
        FileObject imageFO = libDirFO.getFileObject(name);
        if (imageFO != null) {
            if (imageFO.getExt().equalsIgnoreCase("png")) {
                return new DefaultImageLibrary(imageFO);
            }
        } else {
            return new DefaultImageLibrary(libDirFO, name);
        }
        return result;
    }

    @Override
    public LayoutLibrary getLibrary(String name) {
        ActiveRef<String> ref = cache.get(name);
        LayoutLibrary img = null;

        if (ref != null) {
            img = ref.get();
        }

        // icon found
        if (img != null) {
            return img;
        }

        //this point is reachable only if ref == null
        synchronized (cache) {
            // again under the lock
            ref = cache.get(name);

            if (ref != null) {
                // then it is SoftRefrence
                img = ref.get();
            }

            if (img != null) {
                // cannot be NO_ICON, since it never disappears from the map.
                return img;
            }
            //this point is reachable only if ref == null UNDER SYNC
            LayoutLibrary result = loadImage(name);
            if (result != null) {
                //add to cache
                name = new String(name).intern(); // NOPMD
                cache.put(name, new ActiveRef<>(result, cache, name));
                return result;
            }
            return null;
        }
    }

}
