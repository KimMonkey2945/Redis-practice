package com.study.redis.pubsub;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoticePageController {

    @GetMapping("/pub_sub")
    public String index() {
        return "forward:/pub_sub.html";
    }

}