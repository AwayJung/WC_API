package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wc_api.model.db.category.Category;
import wc_api.dao.CategoryDAO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryDAO categoryDAO;

    // 모든 카테고리 조회
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    // 특정 카테고리 조회
    public Category getCategoryById(Long categoryId) {
        Category category = categoryDAO.getCategoryById(categoryId);
        if (category == null) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        return category;
    }
}