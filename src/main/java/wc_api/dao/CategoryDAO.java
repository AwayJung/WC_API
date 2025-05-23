package wc_api.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import wc_api.model.db.category.Category;

import java.util.List;

@Mapper
public interface CategoryDAO {

    // 모든 카테고리 조회
    List<Category> getAllCategories();

    // 특정 카테고리 조회
    Category getCategoryById(@Param("categoryId") Long categoryId);

}