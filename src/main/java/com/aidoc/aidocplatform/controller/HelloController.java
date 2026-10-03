package com.aidoc.aidocplatform.controller;


import com.aidoc.aidocplatform.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Java+AI智能文档协作平台启动成功！");
    }
}