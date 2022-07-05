package com.xuxd.kafka.console.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import kafka.console.KafkaConsole;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TimeBasedCache<K, V> {
    private LoadingCache<K, V> cache;

    private KafkaConsole console;

    public TimeBasedCache(CacheLoader<K, V> loader, RemovalListener<K, V> listener) {
        cache = CacheBuilder.newBuilder()
                .maximumSize(50)                             // maximum 100 records can be cached
                .expireAfterAccess(30, TimeUnit.MINUTES)      // cache will expire after 30 minutes of access
                .removalListener(listener)
                .build(loader);

    }

    public V get(K k) {
        try {
            return cache.get(k);
        } catch (ExecutionException e) {
            throw new RuntimeException("Get connection from cache error.", e);
        }
    }
}
