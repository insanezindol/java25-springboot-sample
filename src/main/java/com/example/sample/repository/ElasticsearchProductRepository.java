package com.example.sample.repository;

import com.example.sample.domain.ProductDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticsearchProductRepository extends ElasticsearchRepository<ProductDoc, String> {
    List<ProductDoc> findByNameContaining(String name);
}
