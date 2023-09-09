package ru.rti.desktop.exception;

public class FileNameExistException extends RuntimeException {
  public FileNameExistException(String message) {
    super(message);
  }
}
