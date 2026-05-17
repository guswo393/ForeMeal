package com.meta.foremeal.recipe.api;

import com.meta.foremeal.recipe.service.RecipeImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecipeImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeImportService recipeImportService;

    @Test
    @WithMockUser(roles = "USER")
    void userCannotImportFoodSafetyRecipes() throws Exception {
        mockMvc.perform(post("/api/recipes/import/food-safety")
                        .param("start", "1")
                        .param("end", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanImportFoodSafetyRecipes() throws Exception {
        when(recipeImportService.importFoodSafetyRecipes(1, 5))
                .thenReturn(new RecipeImportService.ImportResult(5, 5, 0));

        mockMvc.perform(post("/api/recipes/import/food-safety")
                        .param("start", "1")
                        .param("end", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fetched").value(5))
                .andExpect(jsonPath("$.imported").value(5))
                .andExpect(jsonPath("$.skipped").value(0));

        verify(recipeImportService).importFoodSafetyRecipes(1, 5);
    }
}
