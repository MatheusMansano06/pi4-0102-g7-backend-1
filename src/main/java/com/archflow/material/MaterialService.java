package com.archflow.material;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

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

    // Busca vazia nao e erro: significa o catalogo inteiro.
    private List<Material> procurar(String busca) {
        if (busca == null || busca.trim().isEmpty()) {
            return this.materialRepository.findByAtivoTrueOrderByNomeAsc();
        }
        return this.materialRepository.findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
    }
}
