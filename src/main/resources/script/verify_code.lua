-- return  1 : 일치 → 삭제함
--         0 : 불일치 (재시도할 수 있도록 남겨둠)
--        -1 : 키 없음 (만료되었거나 요청한 적 없음)
local saved = redis.call('GET', KEYS[1])
if not saved then
    return -1
end
if saved == ARGV[1] then
    redis.call('DEL', KEYS[1])
    return 1
end
return 0