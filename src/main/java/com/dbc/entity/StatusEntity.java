package com.dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusEntity {
    private Integer hp;
    private Integer ataque;
    private Integer defesa;
    private Integer especialAtaque;
    private Integer especialDefesa;
    private Integer velocidade;
}
