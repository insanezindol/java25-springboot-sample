package com.example.sample.integration;

import com.example.sample.domain.ProductDoc;
import com.example.sample.domain.User;
import com.example.sample.dto.RedisUserDto;
import com.example.sample.dto.UserRequestDto;
import com.example.sample.service.ElasticsearchService;
import com.example.sample.service.KafkaProducerService;
import com.example.sample.service.MysqlService;
import com.example.sample.service.RedisService;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.navercorp.fixturemonkey.api.experimental.JavaGetterMethodPropertySelector.javaGetter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Java25SpringbootIT {

    static final Long _ID = 1L;
    static final String _NAME = "DEAN";
    static final String _USERNAME = "DEAN";
    static final String _EMAIL = "dean@test.com";
    static final int _AGE = 30;
    static final String[] _ITEMS = {"100", "200", "300"};
    static final String _PRD_ID = "PRD-100";
    static final String _CATEGORY = "cloth";
    static final Double _PRICE = 10000.0;
    static final String _DESCRIPTION = "description";

    @Autowired
    MysqlService mysqlService;

    @Autowired
    RedisService redisService;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Autowired
    ElasticsearchService elasticsearchService;

    static FixtureMonkey fixtureMonkey;

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withInitScript("init-db.sql")
            .withDatabaseName("appdb")
            .withUsername("test")
            .withPassword("test")
            .withEnv("MYSQL_ROOT_PASSWORD", "test")
            .withReuse(true)
            .waitingFor(Wait.forListeningPort());

    @Container
    static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.17.28")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withEnv("node.name", "local-node")
            .withEnv("cluster.name", "local-cluster")
            .withEnv("xpack.security.enabled", "false")
            .withReuse(true)
            .waitingFor(Wait.forListeningPort());

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.0"))
            .withReuse(true)
            .waitingFor(Wait.forListeningPort());

    @Container
    static GenericContainer redisContainer = new GenericContainer(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정
        registry.add("spring.datasource.driver-class-name", mySQLContainer::getDriverClassName);
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);

        // Elasticsearch 설정
        registry.add("spring.elasticsearch.uris", () -> "http://" + elasticsearchContainer.getHost() + ":" + elasticsearchContainer.getMappedPort(9200));
        registry.add("spring.elasticsearch.connection-timeout", () -> "60s");
        registry.add("spring.elasticsearch.socket-timeout", () -> "60s");

        // Kafka 설정
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        // Redis 설정
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redisContainer.getMappedPort(6379)));
        registry.add("spring.data.redis.timeout", () -> "60s");
    }

    @BeforeAll
    static void beforeAll() {
        mySQLContainer.start();
        elasticsearchContainer.start();
        kafkaContainer.start();
        redisContainer.start();
        fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE) // Builder 기반으로 생성
                .build();
    }

    @AfterAll
    static void afterAll() {
        mySQLContainer.stop();
        elasticsearchContainer.stop();
        kafkaContainer.stop();
        redisContainer.stop();
    }

    /**
     * TestContainer 정상 체크
     */

    @Test
    @Order(1)
    @DisplayName("check mysql containers")
    void checkMysqlContainers() {
        log.info("getJdbcDriverInstance: {} ", mySQLContainer.getJdbcDriverInstance());
        log.info("getJdbcUrl: {} ", mySQLContainer.getJdbcUrl());
        log.info("getMappedPort: {} ", mySQLContainer.getMappedPort(3306));
        log.info("getHost: {} ", mySQLContainer.getHost());
        log.info("getUsername: {} ", mySQLContainer.getUsername());
        log.info("getPassword: {} ", mySQLContainer.getPassword());
        Assertions.assertEquals("localhost", mySQLContainer.getHost());
    }

    @Test
    @Order(2)
    @DisplayName("check elasticsearch containers")
    void checkElasticsearchContainers() {
        log.info("getImage: {} ", elasticsearchContainer.getImage());
        log.info("getHost: {} ", elasticsearchContainer.getHost());
        log.info("getHttpHostAddress: {} ", elasticsearchContainer.getHttpHostAddress());
        log.info("getEnv: {} ", elasticsearchContainer.getEnv());
        Assertions.assertEquals("localhost", elasticsearchContainer.getHost());
    }


    @Test
    @Order(3)
    @DisplayName("check kafka containers")
    void checkKafkaContainers() {
        log.info("getImage: {} ", kafkaContainer.getImage());
        log.info("getHost: {} ", kafkaContainer.getHost());
        log.info("getBootstrapServers: {} ", kafkaContainer.getBootstrapServers());
        log.info("getEnv: {} ", kafkaContainer.getEnv());
        Assertions.assertEquals("localhost", kafkaContainer.getHost());
    }

    @Test
    @Order(4)
    @DisplayName("check redis containers")
    void checkRedisContainers() {
        log.info("getImage: {} ", redisContainer.getImage());
        log.info("getHost: {} ", redisContainer.getHost());
        log.info("getMappedPort: {} ", redisContainer.getMappedPort(6379));
        Assertions.assertEquals("localhost", redisContainer.getHost());
    }

    /**
     * MySQL 테스트
     */
    @Test
    @Order(10)
    @DisplayName("사용자 추가")
    void addUser() {
        // given
        UserRequestDto userRequestDto = fixtureMonkey.giveMeBuilder(UserRequestDto.class)
                .set(javaGetter(UserRequestDto::name), _NAME)
                .set(javaGetter(UserRequestDto::email), _EMAIL)
                .sample();

        // when
        Long id = mysqlService.save(userRequestDto);

        // then
        Assertions.assertNotNull(id);
        Assertions.assertEquals(id, 1L);
    }

    @Test
    @Order(11)
    @DisplayName("사용자 전체 검색")
    void findAll() {
        // when
        List<User> list = mysqlService.findAll();

        // then
        Assertions.assertNotNull(list);
        Assertions.assertEquals(1, list.size());
        assertThat(list).extracting("name", "email").contains(tuple(_NAME, _EMAIL));
    }

    @Test
    @Order(12)
    @DisplayName("사용자 아이디 검색")
    void findOne() {
        // when
        User user = mysqlService.findOne(_ID);

        // then
        Assertions.assertNotNull(user);
        Assertions.assertEquals(_NAME, user.getName());
    }

    @Test
    @Order(13)
    @DisplayName("사용자 수정")
    void update() {
        // given
        final String modifyName = "NEW NAME";
        final String modifyEmail = "NEWMAIL@test.com";
        UserRequestDto userRequestDto = new UserRequestDto(modifyName, modifyEmail);

        // when
        User savedUser = mysqlService.update(_ID, userRequestDto);

        // then
        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(modifyName, savedUser.getName());
        Assertions.assertEquals(modifyEmail, savedUser.getEmail());
    }

    @Test
    @Order(14)
    @DisplayName("사용자 삭제")
    void delete() {
        // when
        mysqlService.delete(_ID);
        List<User> list = mysqlService.findAll();

        // then
        assertEquals(0, list.size());
    }

    /**
     * kafka 테스트
     */
    @Test
    @Order(20)
    @DisplayName("카프카 publish")
    void publish() throws Exception {
        // when
        final Long userId = 1L;
        final String action = "TEST_ACTION";

        // when-then - 예외가 발생하지 않는지만 확인
        assertDoesNotThrow(() -> kafkaProducerService.sendMessage(userId, action));

        // 잠시 대기 (메시지 전송 완료 보장)
        Thread.sleep(1000);
        log.info("카프카 메시지 전송 완료 - userId: {}, action: {}", userId, action);
    }

    /**
     * Redis 테스트
     */
    @Test
    @Order(30)
    @DisplayName("사용자 생성")
    void createUser() {
        // given
        final RedisUserDto redisUserDto = fixtureMonkey.giveMeBuilder(RedisUserDto.class)
                .set(javaGetter(RedisUserDto::id), String.valueOf(_ID))
                .set(javaGetter(RedisUserDto::username), _USERNAME)
                .set(javaGetter(RedisUserDto::email), _EMAIL)
                .set(javaGetter(RedisUserDto::age), _AGE)
                .sample();

        // when
        RedisUserDto savedUser = redisService.createUser(redisUserDto);

        // then
        Assertions.assertNotNull(savedUser);
        Assertions.assertEquals(redisUserDto.id(), savedUser.id());
        Assertions.assertEquals(redisUserDto.username(), savedUser.username());
        Assertions.assertEquals(redisUserDto.email(), savedUser.email());
        Assertions.assertEquals(redisUserDto.age(), savedUser.age());
    }

    @Test
    @Order(31)
    @DisplayName("사용자 ID 조회")
    void getUserById() {
        // when
        Object returnObject = redisService.getUserById(String.valueOf(_ID));

        // then
        Assertions.assertNotNull(returnObject);
    }

    @Test
    @Order(32)
    @DisplayName("사용자 삭제")
    void deleteUser() {
        // when
        redisService.deleteUser(String.valueOf(_ID));
        Object returnObject = redisService.getUserById(String.valueOf(_ID));

        // then
        Assertions.assertNull(returnObject);
    }

    @Test
    @Order(33)
    @DisplayName("키 패턴 조회")
    void searchKeys() {
        // given
        final RedisUserDto redisUserDto = fixtureMonkey.giveMeOne(RedisUserDto.class);
        final String pattern = "*";

        // when
        redisService.createUser(redisUserDto);
        Set<String> keySet = redisService.searchKeys(pattern);

        // then
        Assertions.assertNotNull(keySet);
    }

    @Test
    @Order(34)
    @DisplayName("최근상품 추가")
    void addToRecentItems() {
        // then - 예외가 발생하지 않는지만 확인
        assertDoesNotThrow(() -> redisService.addToRecentItems(String.valueOf(_ID), _PRD_ID));
    }

    @Test
    @Order(35)
    @DisplayName("최근상품 조회")
    void getRecentItems() {
        // when
        List<Object> recentItems = redisService.getRecentItems(String.valueOf(_ID));

        // then
        Assertions.assertEquals(1, recentItems.size());
    }

    @Test
    @Order(36)
    @DisplayName("장바구니 추가")
    void addToCart() {
        // when-then - 예외가 발생하지 않는지만 확인
        assertDoesNotThrow(() -> redisService.addToCart(String.valueOf(_ID), _ITEMS));
    }

    @Test
    @Order(37)
    @DisplayName("장바구니 조회")
    void getCart() {
        // when
        Set<Object> carts = redisService.getCart(String.valueOf(_ID));

        // then
        Assertions.assertEquals(_ITEMS.length, carts.size());
    }

    @Test
    @Order(38)
    @DisplayName("장바구니 삭제")
    void removeFromCart() {
        // when
        redisService.removeFromCart(String.valueOf(_ID), _ITEMS);
        Set<Object> carts = redisService.getCart(String.valueOf(_ID));

        // then
        Assertions.assertEquals(0, carts.size());
    }

    @Test
    @Order(39)
    @DisplayName("조회수 증가")
    void incrementViewCount() {
        // when
        Long resultViewCount = redisService.incrementViewCount(_PRD_ID);

        // then
        Assertions.assertEquals(1L, resultViewCount);
    }

    @Test
    @Order(40)
    @DisplayName("조회수 가져오기")
    void getViewCount() {
        // when
        redisService.incrementViewCount(_PRD_ID);
        redisService.incrementViewCount(_PRD_ID);
        Long resultViewCount = redisService.getViewCount(_PRD_ID);

        // then
        Assertions.assertEquals(3L, resultViewCount);
    }

    /**
     * elasticsearch 테스트
     */
    @Test
    @Order(50)
    @DisplayName("상품 저장")
    void saveProduct() {
        // given
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), String.valueOf(_ID))
                .set(javaGetter(ProductDoc::getName), _NAME)
                .set(javaGetter(ProductDoc::getCategory), _CATEGORY)
                .set(javaGetter(ProductDoc::getPrice), _PRICE)
                .set(javaGetter(ProductDoc::getDescription), _DESCRIPTION)
                .sample();

        // when
        String savedId = elasticsearchService.saveProduct(productDoc);

        // then
        Assertions.assertNotNull(savedId);
        Assertions.assertEquals(String.valueOf(_ID), savedId);
    }

    @Test
    @Order(51)
    @DisplayName("상품 수정")
    void updateProduct() {
        // given
        final String updateName = "updateName";
        final Double updatePrice = 20000.0;
        ProductDoc productDoc = fixtureMonkey.giveMeBuilder(ProductDoc.class)
                .set(javaGetter(ProductDoc::getId), String.valueOf(_ID))
                .set(javaGetter(ProductDoc::getName), updateName)
                .set(javaGetter(ProductDoc::getCategory), _CATEGORY)
                .set(javaGetter(ProductDoc::getPrice), updatePrice)
                .set(javaGetter(ProductDoc::getDescription), _DESCRIPTION)
                .sample();

        // when
        String updatedId = elasticsearchService.updateProduct(String.valueOf(_ID), productDoc);

        // then
        Assertions.assertNotNull(updatedId);
        Assertions.assertEquals(String.valueOf(_ID), updatedId);
    }

    @Test
    @Order(52)
    @DisplayName("상품 아이디 검색")
    void findById() {
        // given
        final String updateName = "updateName";
        final Double updatePrice = 20000.0;

        // when
        ProductDoc searchProduct = elasticsearchService.findById(String.valueOf(_ID));

        // then
        Assertions.assertNotNull(searchProduct);
        Assertions.assertEquals(String.valueOf(_ID), searchProduct.getId());
        Assertions.assertEquals(updateName, searchProduct.getName());
        Assertions.assertEquals(_CATEGORY, searchProduct.getCategory());
        Assertions.assertEquals(updatePrice, searchProduct.getPrice());
        Assertions.assertEquals(_DESCRIPTION, searchProduct.getDescription());
    }

    @Test
    @Order(53)
    @DisplayName("상품 이름 검색")
    void searchByName() {
        // given
        final String updateName = "updateName";

        // when
        List<ProductDoc> searchProducts = elasticsearchService.searchByName(updateName);

        // then
        Assertions.assertNotNull(searchProducts);
        Assertions.assertEquals(1, searchProducts.size());
    }

    @Test
    @Order(54)
    @DisplayName("상품 삭제")
    void deleteForElasticsearch() {
        // when
        elasticsearchService.delete(String.valueOf(_ID));

        // then
        assertThrows(NoSuchElementException.class, () -> {
            elasticsearchService.findById(String.valueOf(_ID));
        });
    }

}
