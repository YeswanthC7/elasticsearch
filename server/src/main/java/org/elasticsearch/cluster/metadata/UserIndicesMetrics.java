/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.cluster.metadata;

import org.elasticsearch.telemetry.metric.LongWithAttributes;
import org.elasticsearch.telemetry.metric.MeterRegistry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A singleton telemetry metrics sender for total number of user indices per serverless project.
 * This metric is updated by {@link MetadataCreateIndexService} and {@link MetadataDeleteIndexService}
 */
public class UserIndicesMetrics {

    public static final String USER_INDEX_TOTAL_BY_PROJECT_METRIC_NAME = "es.cluster.user.index.total.by.project.current";
    private final ConcurrentHashMap<ProjectId, Long> userIndexTotalByProject;

    public UserIndicesMetrics(MeterRegistry meterRegistry) {
        this.userIndexTotalByProject = new ConcurrentHashMap<>();
        meterRegistry.registerLongsGauge(
            USER_INDEX_TOTAL_BY_PROJECT_METRIC_NAME,
            "Total number of user indices by project",
            "index",
            () -> {
                if (userIndexTotalByProject.isEmpty()) {
                    return List.of();
                }

                return userIndexTotalByProject.entrySet()
                    .stream()
                    .map(entry -> new LongWithAttributes(entry.getValue(), Map.of("serverless_project_id", entry.getKey())))
                    .toList();
            }
        );
    }

    public void recordUserIndexTotal(ProjectId projectId, long userIndexTotal) {
        this.userIndexTotalByProject.put(projectId, userIndexTotal);
    }

}
