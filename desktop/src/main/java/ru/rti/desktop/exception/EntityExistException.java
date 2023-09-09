package ru.rti.desktop.exception;

public class EntityExistException extends RuntimeException {
  public EntityExistException(String message) {
    super(message);
  }
}
