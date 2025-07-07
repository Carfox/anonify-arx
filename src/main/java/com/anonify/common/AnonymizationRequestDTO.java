package com.anonify.common;

import java.util.List;

public class AnonymizationRequestDTO {
    private String filePath;
    private List<AttributeDTO> attributes;
    private PrivacyModelDTO privacyModel;

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public List<AttributeDTO> getAttributes() { return attributes; }
    public void setAttributes(List<AttributeDTO> attributes) { this.attributes = attributes; }

    public PrivacyModelDTO getPrivacyModel() { return privacyModel; }
    public void setPrivacyModel(PrivacyModelDTO privacyModel) { this.privacyModel = privacyModel; }
}
