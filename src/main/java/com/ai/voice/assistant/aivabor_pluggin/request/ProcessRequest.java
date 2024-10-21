package com.ai.voice.assistant.aivabor_pluggin.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProcessRequest {
    private String dockerContainerName;
    private String url;
    private String username;
    private String password;
    private String tableName;
    private String idName;
    private List<String> fields;
}
