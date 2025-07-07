package com.anonify.common;

import java.util.Map;

public class AttributeDTO {
    private String name;
    private String attributeType;
    private Map<String, Object> hierarchyStrategy;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAttributeType() { return attributeType; }
    public void setAttributeType(String attributeType) { this.attributeType = attributeType; }

    public Map<String, Object> getHierarchyStrategy() { return hierarchyStrategy; }
    public void setHierarchyStrategy(Map<String, Object> hierarchyStrategy) { this.hierarchyStrategy = hierarchyStrategy; }
}
