package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.CategoryRequest;
import com.example.Ecommerce.dto.CategoryResponse;
import com.example.Ecommerce.entity.Category;
import com.example.Ecommerce.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryRequest request) {

        if (categoryRepository.existsByName(request.getName())) {
            throw new RuntimeException("Category already exists");
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category saved = categoryRepository.save(category);

        return mapToResponse(saved);
    }

    public List<CategoryResponse> getAll() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CategoryResponse getById(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        return mapToResponse(category);
    }

    public CategoryResponse update(Long id, CategoryRequest request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);

        return mapToResponse(updated);
    }

    public void delete(Long id) {

        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }

        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(Category category) {

        CategoryResponse response = new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());

        return response;
    }
}