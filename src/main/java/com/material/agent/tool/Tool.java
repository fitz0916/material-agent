package com.material.agent.tool;

public interface Tool {
    String getName();
    String getDescription();
    Object execute(Object params);
}
