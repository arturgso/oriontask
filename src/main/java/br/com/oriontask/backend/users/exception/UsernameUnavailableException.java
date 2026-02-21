package br.com.oriontask.backend.users.exception;

public class UsernameUnavailableException extends RuntimeException {
  public UsernameUnavailableException() {
    super("Username unavailable");
  }
}
