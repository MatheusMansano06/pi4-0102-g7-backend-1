package com.archflow.material;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.ErroSaida;
import com.archflow.comum.UsuarioAutenticado;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Entrega parcial: não registra a rota na configuração padrão da aplicação.
// A propriedade é ativada somente nos testes até validar a migração e o fluxo.
@RestController
@RequestMapping("/api/materiais")
@ConditionalOnProperty(name = "archflow.estoque.em-desenvolvimento", havingValue = "true")
public class MaterialEstoqueController {

    private final MaterialService materialService;

    public MaterialEstoqueController(MaterialService materialService) {
        this.materialService = materialService;
    }

    @PatchMapping("/{materialId}/estoque")
    public MaterialEstoqueSaida atualizarEstoque(
            @RequestAttribute(name = "usuarioAutenticado", required = false) UsuarioAutenticado identidade,
            @PathVariable Long materialId,
            @RequestBody MaterialEstoqueEntrada entrada) {
        return this.materialService.atualizarEstoque(identidade, materialId, entrada);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarCorpoInvalido(HttpMessageNotReadableException erro) {
        return new ErroSaida(CodigosDeErro.QUANTIDADE_INVALIDA,
                "Informe uma quantidade inteira não negativa em um objeto JSON válido.");
    }
}
