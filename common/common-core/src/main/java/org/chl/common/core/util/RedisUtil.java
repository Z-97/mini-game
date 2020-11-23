package org.chl.common.core.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author liubo
 * @Description //TODO redis操作
 * @Date 17:23 2019/9/30
 */
@Slf4j
@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate redisTemplate;


    public Set<String> keys(String keys) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return redisTemplate.keys(keys);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * @Author liubo
     * @Description 批量获取//TODO
     * @Date 16:01 2020/3/13
     **/
    public List<String> multiGet(List<String> keys) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return operations.multiGet(keys);
    }

    /**
     * @Author
     * @Description //TODO  指定缓存失效时间
     * @Date 17:25 2019/9/30
     **/
    public boolean expire(String key, long time) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO  指定缓存失效时间
     * @Date 17:25 2019/9/30
     **/
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            if (time > 0) {
                redisTemplate.expire(key, time, timeUnit);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 根据key 获取过期时间 时间(秒) 返回0代表为永久有效
     * @Date 17:25 2019/9/30
     **/
    public long getExpire(String key) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * @Author
     * @Description //TODO 判断key是否存在 true:存在 false:不存在
     * @Date 17:25 2019/9/30
     **/
    public boolean hasKey(String key) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 删除缓存 可以传一个值 或多个
     * @Date 17:27 2019/9/30
     **/
    public void del(String... key) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * @Author liubo
     * @Description 删除缓存//TODO
     * @Date 12:00 2020/3/20
     **/
    public void del(Set<String> keys) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.delete(keys);
    }

    /**
     * @Author
     * @Description //TODO 普通缓存获取
     * @Date 17:27 2019/9/30
     **/
    public Object get(String key) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            return key == null ? null : redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * @Author
     * @Description //TODO 普通缓存放入
     * @Date 17:27 2019/9/30
     **/
    public boolean set(String key, Object value) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 普通缓存放入并设置时间  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @Date 17:27 2019/9/30
     **/
    public boolean set(String key, Object value, long time) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author liubo
     * @Description //TODO
     * @Date 11:57 2019/11/8
     **/
    public boolean set(String key, Object value, long time, TimeUnit timeUnit) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 递增 要增加几(大于0)
     * @Date 17:28 2019/9/30
     **/
    public Double incr(String key, Double delta) {
        try {
            if (delta < 0) {
                throw new RuntimeException("递增因子必须大于0");
            }
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            return operations.increment(key, delta);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public Long incr(String key, Long delta) {
        try {
            if (delta < 0) {
                throw new RuntimeException("递增因子必须大于0");
            }
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            return operations.increment(key, delta);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * @Author
     * @Description //TODO 递减 要减少几(小于0)
     * @Date 17:29 2019/9/30
     **/
    public Double decr(String key, Double delta) {
        try {
            if (delta < 0) {
                throw new RuntimeException("递减因子必须大于0");
            }
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            return operations.increment(key, -delta);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    public Long decr(String key, Long delta) {
        try {
            if (delta < 0) {
                throw new RuntimeException("递减因子必须大于0");
            }
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            return operations.increment(key, -delta);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }


    /**
     * @Author
     * @Description //TODO HashGet
     * @Date 17:30 2019/9/30
     **/
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * @Author
     * @Description //TODO 获取hashKey对应的所有键值
     * @Date 17:30 2019/9/30
     **/
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * @Author
     * @Description //TODO HashSet
     * @Date 17:30 2019/9/30
     **/
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO HashSet 并设置时间
     * @Date 17:30 2019/9/30
     **/
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 向一张hash表中放入数据,如果不存在将创建
     * @Date 17:34 2019/9/30
     **/
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 向一张hash表中放入数据,如果不存在将创建(时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间)
     * @Date 17:35 2019/9/30
     **/
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * @Author
     * @Description //TODO 删除hash表中的值
     * @Date 17:35 2019/9/30
     **/
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * @Author
     * @Description //TODO 判断hash表中是否有该项的值
     * @Date 17:35 2019/9/30
     **/
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * @Author
     * @Description //TODO hash递增 如果不存在,就会创建一个 并把新增后的值返回
     * @Date 17:35 2019/9/30
     **/
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * @Author liubo
     * @Description //TODO hash递减 要减少记(小于0)
     * @Date 17:36 2019/9/30
     **/
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
                expire(key, time);
            return count;
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }
    // ===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 获取list缓存的内容并删除
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     * @return
     */
    public synchronized List<Object> lGetAndTrim(String key, long start, long end) {
        try {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            List<Object> data = redisTemplate.opsForList().range(key, start, end);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.opsForList().trim(key, data.size(), -1);
            return data;
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
                expire(key, time);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            log.error("", e);
            return 0;
        }
    }

    /**
     * 删除list首尾，只保留 [start, end] 之间的值
     *
     * @param key
     * @param start
     * @param end
     */
    public void lTrim(String key, Integer start, Integer end) {
        try {
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.opsForList().trim(key, start, end);
        } catch (Exception e) {
            log.error("", e);
        }

    }
}
