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
package com.appjars.processmanager.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import com.appjars.AppJarsAutoConfiguration;
import com.appjars.processmanager.ProcessManagerAutoConfiguration;
import com.appjars.processmanager.demo.views.MainLayout;
import com.appjars.processmanager.flow.util.RouteConfigurer;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.lumo.Lumo;
import jakarta.annotation.PostConstruct;

/** Process Manager demo application. */
@SuppressWarnings("serial")
@SpringBootApplication
@ComponentScan(
    basePackageClasses = {ProcessManagerAutoConfiguration.class, AppJarsAutoConfiguration.class})
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
@StyleSheet("styles.css")
@PWA(name = "Process Manager Demo", shortName = "Process Manager Demo",
    offlineResources = {"icons/icon.png"})
@Push
@EnableVaadin({"com.appjars.processmanager.flow", "com.appjars.processmanager.demo"})
public class Application extends SpringBootServletInitializer implements AppShellConfigurator {

  private final RouteConfigurer routeConfigurer;

  public Application(RouteConfigurer routeConfigurer) {
    this.routeConfigurer = routeConfigurer;
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void configurePage(AppShellSettings settings) {
    settings.addFavIcon("icon", "icons/icon.png", "180x180");
  }

  @PostConstruct
  public void configure() {
    routeConfigurer.setViewsRouterLayout(MainLayout.class);
  }

}
