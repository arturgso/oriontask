package br.com.oriontask.backend.dharmas.mapper;

import br.com.oriontask.backend.dharmas.dto.DharmasDTO;
import br.com.oriontask.backend.dharmas.dto.NewDharmasDTO;
import br.com.oriontask.backend.dharmas.dto.UpdateDharmasDTO;
import br.com.oriontask.backend.dharmas.model.Dharmas;
import br.com.oriontask.backend.users.mapper.UsersMapper;
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
