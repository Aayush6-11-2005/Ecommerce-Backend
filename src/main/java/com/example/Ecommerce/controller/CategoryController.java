package com.example.Ecommerce.controller;

import com.example.Ecommerce.dto.CategoryRequest;
import com.example.Ecommerce.dto.CategoryResponse;
import com.example.Ecommerce.services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(
                categoryService.create(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {

        return ResponseEntity.ok(
                categoryService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                categoryService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(
                categoryService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        categoryService.delete(id);

        return ResponseEntity.ok("Category deleted successfully");
    }
}