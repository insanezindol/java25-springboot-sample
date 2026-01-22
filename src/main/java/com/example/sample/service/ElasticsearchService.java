package com.example.sample.service;

import com.example.sample.domain.ProductDoc;
import com.example.sample.repository.ElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticsearchService {

    private final ElasticsearchRepository elasticsearchRepository;

    // Create
    public String saveProduct(ProductDoc doc) {
        doc.setCreatedAt();
        return elasticsearchRepository.save(doc).getId();
    }

    // Update
    public String updateProduct(Long id, ProductDoc doc) {
        ProductDoc existingDoc = elasticsearchRepository.findById(String.valueOf(id)).orElseThrow();
        existingDoc.update(doc.getName(), doc.getPrice());
        return elasticsearchRepository.save(existingDoc).getId();
    }

    // Read
    public ProductDoc findById(String id) {
        return elasticsearchRepository.findById(id).orElseThrow();
    }

    // Search (Keyword)
    public List<ProductDoc> searchByName(String name) {
        return elasticsearchRepository.findByNameContaining(name);
    }

    // Delete
    public void delete(String id) {
        elasticsearchRepository.deleteById(id);
    }

}
