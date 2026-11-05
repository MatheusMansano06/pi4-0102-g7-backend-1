package com.archflow.material;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    Material findByMaterialId(Long materialId);

    List<Material> findByAtivoTrueOrderByNomeAsc();

    List<Material> findByAtivoTrueAndNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
