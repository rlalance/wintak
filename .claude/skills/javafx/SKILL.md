---
name: javafx
description: Use for any JavaFX work in Wintak — adding the OpenJFX dependencies / javafx-maven-plugin, Application/Stage/Scene lifecycle, FXML + controllers, JavaFX properties and bindings, the MVC split, FX-thread rules (Platform.runLater), styling, or making JavaFX code headless-testable. Covers setup, architecture, threading, and the FXML/resource gotchas.
---

# JavaFX (setup / MVC / threading / testability)

Follow the canonical playbook — Maven setup (`mvn javafx:run`), the model/view/controller boundaries
(models = javafx.base only, headless-testable), FX-thread rules, FXML resource-path gotchas:

➡️ **[`Process/playbooks/javafx.md`](../../../Process/playbooks/javafx.md)**

Styling goes through the design tokens — see the `ui-design` skill. Single source of truth — edit the
playbook, not this wrapper.
