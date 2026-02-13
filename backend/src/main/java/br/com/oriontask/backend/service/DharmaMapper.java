package br.com.oriontask.backend.service;

import br.com.oriontask.backend.dto.dharmas.DharmaDTO;
import br.com.oriontask.backend.dto.dharmas.NewDharmaDTO;
import br.com.oriontask.backend.dto.dharmas.UpdateDharmaDTO;
import br.com.oriontask.backend.mappers.UsersMapper;
import br.com.oriontask.backend.model.Dharma;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {UsersMapper.class})
public interface DharmaMapper {
    Dharma toEntity(NewDharmaDTO dto);
    DharmaDTO toDTO(Dharma dharma);
    List<DharmaDTO> toDTO(List<Dharma> dharmas);

    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    Dharma partialUpdate(UpdateDharmaDTO updateDTO, @MappingTarget Dharma entity);
}
