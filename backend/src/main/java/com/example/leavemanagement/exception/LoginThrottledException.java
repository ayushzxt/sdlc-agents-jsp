package com.example.leavemanagement.exception;

public class LoginThrottledException extends RuntimeException {
    public LoginThrottledException() { super("Too many login attempts"); }
}