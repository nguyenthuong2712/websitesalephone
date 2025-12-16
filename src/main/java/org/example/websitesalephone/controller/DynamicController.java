package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.dynamic.DynamicRequest;
import org.example.websitesalephone.dto.dynamic.DynamicSearch;
import org.example.websitesalephone.service.dyanmic.DynamicAttributeProductService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dynamic")
@RequiredArgsConstructor
public class DynamicController {

    private final DynamicAttributeProductService dynamicService;

    @PostMapping("/search")
    public CommonResponse getAll(@RequestBody DynamicSearch dynamicSearch) {
        return dynamicService.getALl(dynamicSearch);
    }

    @PostMapping("/create")
    public CommonResponse create(@RequestBody DynamicRequest dynamicRequest) {
        return dynamicService.created(dynamicRequest);
    }

    @PutMapping("/update")
    public CommonResponse update(@RequestBody DynamicRequest dynamicRequest) {
        return dynamicService.updated(dynamicRequest);
    }

    @DeleteMapping("/delete")
    public CommonResponse delete(@RequestBody DynamicRequest dynamicRequest) {
        return dynamicService.deleted(dynamicRequest);
    }
}
