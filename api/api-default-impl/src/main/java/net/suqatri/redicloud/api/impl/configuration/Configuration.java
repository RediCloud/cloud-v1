package net.suqatri.redicloud.api.impl.configuration;

import net.suqatri.redicloud.api.configuration.IConfiguration;
import net.suqatri.redicloud.api.impl.redis.bucket.RBucketObject;

public abstract class Configuration extends RBucketObject implements IConfiguration {}
