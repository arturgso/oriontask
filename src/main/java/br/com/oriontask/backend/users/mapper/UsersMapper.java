package br.com.oriontask.backend.users.mapper;

import br.com.oriontask.backend.auth.dto.SignupRequestDTO;
import br.com.oriontask.backend.users.dto.UpdateUserDTO;
import br.com.oriontask.backend.users.dto.UserResponseDTO;
import br.com.oriontask.backend.users.model.Users;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface UsersMapper {
  Users toEntity(SignupRequestDTO createDTO);

  UserResponseDTO toDTO(Users entity);

  List<UserResponseDTO> toDTO(List<Users> list);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Users partialUpdate(UpdateUserDTO updateDTO, @MappingTarget Users entity);
}
