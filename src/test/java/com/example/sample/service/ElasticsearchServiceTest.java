package com.example.sample.service;

import com.example.sample.domain.ProductDoc;
import com.example.sample.repository.ElasticsearchProductRepository;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ElasticsearchServiceTest {

    static final String _ID  = "1";
    static final String _NAME = "의류";
    static final String _CATEGORY = "cloth";
    static final Double _PRICE = 10000.0;
    static final String _DESCRIPTION = "description";

    ElasticsearchService elasticsearchService;
    FixtureMonkey fixtureMonkey;

    @Mock
    ElasticsearchProductRepository elasticsearchProductRepository;

    @BeforeEach
    void setup() {
        this.elasticsearchService = new ElasticsearchService(elasticsearchProductRepository);
        this.fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE) // Builder 기반으로 생성
                .build();
    }

    @Test
    @DisplayName("상품 저장")
    void saveProduct() {
        // given
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), _ID)
                .set(javaGetter(ProductDoc::getName), _NAME)
                .set(javaGetter(ProductDoc::getCategory), _CATEGORY)
                .set(javaGetter(ProductDoc::getPrice), _PRICE)
                .set(javaGetter(ProductDoc::getDescription), _DESCRIPTION)
                .sample();
        when(elasticsearchProductRepository.save(any())).thenReturn(productDoc);

        // when
        String savedId = elasticsearchService.saveProduct(productDoc);

        // then
        Assertions.assertNotNull(savedId);
        Assertions.assertEquals(productDoc.getId(), savedId);
        verify(elasticsearchProductRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("상품 수정")
    void updateProduct() {
        // given
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), _ID)
                .set(javaGetter(ProductDoc::getName), _NAME)
                .set(javaGetter(ProductDoc::getCategory), _CATEGORY)
                .set(javaGetter(ProductDoc::getPrice), _PRICE)
                .set(javaGetter(ProductDoc::getDescription), _DESCRIPTION)
                .sample();
        when(elasticsearchProductRepository.findById(any())).thenReturn(Optional.of(productDoc));
        when(elasticsearchProductRepository.save(any())).thenReturn(productDoc);

        // when
        String updatedId = elasticsearchService.updateProduct(_ID, productDoc);

        // then
        Assertions.assertNotNull(updatedId);
        Assertions.assertEquals(productDoc.getId(), updatedId);
        verify(elasticsearchProductRepository, times(1)).findById(any());
        verify(elasticsearchProductRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("상품 아이디 검색")
    void findById() {
        // given
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), _ID)
                .set(javaGetter(ProductDoc::getName), _NAME)
                .set(javaGetter(ProductDoc::getCategory), _CATEGORY)
                .set(javaGetter(ProductDoc::getPrice), _PRICE)
                .set(javaGetter(ProductDoc::getDescription), _DESCRIPTION)
                .sample();
        when(elasticsearchProductRepository.findById(any())).thenReturn(Optional.of(productDoc));

        // when
        ProductDoc searchProduct = elasticsearchService.findById(_ID);

        // then
        Assertions.assertNotNull(searchProduct);
        Assertions.assertEquals(productDoc.getId(), searchProduct.getId());
        Assertions.assertEquals(productDoc.getName(), searchProduct.getName());
        verify(elasticsearchProductRepository, times(1)).findById(any());
    }

    @Test
    @DisplayName("상품 이름 검색")
    void searchByName() {
        // given
        final int docSize = 5;
        List<ProductDoc> productDocs = fixtureMonkey.giveMe(ProductDoc.class, docSize);
        when(elasticsearchProductRepository.findByNameContaining(any())).thenReturn(productDocs);

        // when
        List<ProductDoc> searchProducts = elasticsearchService.searchByName(_NAME);

        // then
        Assertions.assertNotNull(searchProducts);
        Assertions.assertEquals(docSize, searchProducts.size());
        verify(elasticsearchProductRepository, times(1)).findByNameContaining(any());
    }

    @Test
    @DisplayName("상품 삭제")
    void delete() {
        // given
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class).sample();
        when(elasticsearchProductRepository.findById(any())).thenReturn(Optional.of(productDoc));

        // when
        elasticsearchService.delete(_ID);

        // then
        verify(elasticsearchProductRepository, times(1)).findById(any());
        verify(elasticsearchProductRepository, times(1)).delete(any());
    }

}