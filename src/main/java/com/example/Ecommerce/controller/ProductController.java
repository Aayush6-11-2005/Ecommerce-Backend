package com.example.Ecommerce.controller;

import com.example.Ecommerce.dto.ProductRequest;
import com.example.Ecommerce.dto.ProductResponse;
import com.example.Ecommerce.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
                productService.create(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {

        return ResponseEntity.ok(
                productService.getAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                productService.getById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        productService.delete(id);

        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam String name) {

        return ResponseEntity.ok(
                productService.search(name)
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductResponse>> getByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                productService.getByCategory(categoryId)
        );
    }
}