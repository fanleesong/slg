package com.h13.slg.config.fetcher;

import com.h13.slg.config.BasicFetcher;
import com.h13.slg.config.cache.RoleCache;
import com.h13.slg.config.co.RoleCO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class RoleConfigFetcher implements BasicFetcher<RoleCO> {

    @Autowired
    RoleCache cache;

    @Override
    public RoleCO get(String key) {
        return cache.get(key);
    }


    public Long getFromZhaoxian(int index, String quality) {
        return cache.getFromZhaoxian(index, quality);
    }


    public long getZhaoxianSize(String quality) {
        return cache.getZhaoxianSize(quality);
    }

    public Long getFromFuben(int index, String quality) {
        return cache.getFromFuben(index, quality);
    }

    public Long getFromHuodong(int index, String quality) {
        return cache.getFromHuodong(index, quality);
    }

    public Long getFromDuihuan(int index, String quality) {
        return cache.getFromDuihuan(index, quality);
    }

}
