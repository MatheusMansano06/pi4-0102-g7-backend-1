
package com.archflow.material;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;

import com.archflow.comum.UsuarioAutenticado;

@RestController
@RequestMapping("/api/materiais")
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(MaterialService materialService) {
        this.materialService = materialService;
    }

    // CT02: GET /api/materiais?busca={termo}
    @GetMapping
    public List<MaterialSaida> listarMateriais(
            @RequestParam(required = false) String busca) {

        return this.materialService.listarMateriais(busca);
    }

    // Cadastro de materiais.
    @PostMapping
    public MaterialSaida cadastrarMaterial(
            @RequestAttribute(
                name = "usuarioAutenticado",
                required = false
            ) UsuarioAutenticado identidade,
            @RequestBody MaterialCriacaoEntrada entrada) {

        return this.materialService.cadastrarMaterial(
                identidade, entrada);
    }
}
