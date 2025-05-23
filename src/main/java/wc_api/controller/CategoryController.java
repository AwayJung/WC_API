package wc_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wc_api.common.constant.ApiRespPolicy;
import wc_api.common.model.response.ApiResp;
import wc_api.model.db.category.Category;
import wc_api.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class CategoryController {

    private final CategoryService categoryService;

    // 모든 카테고리 조회
    @GetMapping("/")
    public ResponseEntity<ApiResp> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, categories));
    }

    // 특정 카테고리 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResp> getCategoryById(@PathVariable Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId);
        return ResponseEntity
                .status(ApiRespPolicy.SUCCESS.getHttpStatus())
                .body(ApiResp.of(ApiRespPolicy.SUCCESS, category));
    }

}