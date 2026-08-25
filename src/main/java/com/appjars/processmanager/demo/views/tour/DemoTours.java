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
package com.appjars.processmanager.demo.views.tour;

import com.appjars.processmanager.flow.util.TestIds;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.SerializableFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vaadin.addons.antlerflow.tour.EngineType;
import org.vaadin.addons.antlerflow.tour.Tour;
import org.vaadin.addons.antlerflow.tour.TourButton;
import org.vaadin.addons.antlerflow.tour.TourButtonType;
import org.vaadin.addons.antlerflow.tour.TourStep;

/**
 * Factory of the guided tours offered by the demo. Tours cover the appjar's views only — the landing
 * page presents itself and has none. Steps anchor to the {@code data-testid} attributes the appjar
 * exposes through {@link TestIds}, never to tag names or translated labels.
 *
 * <p>The engine is always Driver.js: Shepherd.js is not free for commercial use, so every
 * client-side helper below targets Driver's DOM ({@code .driver-popover},
 * {@code .driver-active-element}).
 */
public final class DemoTours {

  /** Session attribute used to start a tour after navigating to its view. */
  public static final String PENDING_TOUR_ATTRIBUTE = DemoTours.class.getName() + ".pendingTour";

  static final String KEY_PREFIX = "appjars.processmanagerdemo.demo.tour.";

  /** Attribute the client-side resolver puts on the visible element each step should point at. */
  private static final String TARGET_ATTR = "data-antler-target";

  /** Attribute of the hidden marker each step carries in its content, to identify the live step. */
  private static final String STEP_ATTR = "data-tour-step";

  /** Id of the step that needs the row's "more actions" menu open while it is shown. */
  private static final String ACTIONS_STEP = "processes-actions";

  /**
   * Points every step at the first <em>visible</em> match of its real selector. A raw selector can
   * also match a hidden, zero-sized duplicate (a closed dialog, a component moved out of the
   * layout, a second instance of the same component) and {@code querySelector} may well return that
   * one, which sends the step to the top-left corner. Instead the resolver tags the winner with
   * {@code data-antler-target} and keeps the tag in sync as the view renders; a selector that
   * matches nothing gets no tag, so Driver centers that step — the safe fallback. {@code $0} is a
   * JSON map of {@code {stepId: cssSelector}}.
   */
  private static final String RESOLVE_TARGETS_JS =
      """
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      const MAP = JSON.parse($0);
      const ATTR = '%s';
      const resolve = () => {
        Object.keys(MAP).forEach(id => {
          let pick = null;
          for (const el of document.querySelectorAll(MAP[id])) {
            const r = el.getBoundingClientRect();
            if (r.width > 4 && r.height > 4) { pick = el; break; }
          }
          document.querySelectorAll("[" + ATTR + "='" + id + "']")
              .forEach(el => { if (el !== pick) { el.removeAttribute(ATTR); } });
          if (pick && pick.getAttribute(ATTR) !== id) { pick.setAttribute(ATTR, id); }
        });
      };
      let scheduled = false;
      const schedule = () => {
        if (scheduled) { return; }
        scheduled = true;
        requestAnimationFrame(() => { scheduled = false; resolve(); });
      };
      resolve();
      const obs = new MutationObserver(schedule);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['hidden', 'style', 'class']});
      window.__antlerResolver = {
        stop() {
          obs.disconnect();
          document.querySelectorAll('[' + ATTR + ']').forEach(el => el.removeAttribute(ATTR));
          window.__antlerResolver = null;
        }
      };
      """
          .formatted(TARGET_ATTR);

  /**
   * Keeps a highlighted control from clipping its neighbours. Driver's own stylesheet forces
   * {@code overflow:hidden} on the parent of {@code .driver-active-element}, which hides the sibling
   * content sharing that container (the row's play/pause button and its actions menu live in the
   * same grid cell). The rule below has higher specificity so it wins over Driver's
   * {@code !important}, and it only matches while a step is active.
   */
  private static final String TOUR_CSS_JS =
      """
      if (!document.getElementById('demo-tour-css')) {
        const style = document.createElement('style');
        style.id = 'demo-tour-css';
        style.textContent =
            'body :not(body):has(> .driver-active-element) { overflow: visible !important; }';
        document.head.appendChild(style);
      }
      """;

  /**
   * Promotes the Driver popover into the browser top layer. In Vaadin 25 every overlay (menu bar,
   * dialog, popover) opens through the native Popover API, so it paints above <em>all</em>
   * z-indexed content and no z-index can put an ordinary DOM node in front of it. Making the
   * popover a manual popover puts it in the same layer; since paint order there follows the last
   * {@code showPopover()} call, the popover is re-asserted whenever another overlay opens after the
   * tour started. Resetting {@code margin} to 0 neutralizes the UA {@code margin:auto} of
   * {@code :popover-open}, which would otherwise re-center the popover and fight Driver's own inset
   * positioning.
   */
  private static final String PROMOTE_TOP_LAYER_JS =
      """
      if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }
      const promote = () => document.querySelectorAll('.driver-popover').forEach(el => {
        if (el.getAttribute('popover') !== 'manual') { el.setAttribute('popover', 'manual'); }
        el.style.margin = '0';
        try { if (!el.matches(':popover-open')) { el.showPopover(); } } catch (e) {}
      });
      const reassert = () => {
        const el = document.querySelector('.driver-popover');
        if (el && el.matches(':popover-open')) {
          try { el.hidePopover(); el.showPopover(); } catch (e) {}
        }
      };
      const onToggle = (e) => {
        const t = e.target;
        if (e.newState === 'open' && t && t.classList
            && !t.classList.contains('driver-popover')) { reassert(); }
      };
      document.addEventListener('toggle', onToggle, true);
      const obs = new MutationObserver(promote);
      obs.observe(document.body, {childList: true, subtree: true});
      promote();
      window.__demoTourTopLayer = {
        stop() {
          obs.disconnect();
          document.removeEventListener('toggle', onToggle, true);
          document.querySelectorAll('.driver-popover[popover]').forEach(el => {
            try { el.hidePopover(); } catch (e) {}
            el.removeAttribute('popover');
          });
          window.__demoTourTopLayer = null;
        }
      };
      """;

  /**
   * Opens the row's "more actions" (⋮) menu while the step explaining it is shown, so the entries
   * are visible as they are described, and closes it again on any other step. antler-tour 1.1.0 has
   * no per-step server hook and Driver does not expose the live step id in the DOM, so the step is
   * identified by the hidden marker every step carries in its content. {@code $0} is the id of the
   * step that wants the menu open, {@code $1} the selector of the menu.
   */
  private static final String MENU_HOOK_JS =
      """
      if (window.__demoTourMenus) { window.__demoTourMenus.stop(); }
      const openStep = $0;
      const menuSelector = $1;
      const closeOverlays = () => {
        document.querySelectorAll('vaadin-menu-bar').forEach(m => { if (m._close) { m._close(); } });
        document.querySelectorAll('vaadin-menu-bar-submenu').forEach(sub => {
          const ov = sub.shadowRoot && sub.shadowRoot.querySelector('vaadin-menu-bar-overlay');
          if (ov) { ov.opened = false; }
        });
      };
      const openRowActions = () => {
        const btn = document.querySelector(menuSelector + ' vaadin-menu-bar-button');
        if (btn) { btn.click(); }
      };
      let current = null;
      const sync = () => {
        const marker = document.querySelector('.driver-popover [%s]');
        const id = marker ? marker.getAttribute('%s') : null;
        if (id === current) { return; }
        current = id;
        closeOverlays();
        if (id === openStep) { setTimeout(openRowActions, 150); }
      };
      const obs = new MutationObserver(sync);
      obs.observe(document.body, {childList: true, subtree: true});
      window.__demoTourMenus = {
        stop() {
          obs.disconnect();
          closeOverlays();
          window.__demoTourMenus = null;
        }
      };
      sync();
      """
          .formatted(STEP_ATTR, STEP_ATTR);

  /** Stops every client-side helper the tour installed. */
  private static final String STOP_JS =
      """
      ['__antlerResolver', '__demoTourMenus', '__demoTourTopLayer']
          .forEach(key => { if (window[key]) { window[key].stop(); } });
      const css = document.getElementById('demo-tour-css');
      if (css) { css.remove(); }
      """;

  public enum DemoTour {
    PROCESS_LIST
  }

  /**
   * A step of a tour. {@code selector} is the real element selector, resolved to the visible match
   * at runtime; a {@code null} selector makes the step centered/floating.
   */
  private record StepDef(String key, String selector, String position, boolean first,
      boolean last) {}

  private DemoTours() {}

  public static Tour create(DemoTour tour, SerializableFunction<String, String> translator) {
    List<TourStep> steps = steps(tour).stream().map(def -> step(def, translator)).toList();
    return Tour.builder().engineType(EngineType.DRIVER).steps(steps).showCancelButton(true)
        .allowClose(true).build();
  }

  /**
   * Creates the tour, attaches it to {@code host} and starts it, detaching it again once it is
   * completed or canceled.
   */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator) {
    Tour t = create(tour, translator);
    host.getElement().appendChild(t.getElement());
    host.getElement().executeJs(TOUR_CSS_JS);
    host.getElement().executeJs(RESOLVE_TARGETS_JS, targetJson(steps(tour)));
    t.addTourCompletedListener(e -> stop(t, host));
    t.addTourCanceledListener(e -> stop(t, host));
    t.start();
    if (tour == DemoTour.PROCESS_LIST) {
      // This tour opens the row actions menu, an overlay that paints in the browser top layer.
      host.getElement().executeJs(PROMOTE_TOP_LAYER_JS);
      host.getElement().executeJs(MENU_HOOK_JS, ACTIONS_STEP, testId(TestIds.PROCESS_ACTIONS_MENU));
    }
  }

  private static void stop(Tour tour, Component host) {
    host.getElement().executeJs(STOP_JS);
    tour.getElement().removeFromParent();
  }

  private static List<StepDef> steps(DemoTour tour) {
    return switch (tour) {
      case PROCESS_LIST -> processListSteps();
    };
  }

  /**
   * Opens with a centered step so the view is fully laid out before the first anchored one. The demo
   * seeds one sample process, so the grid has a row and the per-row controls can be highlighted
   * directly; {@code MENU_HOOK_JS} deploys the ⋮ menu on the actions step. Ends with a centered
   * summary: the free-limit bar only renders without a license, so the closing note about the limit
   * is not attached to it.
   */
  private static List<StepDef> processListSteps() {
    return List.of(new StepDef("processes.intro", null, null, true, false),
        new StepDef("processes.grid", testId(TestIds.PROCESS_GRID), "top", false, false),
        new StepDef("processes.filters", testId(TestIds.PROCESS_FILTERS), "bottom", false, false),
        new StepDef("processes.create", testId(TestIds.PROCESS_CREATE_BUTTON), "bottom", false,
            false),
        new StepDef("processes.playpause", testId(TestIds.PROCESS_SCHEDULE_TOGGLE), "bottom", false,
            false),
        new StepDef("processes.actions", testId(TestIds.PROCESS_ACTIONS_MENU), "top", false, false),
        new StepDef("processes.finish", null, null, false, true));
  }

  private static String testId(String testId) {
    return "[" + TestIds.ATTRIBUTE + "='" + testId + "']";
  }

  private static String stepId(StepDef def) {
    return def.key().replace('.', '-');
  }

  /** {@code {stepId: cssSelector}} of the anchored steps, as the resolver expects it. */
  private static String targetJson(List<StepDef> defs) {
    return defs.stream().filter(def -> def.selector() != null)
        .map(def -> "\"" + stepId(def) + "\":\"" + def.selector().replace("\"", "\\\"") + "\"")
        .collect(Collectors.joining(",", "{", "}"));
  }

  private static TourStep step(StepDef def, SerializableFunction<String, String> t) {
    List<TourButton> buttons = new ArrayList<>();
    if (!def.first()) {
      buttons.add(TourButton.builder().label(t.apply(KEY_PREFIX + "btn.back")).secondary(true)
          .type(TourButtonType.PREVIOUS).build());
    }
    buttons.add(
        TourButton.builder().label(t.apply(KEY_PREFIX + (def.last() ? "btn.done" : "btn.next")))
            .type(TourButtonType.NEXT).build());
    String id = stepId(def);
    // The hidden marker is how the client-side helpers know which step is currently shown.
    String content = t.apply(KEY_PREFIX + def.key() + ".desc") + "<span hidden " + STEP_ATTR + "='"
        + id + "'></span>";
    return TourStep.builder().id(id)
        .attachTo(def.selector() == null ? null : "[" + TARGET_ATTR + "='" + id + "']")
        .position(def.position()).title(t.apply(KEY_PREFIX + def.key() + ".title")).content(content)
        .buttons(buttons).build();
  }
}
