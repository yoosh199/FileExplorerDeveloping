package com.yoosh.fileexplorer;

import java.io.File;

public interface FileClickListener {

    void fileClick(File file);
    void fileLongClick(File file);
}
