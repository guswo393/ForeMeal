package com.meta.foremeal.foodmaster.api;

import com.meta.foremeal.foodmaster.dto.FoodDto;
import com.meta.foremeal.foodmaster.service.FoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FoodControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FoodService foodService;

    @Test
    @WithMockUser
    void searchesFoods() throws Exception {
        when(foodService.searchFoods("닭가슴살"))
                .thenReturn(List.of(foodResponse()));

        mockMvc.perform(get("/api/foods/search").param("name", "닭가슴살"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].foodId").value(1))
                .andExpect(jsonPath("$[0].foodName").value("닭가슴살"))
                .andExpect(jsonPath("$[0].calories").value(110.0));

        verify(foodService).searchFoods("닭가슴살");
    }

    @Test
    @WithMockUser
    void getsFoodDetail() throws Exception {
        when(foodService.getFoodDetail(1L)).thenReturn(foodResponse());

        mockMvc.perform(get("/api/foods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId").value(1))
                .andExpect(jsonPath("$.foodName").value("닭가슴살"));

        verify(foodService).getFoodDetail(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotImportFoodSafetyFoods() throws Exception {
        mockMvc.perform(post("/api/foods/import/food-safety")
                        .param("name", "닭가슴살")
                        .param("start", "1")
                        .param("end", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanImportFoodSafetyFoods() throws Exception {
        when(foodService.importFoodsFromFoodSafety("닭가슴살", 1, 5))
                .thenReturn(new FoodDto.ImportResponse(5, 4, 1, 0));

        mockMvc.perform(post("/api/foods/import/food-safety")
                        .param("name", "닭가슴살")
                        .param("start", "1")
                        .param("end", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fetched").value(5))
                .andExpect(jsonPath("$.imported").value(4))
                .andExpect(jsonPath("$.updated").value(1))
                .andExpect(jsonPath("$.skipped").value(0));

        verify(foodService).importFoodsFromFoodSafety("닭가슴살", 1, 5);
    }

    private FoodDto.Response foodResponse() {
        return new FoodDto.Response(
                1L,
                "F001",
                "FOOD_SAFETY_KOREA",
                "닭가슴살",
                "육류",
                "테스트식품",
                "2025-01-23",
                "식품의약품안전처",
                100.0,
                "g",
                110.0,
                0.0,
                23.0,
                1.5,
                0.0,
                55.0,
                60.0,
                0.4,
                0.0,
                null
        );
    }
}
