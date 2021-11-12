package com.dbc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Setter
@Getter
@Entity(name = "pokemon")
public class PokemonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_id_pokemon")
    @SequenceGenerator(name = "seq_id_pokemon", sequenceName = "seq_id_pokemon", allocationSize = 1)
    @Column(name = "id_pokemon")
    private Integer idPokemon;

    @Column(name = "numero_pokemon")
    private Integer numero;

    @Column(name = "nome_pokemon")
    private String nome;

    @Column(name = "level_pokemon")
    private Integer level;

    @Column(name = "peso_pokemon")
    private Double peso;

    @Column(name = "altura_pokemon")
    private Double altura;

    @Column(name = "categoria_pokemon")
    private String categoria;

    @Column(name = "regiao_dominante_poke_lendario")
    private String regiaoDominante;

    @Embedded
    private StatusEntity status;

    @JsonIgnore
    @OneToMany(mappedBy = "pokemon", fetch = FetchType.LAZY)
    private Set<TipoPokemonEntity> tipos;
}
