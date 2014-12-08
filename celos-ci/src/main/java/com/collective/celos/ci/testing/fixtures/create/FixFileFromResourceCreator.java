package com.collective.celos.ci.testing.fixtures.create;

import com.collective.celos.ci.mode.test.TestRun;
import com.collective.celos.ci.testing.structure.fixobject.FixFile;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by akonopko on 10/7/14.
 */
public class FixFileFromResourceCreator implements FixObjectCreator<FixFile> {

    private final File path;

    public FixFileFromResourceCreator(File testCasesDir, String path) {
        this.path = new File(testCasesDir, path);
    }

    public FixFile create(TestRun testRun) throws Exception {
        if (!path.isFile()) {
            throw new IllegalStateException("Cannot find file: " + path);
        }
        return new FixFile(new FileInputStream(path));
    }

    @Override
    public String getDescription(TestRun testRun) {
        return path.getAbsolutePath();
    }

    public File getPath() {
        return path;
    }
}
