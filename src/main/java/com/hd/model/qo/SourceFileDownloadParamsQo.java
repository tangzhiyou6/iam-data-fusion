package com.hd.model.qo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceFileDownloadParamsQo {

    private String fileId;

    private String fileType;

    private String remoteRelativePath;
}
