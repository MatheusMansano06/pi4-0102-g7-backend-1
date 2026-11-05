package com.archflow.solicitacao;

import com.archflow.comum.AcessoNegadoException;
import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.ConfiguracaoPendenteException;
import com.archflow.comum.NaoAutenticadoException;
import com.archflow.comum.NaoEncontradoException;
import com.archflow.comum.NegocioException;
import com.archflow.comum.PerfilUsuario;
import com.archflow.comum.UsuarioAutenticado;
import com.archflow.material.Material;
import com.archflow.material.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitacaoCriacaoService {

    private final MaterialRepository materiais;
    private final SolicitacaoRepository solicitacoes;

    public SolicitacaoCriacaoService(MaterialRepository materiais, SolicitacaoRepository solicitacoes) {
        this.materiais = materiais;
        this.solicitacoes = solicitacoes;
    }

    @Transactional
    public SolicitacaoCriadaSaida criar(UsuarioAutenticado identidade, SolicitacaoCriacaoEntrada entrada) {
        UsuarioAutenticado aluno = this.exigirAluno(identidade);
        this.exigirEntradaValida(entrada);
        Material material = this.buscarMaterialAtivo(entrada.getMaterialId());
        this.exigirPoliticaDefinida(material);
        this.exigirParametrosPermitidos(material, entrada);
        Solicitacao solicitacao = new Solicitacao(aluno.getUsuarioId(), material,
                entrada.getQuantidade(), entrada.getObservacao());
        solicitacao.setDimensao(entrada.getDimensao());
        solicitacao.setDiametro(entrada.getDiametro());
        return new SolicitacaoCriadaSaida(this.solicitacoes.saveAndFlush(solicitacao));
    }

    private UsuarioAutenticado exigirAluno(UsuarioAutenticado identidade) {
        if (identidade == null) {
            throw new NaoAutenticadoException(CodigosDeErro.NAO_AUTENTICADO,
                    "É preciso autenticação válida para criar uma solicitação.");
        }
        if (identidade.getPerfil() != PerfilUsuario.ALUNO) {
            throw new AcessoNegadoException(CodigosDeErro.ACESSO_NEGADO,
                    "Somente o aluno pode criar uma solicitação.");
        }
        return identidade;
    }

    private void exigirEntradaValida(SolicitacaoCriacaoEntrada entrada) {
        if (entrada == null || entrada.getMaterialId() == null || entrada.getMaterialId() <= 0) {
            throw new NegocioException(CodigosDeErro.DADOS_INVALIDOS,
                    "Informe o identificador de um material.");
        }
        if (entrada.getQuantidade() == null || entrada.getQuantidade() <= 0) {
            throw new NegocioException(CodigosDeErro.QUANTIDADE_INVALIDA,
                    "A quantidade do pedido deve ser um inteiro positivo.");
        }
    }

    private Material buscarMaterialAtivo(Long materialId) {
        Material material = this.materiais.findByMaterialId(materialId);
        if (material == null || !material.isAtivo()) {
            throw new NaoEncontradoException(CodigosDeErro.MATERIAL_NAO_ENCONTRADO,
                    "O material não existe ou está inativo.");
        }
        return material;
    }

    private void exigirPoliticaDefinida(Material material) {
        if (material.getAceitaDimensao() == null || material.getExigeDimensao() == null
                || material.getAceitaDiametro() == null || material.getExigeDiametro() == null) {
            throw new ConfiguracaoPendenteException("Os parâmetros deste material ainda não foram definidos.");
        }
        if ((material.getExigeDimensao() && !material.getAceitaDimensao())
                || (material.getExigeDiametro() && !material.getAceitaDiametro())) {
            throw new ConfiguracaoPendenteException("Os parâmetros deste material precisam de revisão.");
        }
    }

    private void exigirParametrosPermitidos(Material material, SolicitacaoCriacaoEntrada entrada) {
        boolean temDimensao = entrada.getDimensao() != null && !entrada.getDimensao().trim().isEmpty();
        boolean temDiametro = entrada.getDiametro() != null;
        if ((temDimensao && !material.getAceitaDimensao()) || (!temDimensao && material.getExigeDimensao())
                || (temDiametro && !material.getAceitaDiametro()) || (!temDiametro && material.getExigeDiametro())) {
            throw new NegocioException(CodigosDeErro.PARAMETROS_INVALIDOS,
                    "Informe somente os parâmetros aceitos e preencha os obrigatórios do material.");
        }
        if (entrada.getDimensao() != null && !temDimensao) {
            throw new NegocioException(CodigosDeErro.PARAMETROS_INVALIDOS,
                    "A dimensão informada não pode conter somente espaços.");
        }
    }
}
