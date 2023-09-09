package ru.rti.desktop.exception;

public class TimeoutConnectionException extends RuntimeException {
  public TimeoutConnectionException(String message) {
    super(message);
  }
}
