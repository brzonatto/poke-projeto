package com.dbc.service;

import com.dbc.dto.*;
import com.dbc.entity.EvolucaoEntity;
import com.dbc.entity.PokemonEntity;
import com.dbc.exceptions.RegraDeNegocioException;
import com.dbc.repository.EvolucaoRepository;
import com.dbc.repository.PokemonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvolucaoService {
    private final EvolucaoRepository evolucaoRepository;
    private final PokemonRepository pokemonRepository;
    private final ObjectMapper objectMapper;

    private EvolucaoEntity findById(Integer id) throws RegraDeNegocioException {
        EvolucaoEntity entity = evolucaoRepository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException("Não encontrado"));
        return entity;
    }

    public Boolean existPokemonNaEvolucao(EvolucaoEntity evolucaoEntity) {
        if (evolucaoEntity.getEstagioTres() != null) {
            if (evolucaoRepository.searchByPokemon(evolucaoEntity.getEstagioUm().getIdPokemon()) == 0
                    && evolucaoRepository.searchByPokemon(evolucaoEntity.getEstagioDois().getIdPokemon()) == 0
                    && evolucaoRepository.searchByPokemon(evolucaoEntity.getEstagioTres().getIdPokemon()) == 0) {
                return false;
            }
        } else {
            if (evolucaoRepository.searchByPokemon(evolucaoEntity.getEstagioUm().getIdPokemon()) == 0
                    && evolucaoRepository.searchByPokemon(evolucaoEntity.getEstagioDois().getIdPokemon()) == 0) {
                return false;
            }
        }
        return true;
    }

    public Boolean existePokemonRepetidoNaEvolucao(EvolucaoEntity evolucao) {
        PokemonEntity pokemon1 = evolucao.getEstagioUm();
        PokemonEntity pokemon2 = evolucao.getEstagioDois();
        PokemonEntity pokemon3 = evolucao.getEstagioTres();
        return pokemon1.equals(pokemon2) || pokemon1.equals(pokemon3) || pokemon2.equals(pokemon3);
    }

    public EvolucaoDTO create(EvolucaoCreateDTO evolucaoCreateDTO) throws RegraDeNegocioException {
        PokemonEntity estagioUm = pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioUm()).get();
        PokemonEntity estagioDois = pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioDois()).get();
        PokemonEntity estagioTres = null;

        EvolucaoEntity evolucaoEntity = objectMapper.convertValue(evolucaoCreateDTO, EvolucaoEntity.class);
        evolucaoEntity.setEstagioUm(estagioUm);
        evolucaoEntity.setEstagioDois(estagioDois);
        evolucaoEntity.setEstagioTres(null);
        if (evolucaoCreateDTO.getIdEstagioTres() != null) {
            estagioTres = pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioTres()).get();
            evolucaoEntity.setEstagioTres(estagioTres);
        }

        if (existePokemonRepetidoNaEvolucao(evolucaoEntity)) {
            throw new RegraDeNegocioException("não deve conter pokemons repetidos na evolução");
        }
        if (existPokemonNaEvolucao(evolucaoEntity)) {
            throw new RegraDeNegocioException("Pokémon deve ser diferente, pois, já existe em uma evolução cadastrada");
        }

        EvolucaoEntity evolucaoCriada = evolucaoRepository.save(evolucaoEntity);

        estagioUm.setEvolucaoEntity(evolucaoCriada);
        estagioDois.setEvolucaoEntity(evolucaoCriada);
        if (evolucaoCreateDTO.getIdEstagioTres() != null) {
            estagioTres.setEvolucaoEntity(evolucaoCriada);
        }

        PokemonEntity updateEstagioUm = pokemonRepository.save(estagioUm);
        PokemonEntity updateEstagioDois = pokemonRepository.save(estagioDois);
        PokemonEntity updateEstagioTres = null;
        if (evolucaoCreateDTO.getIdEstagioTres() != null) {
            updateEstagioTres = pokemonRepository.save(estagioTres);
        }

        EvolucaoDTO evolucaoDTO = objectMapper.convertValue(evolucaoCriada, EvolucaoDTO.class);
        evolucaoDTO.setEstagioUm(objectMapper.convertValue(updateEstagioUm, PokemonDTO.class));
        evolucaoDTO.setEstagioDois(objectMapper.convertValue(updateEstagioDois, PokemonDTO.class));
        evolucaoDTO.setEstagioTres(objectMapper.convertValue(updateEstagioTres, PokemonDTO.class));
        return evolucaoDTO;
    }

    public List<EvolucaoDTO> list() {
        List<EvolucaoEntity> evolucoes = evolucaoRepository.findAll().stream().collect(Collectors.toList());

        List<EvolucaoDTO> evolucaosShow = evolucoes.stream()
                .map(evolucaoEntity -> {
                    EvolucaoDTO evolucaoDTO = objectMapper.convertValue(evolucaoEntity, EvolucaoDTO.class);
                    evolucaoDTO.setEstagioUm(objectMapper.convertValue(evolucaoEntity.getEstagioUm(), PokemonDTO.class));
                    evolucaoDTO.setEstagioDois(objectMapper.convertValue(evolucaoEntity.getEstagioDois(), PokemonDTO.class));
                    evolucaoDTO.setEstagioTres(objectMapper.convertValue(evolucaoEntity.getEstagioTres(), PokemonDTO.class));
                    return evolucaoDTO;
                })
                .collect(Collectors.toList());
        return evolucaosShow;
    }

    public EvolucaoDTO update(Integer idEvolucao, EvolucaoCreateDTO evolucaoCreateDTO) throws RegraDeNegocioException {
        EvolucaoEntity compare = new EvolucaoEntity();
        compare.setEstagioUm(pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioUm()).get());
        compare.setEstagioDois(pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioDois()).get());
        if (evolucaoCreateDTO.getIdEstagioTres() != null) {
            compare.setEstagioTres(pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioTres()).get());
        }

        EvolucaoEntity evolucaoEntity = findById(idEvolucao);
        EvolucaoEntity backup = new EvolucaoEntity();
        backup.setIdEvolucao(evolucaoEntity.getIdEvolucao());
        backup.setEstagioUm(evolucaoEntity.getEstagioUm());
        backup.setEstagioDois(evolucaoEntity.getEstagioDois());
        backup.setEstagioTres(evolucaoEntity.getEstagioTres());
        updateToNull(idEvolucao);

        if (existePokemonRepetidoNaEvolucao(compare)) {
            evolucaoRepository.save(backup);
            throw new RegraDeNegocioException("não deve conter pokemons repetidos na evolução");
        }
        if (existPokemonNaEvolucao(compare)) {
            evolucaoRepository.save(backup);
            throw new RegraDeNegocioException("Pokémon deve ser diferente, pois, já existe em uma evolução cadastrada");
        }

        evolucaoEntity.setEstagioUm(pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioUm()).get());
        evolucaoEntity.setEstagioDois(pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioDois()).get());
        if (evolucaoCreateDTO.getIdEstagioTres() != null) {
            evolucaoEntity.setEstagioTres(pokemonRepository.findById(evolucaoCreateDTO.getIdEstagioTres()).get());
        }
        EvolucaoEntity update = evolucaoRepository.save(evolucaoEntity);
        EvolucaoDTO evolucaoDTO = new EvolucaoDTO();
        evolucaoDTO.setIdEvolucao(update.getIdEvolucao());
        evolucaoDTO.setEstagioUm(objectMapper.convertValue(update.getEstagioUm(), PokemonDTO.class));
        evolucaoDTO.setEstagioDois(objectMapper.convertValue(update.getEstagioDois(), PokemonDTO.class));
        evolucaoDTO.setEstagioTres(objectMapper.convertValue(update.getEstagioTres(), PokemonDTO.class));
        return evolucaoDTO;
    }

    public void delete(Integer idEvolucao) throws RegraDeNegocioException {
        List<PokemonEntity> list = new ArrayList<>();
        EvolucaoEntity entity = findById(idEvolucao);
        PokemonEntity updateUm = pokemonRepository.findById(entity.getEstagioUm().getIdPokemon()).get();
        updateUm.setEvolucaoEntity(null);
        PokemonEntity updateDois = pokemonRepository.findById(entity.getEstagioDois().getIdPokemon()).get();
        updateDois.setEvolucaoEntity(null);
        list.add(updateUm);
        list.add(updateDois);
        if (entity.getEstagioTres() != null) {
            PokemonEntity updateTres = pokemonRepository.findById(entity.getEstagioTres().getIdPokemon()).get();
            updateTres.setEvolucaoEntity(null);
            list.add(updateTres);
        }
        pokemonRepository.saveAll(list);
        evolucaoRepository.delete(entity);
    }

    public void updateToNull(Integer idEvolucao) throws RegraDeNegocioException {
        EvolucaoEntity evolucaoEntity = evolucaoRepository.findById(idEvolucao).get();
        evolucaoEntity.setEstagioUm(null);
        evolucaoEntity.setEstagioDois(null);
        evolucaoEntity.setEstagioTres(null);
        evolucaoRepository.save(evolucaoEntity);
    }
}
