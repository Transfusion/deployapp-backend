package io.github.transfusion.deployapp.config.jobrunr;

import io.lettuce.core.RedisClient;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.nosql.redis.LettuceRedisStorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobRunrConfiguration {

    @Autowired
    @Qualifier("jobRunrRedisClient")
    private RedisClient jobRunrRedisClient;

    @Bean
    StorageProvider storageProvider(JobMapper jobMapper) {
        LettuceRedisStorageProvider storageProvider = new LettuceRedisStorageProvider(jobRunrRedisClient);
        storageProvider.setJobMapper(jobMapper);
        // [옵션] Job의 상태 변화 시점을 제어할 수 있는 리스너 빈 등록
//        storageProvider.addJobStorageOnChangeListener(JobChangeListenerImpl())

        return storageProvider;
    }
}
