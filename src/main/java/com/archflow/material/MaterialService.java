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

    // #1144 Cadastro de um novo material.
    @Transactional
    public MaterialSaida cadastrarMaterial(
            UsuarioAutenticado identidade,
            MaterialCriacaoEntrada entrada) {

        this.exigirResponsavel(identidade);

        if (entrada == null
                || entrada.getNome() == null
                || entrada.getNome().trim().isEmpty()
                || entrada.getEspecificacao() == null
                || entrada.getEspecificacao().trim().isEmpty()) {
            throw new NegocioException(
                    CodigosDeErro.DADOS_INVALIDOS,
                    "Nome e especificação são obrigatórios.");
        }
        this.exigirTamanhoTexto(entrada.getNome());
        this.exigirTamanhoTexto(entrada.getDescricao());
        this.exigirTamanhoTexto(entrada.getEspecificacao());

        if (entrada.getQuantidadeDisponivel() == null
                || entrada.getQuantidadeDisponivel() < 0
                || entrada.getQuantidadeMinima() == null
                || entrada.getQuantidadeMinima() < 0) {
            throw new NegocioException(
                    CodigosDeErro.QUANTIDADE_INVALIDA,
                    "As quantidades devem ser inteiros não negativos.");
        }

        Material material = new Material(
                entrada.getNome(),
                entrada.getDescricao(),
                entrada.getEspecificacao(),
                entrada.getQuantidadeDisponivel(),
                entrada.getQuantidadeMinima());

        Material salvo = this.materialRepository.saveAndFlush(material);

        return new MaterialSaida(salvo);
    }

    private void exigirTamanhoTexto(String valor) {
        if (valor != null && valor.length() > 255) {
            throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                    "Os textos do material devem ter até 255 caracteres.");
        }
    }

    private void exigirResponsavel(UsuarioAutenticado identidade) {
        if (identidade == null) {
            throw new NaoAutenticadoException(
                    CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso estar autenticado para realizar esta operação.");
        }

        if (!PerfilUsuario.RESPONSAVEL.equals(identidade.getPerfil())) {
            throw new AcessoNegadoException(
                    CodigosDeErro.ACESSO_NEGADO,
                    "Só o responsável pode realizar esta operação.");
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
