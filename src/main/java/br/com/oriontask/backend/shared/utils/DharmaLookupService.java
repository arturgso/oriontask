package br.com.oriontask.backend.shared.utils;

import br.com.oriontask.backend.dharmas.model.Dharmas;
import java.util.UUID;

public interface DharmaLookupService {
  Dharmas getRequiredDharma(Long dharmasId, UUID userId);
}
