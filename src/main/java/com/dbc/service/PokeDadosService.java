package com.dbc.service;

import com.dbc.dto.*;
import com.dbc.entity.PokemonEntity;
import com.dbc.exceptions.RegraDeNegocioException;
import com.dbc.repository.PokemonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PokeDadosService {
    private final PokemonRepository pokemonRepository;
    private final ObjectMapper objectMapper;
    private final PokemonService pokemonService;

    public List<PokeDadosDTO> list(Integer idPoke) throws RegraDeNegocioException {
        List<PokeDadosDTO> listaPokemonComTodosDados = new ArrayList<>();
        if (idPoke == null) {
            return  pokemonRepository.findAll()
                    .stream()
                    .map(this::setPokeDadosDTO)
                    .collect(Collectors.toList());
        }
        PokemonEntity pokemonEntity = pokemonService.findById(idPoke);
        listaPokemonComTodosDados.add(setPokeDadosDTO(pokemonEntity));
        return listaPokemonComTodosDados;
    }

    public PokeDadosDTO setPokeDadosDTO(PokemonEntity pokemon) {
        PokemonCreateDTO pokemonCreateDTO = objectMapper.convertValue(pokemon, PokemonCreateDTO.class);
        PokeDadosDTO pokeDadosDTO = new PokeDadosDTO();
        pokeDadosDTO.setPokemon(pokemonCreateDTO);
        pokeDadosDTO.setTipos(
                pokemon.getTipos()
                        .stream()
                        .map(tipo -> objectMapper.convertValue(tipo, TipoPokemonCreateDTO.class))
                        .collect(Collectors.toList())
        );
        pokeDadosDTO.setHabilidades(
                pokemon.getHabilidades()
                        .stream()
                        .map(habilidade -> objectMapper.convertValue(habilidade, HabilidadeCreateDTO.class))
                        .collect(Collectors.toList())
        );
        if (pokemon.getEvolucaoEntity() != null) {
            EvolucaoNomesDTO evolucaoNomesDTO = new EvolucaoNomesDTO();
            evolucaoNomesDTO.setEstagioUm(pokemon.getEvolucaoEntity().getEstagioUm().getNome());
            evolucaoNomesDTO.setEstagioDois(pokemon.getEvolucaoEntity().getEstagioDois().getNome());
            if (pokemon.getEvolucaoEntity().getEstagioTres() != null) {
                evolucaoNomesDTO.setEstagioTres(pokemon.getEvolucaoEntity().getEstagioTres().getNome());
            }
            pokeDadosDTO.setEvolucao(evolucaoNomesDTO);
        }
        return pokeDadosDTO;
    }
}