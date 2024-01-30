package org.processmining.processcomparisonpetrinetdecomposition.util;
 
import java.io.File;
import java.net.MalformedURLException;

import org.processmining.framework.util.ProMClassLoader;
 
public class MyProMDebugClassLoader extends ProMClassLoader {
 
                // Before
                // -ea -Xmx8G -Djava.library.path=C:\Users\brockhoff\.ProM\packages\lpsolve-5.5.4\lib\win64 -Djava.util.Arrays.useLegacyMergeSort=true -Djava.system.class.loader=org.processmining.framework.util.ProMClassLoader  --add-opens java.base/java.net=ALL-UNNAMED -verbose
                public MyProMDebugClassLoader(ClassLoader loader) {
                                super(loader);
                                // TODO Auto-generated constructor stub
                }
                
                public void appendToClassPathForInstrumentation(String path) throws MalformedURLException {
        addURL(new File(path).toURI().toURL());
    }
 
}
