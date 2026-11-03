package com.archflow.material;

import com.archflow.comum.CodigosDeErro;
import com.archflow.comum.ErroSaida;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    // CT02: GET /api/materiais?busca={termo}
    @GetMapping
    public List<MaterialSaida> listarMateriais(@RequestParam(required = false) String busca) {
        return this.materialService.listarMateriais(busca);
    }

    // CT05: PATCH /api/materiais/{materialId}/estoque
    // Provisório, como na fila: X-Perfil deve ser substituído pela sessão do H02.2.
    @PatchMapping("/{materialId}/estoque")
    public MaterialEstoqueSaida atualizarEstoque(
            @RequestHeader(name = "X-Perfil", required = false) String perfil,
            @PathVariable Long materialId,
            @RequestBody MaterialEstoqueEntrada entrada) {
        return this.materialService.atualizarEstoque(perfil, materialId, entrada);
    }

    // O corpo do CT05 precisa ser um objeto JSON legível, com quantidade inteira.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroSaida tratarCorpoInvalido(HttpMessageNotReadableException erro) {
        return new ErroSaida(CodigosDeErro.QUANTIDADE_INVALIDA,
                "Informe uma quantidade inteira não negativa em um objeto JSON válido.");
    }
}
