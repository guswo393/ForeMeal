package com.meta.foremeal.recipe.exception;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException() {
        super("레시피를 찾을 수 없습니다.");
    }
}
