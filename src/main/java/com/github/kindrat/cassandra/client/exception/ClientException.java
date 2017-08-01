package com.github.kindrat.cassandra.client.exception;

public abstract class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }
}
