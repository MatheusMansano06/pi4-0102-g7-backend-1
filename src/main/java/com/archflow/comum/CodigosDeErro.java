package com.archflow.comum;

// Os codigos de erro que os contratos CT01 a CT13 definem.
public class CodigosDeErro {

    // Autenticacao e permissao
    public static final String NAO_AUTENTICADO = "NAO_AUTENTICADO";
    public static final String CREDENCIAIS_INVALIDAS = "CREDENCIAIS_INVALIDAS";
    public static final String USUARIO_INATIVO = "USUARIO_INATIVO";
    public static final String ACESSO_NEGADO = "ACESSO_NEGADO";

    // Dados que o cliente mandou
    public static final String DADOS_INVALIDOS = "DADOS_INVALIDOS";
    public static final String QUANTIDADE_INVALIDA = "QUANTIDADE_INVALIDA";
    public static final String PARAMETROS_INVALIDOS = "PARAMETROS_INVALIDOS";
    public static final String STATUS_INVALIDO = "STATUS_INVALIDO";
    public static final String PREVISAO_INVALIDA = "PREVISAO_INVALIDA";

    // Coisas que nao existem
    public static final String MATERIAL_NAO_ENCONTRADO = "MATERIAL_NAO_ENCONTRADO";
    public static final String SOLICITACAO_NAO_ENCONTRADA = "SOLICITACAO_NAO_ENCONTRADA";

    // Falha inesperada
    public static final String ERRO_INTERNO = "ERRO_INTERNO";
    public static final String CONFIGURACAO_PENDENTE = "CONFIGURACAO_PENDENTE";
}
