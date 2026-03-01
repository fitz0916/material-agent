package com.material.agent.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, String identifier) {
        super(404, resource + " not found: " + identifier);
    }

    public ResourceNotFoundException(String message) {
        super(404, message);
    }
}
