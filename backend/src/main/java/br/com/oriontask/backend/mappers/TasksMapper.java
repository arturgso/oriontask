package br.com.oriontask.backend.mappers;

import br.com.oriontask.backend.dto.tasks.NewTaskDTO;
import br.com.oriontask.backend.dto.tasks.TaskDTO;
import br.com.oriontask.backend.dto.tasks.UpdateTaskDTO;
import br.com.oriontask.backend.model.Tasks;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TasksMapper {
    @Mapping(target = "dharmas", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "hidden", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "snoozedUntil", ignore = true)
    Tasks toEntity(NewTaskDTO createDTO);

    @Mapping(source = "dharmas.id", target = "dharmasId")
    TaskDTO toDTO(Tasks entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Tasks partialUpdate(UpdateTaskDTO updateDTO, @MappingTarget Tasks entity);
}
