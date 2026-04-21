package org.abondar.experimental.concurrency.pattern.future;

public class FutureException extends RuntimeException {

  public FutureException(Throwable cause) {
    super(cause);
  }

  public FutureException(String message) {
    super(message);
  }
}
