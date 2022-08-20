package dev.redicloud.api.redis.bucket.fetch;

import dev.redicloud.api.redis.bucket.IRBucketObject;

public interface IRBucketFetchAble extends IRBucketObject {

    String getFetchKey();
    String getFetchValue();

}
