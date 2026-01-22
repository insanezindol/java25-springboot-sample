package com.example.sample.controller;

import com.example.sample.domain.ProductDoc;
import com.example.sample.service.ElasticsearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elasticsearch")
@RequiredArgsConstructor
public class ElasticsearchController {

    private final ElasticsearchService elasticsearchService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody ProductDoc doc) {
        return ResponseEntity.ok(elasticsearchService.saveProduct(doc));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDoc> detail(@PathVariable String id) {
        return ResponseEntity.ok(elasticsearchService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDoc>> search(@RequestParam String name) {
        return ResponseEntity.ok(elasticsearchService.searchByName(name));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody ProductDoc doc) {
        return ResponseEntity.ok(elasticsearchService.updateProduct(id, doc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDoc> delete(@PathVariable String id) {
        elasticsearchService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
