package com.ai.voice.assistant.aivabor_pluggin.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableProcessRequest {
    private String tableName;
    private List<String> fields;
}
