package com.tutorhub.common.exception;
import org.springframework.http.HttpStatus;
public class UnauthorizedException extends ApiException { public UnauthorizedException(String message) { super("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED); } }
