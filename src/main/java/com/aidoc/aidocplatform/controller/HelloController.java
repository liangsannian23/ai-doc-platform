package com.aidoc.aidocplatform.controller;


import com.aidoc.aidocplatform.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器：提供一个无需鉴权的探活接口，用于确认服务已启动。
 */
@RestController
public class HelloController {

    /**
     * 服务探活。
     *
     * @return 启动成功提示语
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Java+AI智能文档协作平台启动成功！");
    }
}