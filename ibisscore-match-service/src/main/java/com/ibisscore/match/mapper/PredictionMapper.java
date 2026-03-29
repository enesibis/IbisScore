package com.ibisscore.match.mapper;

import com.ibisscore.common.dto.PredictionDTO;
import com.ibisscore.match.entity.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PredictionMapper {

    @Mapping(target = "fixtureId", source = "fixture.id")
    @Mapping(target = "bttsProbability", source = "bttsProb")
    @Mapping(target = "valueBet", ignore = true)
    PredictionDTO toDto(Prediction prediction);

    List<PredictionDTO> toDtoList(List<Prediction> predictions);
}
