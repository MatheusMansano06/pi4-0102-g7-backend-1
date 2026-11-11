package com.archflow.material;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NaoEncontradoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    // CT02: o catalogo mostra so material ativo, em ordem alfabetica.
    public List<MaterialSaida> listarMateriais(String busca) {
        List<Material> encontrados = this.procurar(busca);
        List<MaterialSaida> catalogo = new ArrayList<MaterialSaida>();
        for (Material material : encontrados) {
            catalogo.add(new MaterialSaida(material));
        }
        return catalogo;
    }

    // CT05: a quantidade recebida substitui o saldo.
    @Transactional
    public MaterialEstoqueSaida atualizarEstoque(UsuarioAutenticado identidade, Long materialId,
            MaterialEstoqueEntrada entrada) {
        this.exigirResponsavel(identidade);
        this.exigirQuantidadeValida(entrada);
        Material material = this.buscar(materialId);
        material.atualizarEstoque(entrada.getQuantidadeDisponivel(), LocalDateTime.now().withNano(0));
        return new MaterialEstoqueSaida(this.materialRepository.saveAndFlush(material));
    }

    private void exigirResponsavel(UsuarioAutenticado identidade) {
        if (identidade == null) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para atualizar o estoque.");
        }
        if (!PerfilUsuario.RESPONSAVEL.equals(identidade.getPerfil())) {
            throw new AcessoNegadoException(CodigosDeErro.ACESSO_NEGADO,
                    "Só o responsável pode atualizar o estoque.");
        }
    }

    private void exigirQuantidadeValida(MaterialEstoqueEntrada entrada) {
        if (entrada == null || entrada.getQuantidadeDisponivel() == null
                || entrada.getQuantidadeDisponivel() < 0) {
            throw new NegocioException(CodigosDeErro.QUANTIDADE_INVALIDA,
                    "A quantidade disponível deve ser um inteiro não negativo.");
        }
    }

    private Material buscar(Long materialId) {
        Material material = this.materialRepository.findByMaterialId(materialId);
        if (material == null) {
            throw new NaoEncontradoException(CodigosDeErro.MATERIAL_NAO_ENCONTRADO,
                    "O material " + materialId + " não existe.");
        }
        return material;
    }

    // Busca vazia e o catalogo inteiro.
    private List<Material> procurar(String busca) {
        if (busca == null || busca.trim().isEmpty()) {
            return this.materialRepository.findByAtivoTrueOrderByNomeAsc();
        }
        return this.materialRepository.findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
    }
}
