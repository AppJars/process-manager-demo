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
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.Flex;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The main view is a top-level placeholder for other views.
 *
 * <p>Anonymous so the public landing page (HomeView) can render inside this layout; this is an
 * anonymous demo (no login), so no reroute is needed.
 */
@SuppressWarnings("serial")
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

  private static final String LAYOUT_KEY_PREFIX = "appjars.processmanagerdemo.mainlayout.";
  private static final String TOUR_KEY_PREFIX = "appjars.processmanagerdemo.demo.home.tour.";

  private H2 viewTitle;
  private MenuItem thisPageItem;

  public MainLayout() {
    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.getElement().setAttribute("aria-label", "Menu toggle");

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE, Flex.GROW);

    addToNavbar(true, toggle, viewTitle, createTourMenu());
  }

  /**
   * The tour menu of the navigation bar: it makes the guided tours reachable from any view, not only
   * from the landing page.
   */
  private MenuBar createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addClassName("navbar-tour-menu");
    menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    SubMenu tours = menu
        .addItem(new Div(VaadinIcon.MAP_MARKER.create(),
            new Span(getTranslation(LAYOUT_KEY_PREFIX + "tour"))))
        .getSubMenu();
    thisPageItem = tours.addItem(getTranslation(TOUR_KEY_PREFIX + "thispage"),
        e -> startCurrentTour());
    // SubMenu has no add(Component) in Vaadin 25 — addSeparator() is the API for a divider.
    tours.addSeparator();
    tours.addItem(getTranslation(TOUR_KEY_PREFIX + "processes"),
        e -> startTour(DemoTour.PROCESS_LIST));
    return menu;
  }

  /**
   * Starts a tour: if its view is already showing it runs right away, otherwise it is stashed and
   * the view is navigated to, so {@link #startPendingTour()} starts it once that view renders.
   */
  private void startTour(DemoTour tour) {
    Class<? extends Component> target = tourView(tour);
    if (getContent() != null && target.equals(getContent().getClass())) {
      DemoTours.start(tour, this, this::getTranslation);
    } else {
      VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
      getUI().ifPresent(ui -> ui.navigate(target));
    }
  }

  /** "This page": runs the tour of the view currently shown. */
  private void startCurrentTour() {
    currentTour().ifPresent(tour -> DemoTours.start(tour, this, this::getTranslation));
  }

  /** The tour of the view currently shown, if that view has one (the landing page has none). */
  private Optional<DemoTour> currentTour() {
    if (getContent() == null) {
      return Optional.empty();
    }
    Class<?> current = getContent().getClass();
    return Stream.of(DemoTour.values()).filter(tour -> tourView(tour).equals(current)).findFirst();
  }

  /** Single source of truth mapping each tour to the view it runs on. */
  private Class<? extends Component> tourView(DemoTour tour) {
    return switch (tour) {
      case PROCESS_LIST -> ProcessListView.class;
    };
  }

  private void addDrawerContent() {
    VerticalLayout drawerLayout = new VerticalLayout();
    drawerLayout.addClassNames(Margin.NONE, Padding.NONE, AlignItems.STRETCH, Gap.XSMALL);
    drawerLayout.setSizeFull();

    Image logo = new Image("/icons/icon.png", null);
    logo.setHeight("5vh");
    logo.setWidth("5vh");

    H3 title = new H3(getTranslation(LAYOUT_KEY_PREFIX + "title"));

    Header header = new Header(logo, title);
    header.addClassNames(Display.FLEX, Gap.XSMALL, AlignItems.CENTER, Margin.MEDIUM);
    title.addClassName(Flex.GROW);

    Scroller scroller = new Scroller(createNavigation());

    drawerLayout.add(header, scroller);
    drawerLayout.expand(scroller);

    addToDrawer(drawerLayout);
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();

    SideNavItem homeItem =
        new SideNavItem(getTranslation(LAYOUT_KEY_PREFIX + "nav.home"), HomeView.class);
    homeItem.setPrefixComponent(VaadinIcon.HOME.create());

    SideNavItem processManagerItem =
        new SideNavItem(getTranslation(LAYOUT_KEY_PREFIX + "nav.main"));
    processManagerItem.setPrefixComponent(VaadinIcon.SERVER.create());
    processManagerItem.setExpanded(true);
    processManagerItem.addItem(new SideNavItem(
        getTranslation(LAYOUT_KEY_PREFIX + "nav.processes"), ProcessListView.class));

    nav.addItem(homeItem, processManagerItem);

    return nav;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    viewTitle.setText(getCurrentPageTitle());
    // Nothing to tour on the landing page, so "This page" is offered but disabled there.
    thisPageItem.setEnabled(currentTour().isPresent());
    startPendingTour();
  }

  // A tour requested from another view starts once its target view has actually rendered.
  private void startPendingTour() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session.getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending
        && getContent() != null
        && tourView(pending).equals(getContent().getClass())) {
      session.setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, null);
      DemoTours.start(pending, this, this::getTranslation);
    }
  }

  private String getCurrentPageTitle() {
    if (getContent() instanceof HasDynamicTitle dynamicTitle) {
      return dynamicTitle.getPageTitle();
    }
    PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
    return title == null ? "" : title.value();
  }
}
