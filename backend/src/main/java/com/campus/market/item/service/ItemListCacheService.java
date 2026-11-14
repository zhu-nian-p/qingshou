package com.campus.market.item.service;

import com.campus.market.common.PageResult;
import com.campus.market.item.dto.ItemListVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 首页商品列表 Redis 缓存。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>缓存 key 带「版本号」，商品发布/编辑/下架/成交/置顶时版本 +1，旧缓存自然失效，避免逐个删 key；</li>
 *   <li>空结果也缓存 60 秒，防止恶意参数反复穿透到 MySQL（缓存穿透）；</li>
 *   <li>正常结果 TTL 5 分钟 + 随机 0~60 秒，避免同一时刻大量缓存一起过期（缓存雪崩）；</li>
 *   <li>Redis 不可用时直接返回 null，调用方回退数据库，不影响主流程。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemListCacheService {

    private static final String VERSION_KEY = "qingshou:item:list:version";
    private static final String KEY_PREFIX = "qingshou:item:list:";
    private static final Duration NORMAL_TTL = Duration.ofMinutes(5);
    private static final Duration EMPTY_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /** 当前缓存版本号 */
    private String version() {
        try {
            String v = redisTemplate.opsForValue().get(VERSION_KEY);
            return v == null ? "0" : v;
        } catch (Exception e) {
            return "0";
        }
    }

    /** 商品数据变更后调用：版本号 +1，所有列表缓存立即失效 */
    public void evictAll() {
        try {
            redisTemplate.opsForValue().increment(VERSION_KEY);
        } catch (Exception e) {
            log.warn("清空商品列表缓存失败（Redis 不可用）: {}", e.getMessage());
        }
    }

    public PageResult<ItemListVO> get(String keyword, String category, String campus,
                                      String sort, long page, long size) {
        String key = buildKey(keyword, category, campus, sort, page, size);
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, new TypeReference<PageResult<ItemListVO>>() {});
        } catch (Exception e) {
            log.warn("读取商品列表缓存失败，回退数据库: {}", e.getMessage());
            return null;
        }
    }

    public void put(String keyword, String category, String campus, String sort,
                    long page, long size, PageResult<ItemListVO> result) {
        String key = buildKey(keyword, category, campus, sort, page, size);
        boolean empty = result == null || result.getRecords() == null || result.getRecords().isEmpty();
        Duration ttl = empty
                ? EMPTY_TTL
                : NORMAL_TTL.plusSeconds(ThreadLocalRandom.current().nextInt(0, 60));
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), ttl);
        } catch (Exception e) {
            log.warn("写入商品列表缓存失败（忽略）: {}", e.getMessage());
        }
    }

    private String buildKey(String keyword, String category, String campus,
                            String sort, long page, long size) {
        String raw = String.join("|",
                version(),
                keyword == null ? "" : keyword.trim(),
                category == null ? "" : category.trim(),
                campus == null ? "" : campus.trim(),
                sort == null ? "" : sort.trim(),
                String.valueOf(page),
                String.valueOf(size));
        // 用 MD5 压缩 key 长度，避免关键词很长时 key 过大
        String hash = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));
        return KEY_PREFIX + hash;
    }
}
