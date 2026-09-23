package com.example.Ecommerce.services;

import com.example.Ecommerce.dto.CategoryRequest;
import com.example.Ecommerce.dto.CategoryResponse;
import com.example.Ecommerce.entity.Category;
import com.example.Ecommerce.exception.BadRequestException;
import com.example.Ecommerce.exception.ResourceNotFoundException;
import com.example.Ecommerce.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository) {

        this.categoryRepository = categoryRepository;
    }

    @Caching(
            put = @CachePut(
                    value = "categories",
                    key = "#result.id"
            ),
            evict = @CacheEvict(
                    value = "categoryList",
                    allEntries = true
            )
    )
    public CategoryResponse create(
            CategoryRequest request) {

        if (categoryRepository.existsByName(
                request.getName())) {

            throw new BadRequestException(
                    "Category already exists"
            );
        }

        Category category = new Category();

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category saved =
                categoryRepository.save(category);

        return mapToResponse(saved);
    }

    @Cacheable(value = "categoryList")
    public List<CategoryResponse> getAll() {

        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Cacheable(value = "categories", key = "#id")
    public CategoryResponse getById(Long id) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: "
                                                + id
                                )
                        );

        return mapToResponse(category);
    }

    @Caching(
            put = @CachePut(
                    value = "categories",
                    key = "#id"
            ),
            evict = @CacheEvict(
                    value = "categoryList",
                    allEntries = true
            )
    )
    public CategoryResponse update(
            Long id,
            CategoryRequest request) {

        Category category =
                categoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Category not found with id: "
                                                + id
                                )
                        );

        if (categoryRepository.existsByName(
                request.getName())
                && !category.getName()
                .equalsIgnoreCase(request.getName())) {

            throw new BadRequestException(
                    "Category already exists"
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        Category updated =
                categoryRepository.save(category);

        return mapToResponse(updated);
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "categories",
                            key = "#id"
                    ),
                    @CacheEvict(
                            value = "categoryList",
                            allEntries = true
                    )
            }
    )
    public void delete(Long id) {

        if (!categoryRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Category not found with id: " + id
            );
        }

        categoryRepository.deleteById(id);
    }

    private CategoryResponse mapToResponse(
            Category category) {

        CategoryResponse response =
                new CategoryResponse();

        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(
                category.getDescription()
        );

        return response;
    }
}