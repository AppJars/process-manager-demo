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
package com.appjars.processmanager.demo.views;

import com.appjars.processmanager.demo.views.tour.DemoTours;
import com.appjars.processmanager.demo.views.tour.DemoTours.DemoTour;
import com.appjars.processmanager.flow.view.ProcessListView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Public landing page of the demo: presents the appjar features, the license model and offers guided
 * tours of the views. This is an anonymous demo (no login), so there is no credentials block.
 */
@SuppressWarnings("serial")
@AnonymousAllowed
@Route(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.processmanagerdemo.demo.home.";

  private static final String APPJARS_SITE_URL = "https://www.appjars.com";
  private static final String GITHUB_ORG_URL = "https://github.com/AppJars";
  private static final String PROCESS_MANAGER_DOCS_URL =
      "https://docs.appjars.com/process-manager/overview/";

  public HomeView() {
    addClassName("home-view");
    add(createHero(), createFeaturesSection(), createTryItSection(), createLicenseSection(),
        createLinksSection());
    setAlignItems(Alignment.STRETCH);
  }

  private Component createHero() {
    Image logo = new Image("icons/icon-appjars-full.png", "AppJars");
    logo.addClassName("home-logo");

    H1 title = new H1(t("hero.title"));
    Paragraph tagline = new Paragraph(t("hero.tagline"));
    tagline.addClassName("home-tagline");

    Div hero = new Div(logo, title, tagline);
    hero.setId("home-hero");
    hero.addClassName("home-hero");
    return hero;
  }

  private Component createFeaturesSection() {
    Div cards = new Div(
        featureCard(VaadinIcon.CALENDAR_CLOCK, "features.scheduling"),
        featureCard(VaadinIcon.PLAY, "features.execute"),
        featureCard(VaadinIcon.RECORDS, "features.history"),
        featureCard(VaadinIcon.PAUSE, "features.control"),
        featureCard(VaadinIcon.CODE, "features.yourtasks"),
        featureCard(VaadinIcon.CLIPBOARD_PULSE, "features.status"),
        featureCard(VaadinIcon.PLUS_CIRCLE, "features.crud"),
        featureCard(VaadinIcon.MOBILE, "features.responsive"));
    cards.addClassName("home-features");

    return section("home-features", t("features.title"), cards);
  }

  private Card featureCard(VaadinIcon icon, String key) {
    Card card = new Card();
    card.addClassName("home-feature-card");
    Icon prefix = icon.create();
    prefix.addClassName("home-feature-icon");
    card.setHeaderPrefix(prefix);
    card.setTitle(t(key + ".title"));
    card.add(new Paragraph(t(key + ".desc")));
    return card;
  }

  private Component createTryItSection() {
    Paragraph intro = new Paragraph(t("tryit.intro"));

    Div tasks = new Div(taskRow("ReminderTask", t("tryit.tasks.reminder")),
        taskRow("ServerTask", t("tryit.tasks.server")),
        taskRow("UpdateServiceTask", t("tryit.tasks.updateservice")));
    tasks.addClassName("home-tasks");

    Button processes = new Button(t("tryit.processes"),
        e -> getUI().ifPresent(ui -> ui.navigate(ProcessListView.class)));
    processes.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Div actions = new Div(processes, createTourMenu());
    actions.addClassName("home-actions");

    return section("home-tryit", t("tryit.title"), intro, tasks, actions);
  }

  private Div taskRow(String taskClass, String description) {
    Span code = new Span(taskClass);
    code.addClassName("home-task-code");
    Span desc = new Span(description);
    desc.addClassName("home-task-desc");
    Div row = new Div(code, desc);
    row.addClassName("home-task");
    return row;
  }

  private Component createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    SubMenu tours =
        menu.addItem(new Div(VaadinIcon.MAP_MARKER.create(), new Span(t("tour.button")))).getSubMenu();
    // Tours cover the appjar's views; this landing page has none, so its entry stays disabled.
    tours.addItem(t("tour.thispage")).setEnabled(false);
    tours.addSeparator();
    tours.addItem(t("tour.processes"),
        e -> startViewTour(DemoTour.PROCESS_LIST, ProcessListView.class));
    return menu;
  }

  private void startViewTour(DemoTour tour, Class<? extends Component> view) {
    VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
    getUI().ifPresent(ui -> ui.navigate(view));
  }

  private Component createLicenseSection() {
    Paragraph desc = new Paragraph(t("license.desc"));
    Anchor link = new Anchor(APPJARS_SITE_URL, t("license.link"));
    link.setTarget("_blank");
    return section("home-license", t("license.title"), desc, new Paragraph(link));
  }

  private Component createLinksSection() {
    Anchor github = new Anchor(GITHUB_ORG_URL, t("links.github"));
    github.setTarget("_blank");
    Anchor readme = new Anchor(PROCESS_MANAGER_DOCS_URL, t("links.readme"));
    readme.setTarget("_blank");
    Div links = new Div(github, readme);
    links.addClassName("home-links");
    return section("home-links", t("links.title"), links);
  }

  private Div section(String id, String title, Component... content) {
    Div section = new Div();
    section.setId(id);
    section.addClassName("home-section");
    section.add(new H3(title));
    section.add(content);
    return section;
  }

  private String t(String key) {
    return getTranslation(KEY_PREFIX + key);
  }

  @Override
  public String getPageTitle() {
    return t("title");
  }
}
