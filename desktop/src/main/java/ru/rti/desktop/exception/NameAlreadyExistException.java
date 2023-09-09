package ru.rti.desktop.exception;

public class NameAlreadyExistException extends RuntimeException {
  public NameAlreadyExistException(String message) {
    super(message);
  }
}
