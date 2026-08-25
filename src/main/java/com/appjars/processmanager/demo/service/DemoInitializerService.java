/*-
 * #%L
 * Process Manager Appjars - Demo
 * %%
 * Copyright (C) 2023 - 2026 AppJars
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.appjars.processmanager.demo.service;

import com.appjars.processmanager.model.AssignableTask;
import com.appjars.processmanager.model.ProcessDto;
import com.appjars.processmanager.model.ScheduleStatus;
import com.appjars.processmanager.service.ProcessService;
import com.flowingcode.backendcore.model.QuerySpec;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Seeds a sample process the first time the demo runs, so evaluators land on a populated grid that
 * shows the scheduling, per-row play/pause toggle and the row actions menu (instead of an empty
 * table). Only one process is created: in free mode that is the whole free-tier quota, and exactly
 * one process keeps every row action enabled.
 */
@Service
public class DemoInitializerService {

  private static final Logger logger = LoggerFactory.getLogger(DemoInitializerService.class);

  private final ProcessService processService;

  public DemoInitializerService(ProcessService processService) {
    this.processService = processService;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void initProcesses() {
    try {
      if (processService.count(new QuerySpec()) > 0) {
        return;
      }
      AssignableTask task = pickSampleTask();
      if (task == null) {
        return;
      }
      ProcessDto process = ProcessDto.builder()
          .name("Service update")
          .schedule("*/5 * * * * *") // every 5 seconds (Spring 6-field cron) so executions show quickly
          // Save as PAUSED first: save() schedules an ENABLED process via resumeProcess() BEFORE the
          // id is assigned, which NPEs on the still-null id for a brand-new process.
          .scheduleStatus(ScheduleStatus.PAUSED)
          .jobQualifiedName(task.getFullyQualifiedName())
          .build();
      processService.save(process);
      logger.info("Seeded demo process '{}'", process.getName());
      try {
        // Now the id exists, so enabling the schedule works — leaves the demo looking active.
        processService.resumeProcess(process);
      } catch (Exception e) {
        logger.info("Seeded process left paused (schedule not enabled): {}", e.getMessage());
      }
    } catch (Exception e) {
      // Seeding is best-effort: never let it break application startup.
      logger.warn("Could not seed the demo process: {}", e.getMessage());
    }
  }

  private AssignableTask pickSampleTask() throws ClassNotFoundException {
    Set<AssignableTask> tasks = processService.getTasks();
    return tasks.stream()
        .filter(t -> t.getFullyQualifiedName() != null
            && t.getFullyQualifiedName().endsWith("UpdateServiceTask"))
        .findFirst()
        .orElse(tasks.stream().findFirst().orElse(null));
  }
}
