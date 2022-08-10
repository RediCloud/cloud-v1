package dev.redicloud.node.service.factory;

import dev.redicloud.api.service.configuration.IServiceStartConfiguration;

import java.util.Comparator;

public class CloudServiceStartComparator implements Comparator<IServiceStartConfiguration> {

    @Override
    public int compare(IServiceStartConfiguration o1, IServiceStartConfiguration o2) {
        return o1.getStartPriority() - o2.getStartPriority();
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

}
