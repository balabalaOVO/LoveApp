package com.yupi.yuaiagent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadInfo {
    private String name;
    private String url;
    private String cosKey;
    private long size;
    private String contentType;
}
