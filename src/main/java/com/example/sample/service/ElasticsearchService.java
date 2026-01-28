package com.example.sample.service;

import com.example.sample.domain.ProductDoc;
import com.example.sample.repository.ElasticsearchProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchProductRepository elasticsearchProductRepository;

    // Create
    public String saveProduct(ProductDoc doc) {
        doc.setCreatedAt();
        return elasticsearchProductRepository.save(doc).getId();
    }

    // Update
    public String updateProduct(String id, ProductDoc doc) {
        ProductDoc existingDoc = elasticsearchProductRepository.findById(id).orElseThrow();
        existingDoc.update(doc.getName(), doc.getPrice());
        return elasticsearchProductRepository.save(existingDoc).getId();
    }

    // Read
    public ProductDoc findById(String id) {
        return elasticsearchProductRepository.findById(id).orElseThrow();
    }

    // Search (Keyword)
    public List<ProductDoc> searchByName(String name) {
        return elasticsearchProductRepository.findByNameContaining(name);
    }

    // Delete
    public void delete(String id) {
        ProductDoc existingDoc = elasticsearchProductRepository.findById(id).orElse(null);
        Optional.ofNullable(existingDoc)
                .ifPresent(product -> {
                    elasticsearchProductRepository.delete(product);
                });
    }

}
