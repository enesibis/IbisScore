package com.ibisscore.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeamDTO {
    private Long id;
    private Integer apiId;
    private String name;
    private String shortName;
    private String logoUrl;
    private String country;
    private String form;          // "WWDLW"
    private Integer formPoints;
}
