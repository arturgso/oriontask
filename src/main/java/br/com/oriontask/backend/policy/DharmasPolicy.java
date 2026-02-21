package br.com.oriontask.backend.policy;

import org.springframework.stereotype.Component;

@Component
public class DharmasPolicy {

  private static final Integer MAX_DHARMAS_PER_USER = 8;

  public void validateMaxDharmasPerUser(Long dharmaCount) {
    if (dharmaCount >= MAX_DHARMAS_PER_USER) {
      throw new IllegalStateException("Maximum number of dharmas reached for this user");
    }
  }
}
