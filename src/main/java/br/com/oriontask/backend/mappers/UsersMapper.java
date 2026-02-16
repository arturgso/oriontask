package br.com.oriontask.backend.mappers;

import br.com.oriontask.backend.dto.auth.SignupRequestDTO;
import br.com.oriontask.backend.dto.users.UpdateUserDTO;
import br.com.oriontask.backend.dto.users.UserResponseDTO;
import br.com.oriontask.backend.model.Users;
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
