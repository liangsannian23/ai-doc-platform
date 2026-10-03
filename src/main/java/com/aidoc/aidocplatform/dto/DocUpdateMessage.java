package com.aidoc.aidocplatform.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class DocUpdateMessage implements Serializable {
    private Long docId;
    private Long userId;
    private String operation; // CREATE / UPDATE / DELETE
    private Long timestamp;
}