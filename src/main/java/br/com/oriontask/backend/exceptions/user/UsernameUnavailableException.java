package br.com.oriontask.backend.exceptions.user;

public class UsernameUnavailableException extends RuntimeException {
  public UsernameUnavailableException() {
    super("Username unavailable");
  }
}
