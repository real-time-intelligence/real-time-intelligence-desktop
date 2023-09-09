package ru.rti.desktop.config;

import dagger.Binds;
import dagger.Module;
import ru.rti.desktop.cache.AppCache;
import ru.rti.desktop.cache.impl.AppCacheImpl;


import javax.inject.Named;

@Module
public abstract class CacheConfig {

    @Binds
    @Named("appCache")
    public abstract AppCache bindAppCache(AppCacheImpl appCache);
}
