package br.com.oriontask.backend.exceptions.user;

public class UserLookupExceptionImpl extends IllegalArgumentException {
  public UserLookupExceptionImpl() {
    super("User not found");
  }
}
