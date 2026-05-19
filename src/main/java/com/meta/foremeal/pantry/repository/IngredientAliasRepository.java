package com.meta.foremeal.pantry.repository;

import com.meta.foremeal.pantry.domain.IngredientAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientAliasRepository extends JpaRepository<IngredientAlias, Long> {
    Optional<IngredientAlias> findFirstByDetectedNameIgnoreCaseAndEnabledTrueOrderByAliasIdAsc(String detectedName);
}
