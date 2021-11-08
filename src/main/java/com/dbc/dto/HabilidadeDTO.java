package com.dbc.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HabilidadeDTO {
    private Integer idHabilidade;

    @NotEmpty
    @NotBlank
    @ApiModelProperty("Nome da habilidade")
    private String nome;

    @NotNull
    @ApiModelProperty("Número de Multiplicador de poder")
    private Double multiplicacaoDePoder;
}
