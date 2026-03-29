package com.ibisscore.match.mapper;

import com.ibisscore.common.dto.FixtureDTO;
import com.ibisscore.common.dto.LeagueDTO;
import com.ibisscore.common.dto.TeamDTO;
import com.ibisscore.match.entity.Fixture;
import com.ibisscore.match.entity.League;
import com.ibisscore.match.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FixtureMapper {

    @Mapping(target = "prediction", ignore = true)
    @Mapping(target = "bestOdds", ignore = true)
    FixtureDTO toDto(Fixture fixture);

    List<FixtureDTO> toDtoList(List<Fixture> fixtures);

    LeagueDTO toDto(League league);

    TeamDTO toDto(Team team);
}
