package br.com.oriontask.backend.users.exception;

public class UserLookupExceptionImpl extends IllegalArgumentException {
  public UserLookupExceptionImpl() {
    super("User not found");
  }
}
