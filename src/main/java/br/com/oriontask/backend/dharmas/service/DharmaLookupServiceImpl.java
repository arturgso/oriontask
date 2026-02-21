package br.com.oriontask.backend.dharmas.service;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.dharmas.repository.DharmasRepository;
import br.com.oriontask.backend.shared.utils.DharmaLookupService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DharmaLookupServiceImpl implements DharmaLookupService {

  private final DharmasRepository repository;

  @Override
  public Dharmas getRequiredDharma(Long dharmasId, UUID userId) {
    return repository
        .findByIdAndUserId(dharmasId, userId)
        .orElseThrow(() -> new IllegalArgumentException("Dharmas not found"));
  }
}
