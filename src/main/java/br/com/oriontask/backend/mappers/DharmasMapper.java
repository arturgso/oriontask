package br.com.oriontask.backend.mappers;

import br.com.oriontask.backend.dto.dharmas.DharmasDTO;
import br.com.oriontask.backend.dto.dharmas.NewDharmasDTO;
import br.com.oriontask.backend.dto.dharmas.UpdateDharmasDTO;
import br.com.oriontask.backend.model.Dharmas;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {UsersMapper.class})
public interface DharmasMapper {
  Dharmas toEntity(NewDharmasDTO dto);

  DharmasDTO toDTO(Dharmas dharmas);

  List<DharmasDTO> toDTO(List<Dharmas> dharmas);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Dharmas partialUpdate(UpdateDharmasDTO updateDTO, @MappingTarget Dharmas entity);
}
