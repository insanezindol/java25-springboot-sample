package com.example.sample.controller;

import com.example.sample.domain.ProductDoc;
import com.example.sample.service.ElasticsearchService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ElasticsearchControllerTest {

    static final String _ID  = "1";
    static final String _NAME = "의류";
    static final String _CATEGORY = "cloth";
    static final Double _PRICE = 10000.0;
    static final String _DESCRIPTION = "description";

    @Mock
    private ElasticsearchService elasticsearchService; // 가짜 객체 생성

    @InjectMocks
    private ElasticsearchController elasticsearchController; // Mock 객체를 주입받는 Controller

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(elasticsearchController).build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE) // Builder 기반으로 생성
                .build();
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void findByProductId() throws Exception {
        // given
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), _ID)
                .set(javaGetter(ProductDoc::getName), _NAME)
                .set(javaGetter(ProductDoc::getCategory), _CATEGORY)
                .set(javaGetter(ProductDoc::getPrice), _PRICE)
                .set(javaGetter(ProductDoc::getDescription), _DESCRIPTION)
                .sample();

        given(elasticsearchService.findById(_ID)).willReturn(productDoc);

        // when & then
        mockMvc.perform(get("/api/v1/elasticsearch/{id}", _ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(_ID));

        verify(elasticsearchService, times(1)).findById(_ID);
    }

    @Test
    @DisplayName("상품 이름으로 검색 성공")
    void searchProduct() throws Exception {
        // given
        String searchName = "의류";
        List<ProductDoc> products = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), "1")
                .set(javaGetter(ProductDoc::getName), "의류 상품1")
                .set(javaGetter(ProductDoc::getCategory), "cloth")
                .set(javaGetter(ProductDoc::getPrice), 15000.0)
                .set(javaGetter(ProductDoc::getDescription), "테스트 상품 설명1")
                .sampleList(3);

        // 상품명 설정
        products.get(0).update("의류 A", 10000.0);
        products.get(1).update("의류 B", 10000.0);
        products.get(2).update("의류 C", 10000.0);

        given(elasticsearchService.searchByName(searchName)).willReturn(products);

        // when & then
        mockMvc.perform(get("/api/v1/elasticsearch/search")
                        .param("name", searchName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("의류 A"))
                .andExpect(jsonPath("$[1].name").value("의류 B"))
                .andExpect(jsonPath("$[2].name").value("의류 C"));

        verify(elasticsearchService, times(1)).searchByName(searchName);
    }

    @Test
    @DisplayName("상품 생성 성공")
    void createProduct() throws Exception {
        // given
        ProductDoc requestDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .setNull(javaGetter(ProductDoc::getId)) // ID는 null로 (생성 시 자동 생성됨)
                .set(javaGetter(ProductDoc::getName), "새로운 상품")
                .set(javaGetter(ProductDoc::getCategory), "electronics")
                .set(javaGetter(ProductDoc::getPrice), 299000.0)
                .set(javaGetter(ProductDoc::getDescription), "새로 생성된 상품입니다.")
                .sample();

        String generatedId = "generated-12345";

        given(elasticsearchService.saveProduct(any(ProductDoc.class))).willReturn(generatedId);

        // when & then
        mockMvc.perform(post("/api/v1/elasticsearch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDoc)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string(generatedId));

        verify(elasticsearchService, times(1)).saveProduct(any(ProductDoc.class));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProduct() throws Exception {
        // given
        String productId = "product-123";
        ProductDoc updateDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getName), "수정된 상품명")
                .set(javaGetter(ProductDoc::getCategory), "updated-category")
                .set(javaGetter(ProductDoc::getPrice), 50000.0)
                .set(javaGetter(ProductDoc::getDescription), "수정된 설명입니다.")
                .sample();

        String resultMessage = "상품이 성공적으로 수정되었습니다: " + productId;

        given(elasticsearchService.updateProduct(eq(productId), any(ProductDoc.class))).willReturn(resultMessage);

        // when & then
        mockMvc.perform(put("/api/v1/elasticsearch/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept("text/plain;charset=UTF-8")
                        .content(objectMapper.writeValueAsString(updateDoc)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string(resultMessage));

        verify(elasticsearchService, times(1)).updateProduct(eq(productId), any(ProductDoc.class));
    }

    @Test
    @DisplayName("")
    void deleteProduct() throws Exception {
        // given
        String productId = "product-to-delete";

        // 서비스의 delete 메서드는 void이므로 doNothing 설정
        doNothing().when(elasticsearchService).delete(productId);

        // when & then
        mockMvc.perform(delete("/api/v1/elasticsearch/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent());

        verify(elasticsearchService, times(1)).delete(productId);
    }

}