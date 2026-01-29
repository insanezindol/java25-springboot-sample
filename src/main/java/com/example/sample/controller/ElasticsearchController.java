package com.example.sample.controller;

import com.example.sample.domain.ProductDoc;
import com.example.sample.service.ElasticsearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "상품 관리 API(elasticsearch)", description = "상품 관리 elasticsearch CRUD")
@Slf4j
@RestController
@RequestMapping("/api/v1/elasticsearch")
@RequiredArgsConstructor
public class ElasticsearchController {

    private final ElasticsearchService elasticsearchService;

    @Operation(summary = "상품 상세 조회", description = "상품 상세를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDoc> findByProductId(@PathVariable String id) {
        return ResponseEntity.ok(elasticsearchService.findById(id));
    }

    @Operation(summary = "상품 이름 조회", description = "상품 이름으로 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<ProductDoc>> searchProduct(@RequestParam String name) {
        return ResponseEntity.ok(elasticsearchService.searchByName(name));
    }

    @Operation(summary = "상품 생성", description = "상품을 생성합니다.")
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductDoc doc) {
        return ResponseEntity.ok(elasticsearchService.saveProduct(doc));
    }

    @Operation(summary = "상품 수정", description = "상품을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable String id, @RequestBody ProductDoc doc) {
        return ResponseEntity.ok(elasticsearchService.updateProduct(id, doc));
    }

    @Operation(summary = "상품 삭제", description = "상품을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductDoc> deleteProduct(@PathVariable String id) {
        elasticsearchService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
