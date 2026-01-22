package com.example.sample.repository;

import com.example.sample.domain.ProductDoc;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticsearchRepository extends org.springframework.data.elasticsearch.repository.ElasticsearchRepository<ProductDoc, String> {
    List<ProductDoc> findByNameContaining(String name);
}
