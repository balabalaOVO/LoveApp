package com.yupi.yuaiagent.constant;

import java.io.File;

public interface FileConstant {

    /**
     * 文件保存目录，优先使用系统临时目录保证跨平台可写
     */
    String FILE_SAVE_DIR = System.getProperty("java.io.tmpdir") + File.separator + "yu-ai-agent";
}
