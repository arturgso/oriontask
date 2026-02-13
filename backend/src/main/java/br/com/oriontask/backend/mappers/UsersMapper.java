package br.com.oriontask.backend.mappers;

import br.com.oriontask.backend.dto.SignupRequestDTO;
import br.com.oriontask.backend.dto.UpdateUserDTO;
import br.com.oriontask.backend.dto.UserResponseDTO;
import br.com.oriontask.backend.model.Users;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsersMapper {
    Users toEntity(SignupRequestDTO createDTO);
    UserResponseDTO toDTO(Users entity);

    List<UserResponseDTO> toDTO(List<Users> list);

    @BeanMapping(nullValuePropertyMappingStrategy =  NullValuePropertyMappingStrategy.IGNORE)
    Users partialUpdate(UpdateUserDTO updateDTO, @MappingTarget Users entity);
}
