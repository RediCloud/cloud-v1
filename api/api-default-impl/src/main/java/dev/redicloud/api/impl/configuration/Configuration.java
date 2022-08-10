package dev.redicloud.api.impl.configuration;

import dev.redicloud.api.configuration.IConfiguration;
import dev.redicloud.api.impl.redis.bucket.RBucketObject;

public abstract class Configuration extends RBucketObject implements IConfiguration {}
