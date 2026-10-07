package com.study.redis.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final StringRedisTemplate redisTemplate;

    @PostMapping("items/{itemId}")
    public Map<String, Object> setItem(@PathVariable String itemId, @RequestParam String value){

        String key = "item:" + itemId;
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, 10, TimeUnit.SECONDS);

        return Map.of("massage", "Data saved to Redis successfully", "key", key, "value", value);

    }

    @GetMapping("/items/{itemId}")
    public Map<String, Object> getItem(@PathVariable String itemId){
        String key = "item:" + itemId;
        String result = redisTemplate.opsForValue().get(key);

        if(result == null){
            return Map.of("massage", "Item not fount in Redis");
        }
        return Map.of("key", key, "value", result);
    }

}
