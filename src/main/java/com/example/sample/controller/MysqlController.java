package com.example.sample.controller;

import com.example.sample.domain.User;
import com.example.sample.dto.UserRequestDto;
import com.example.sample.service.MysqlService;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "사용자 관리 API(MySQL)", description = "사용자 관리 MySQL CRUD")
@Slf4j
@RestController
@RequestMapping("/api/v1/mysql")
@RequiredArgsConstructor
public class MysqlController {

    private final MysqlService mysqlService;
    private final static String _METRIC_NAME = "api_v1_mysql_controller";

    @Operation(summary = "사용자 목록 조회", description = "사용자 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<User>> list() {
        // add metric
        List<Tag> tags = List.of(Tag.of("method_name", "list"));
        Metrics.counter(_METRIC_NAME, tags).increment();

        return ResponseEntity.ok(mysqlService.findAll());
    }

    @Operation(summary = "사용자 상세 조회", description = "사용자 상세를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<User> detail(@PathVariable Long id) {
        // add metric
        List<Tag> tags = List.of(Tag.of("method_name", "detail"), Tag.of("id", String.valueOf(id)));
        Metrics.counter(_METRIC_NAME, tags).increment();

        return ResponseEntity.ok(mysqlService.findOne(id));
    }

    @Operation(summary = "사용자 생성", description = "사용자를 생성합니다.")
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody UserRequestDto dto) {
        // add metric
        List<Tag> tags = List.of(Tag.of("method_name", "create"));
        Metrics.counter(_METRIC_NAME, tags).increment();

        return ResponseEntity.ok(mysqlService.save(dto));
    }

    @Operation(summary = "사용자 수정", description = "사용자를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @RequestBody UserRequestDto dto) {
        // add metric
        List<Tag> tags = List.of(Tag.of("method_name", "update"), Tag.of("id", String.valueOf(id)));
        Metrics.counter(_METRIC_NAME, tags).increment();

        return ResponseEntity.ok(mysqlService.update(id, dto));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // add metric
        List<Tag> tags = List.of(Tag.of("method_name", "delete"), Tag.of("id", String.valueOf(id)));
        Metrics.counter(_METRIC_NAME, tags).increment();

        mysqlService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
