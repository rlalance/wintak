---
name: persistence
description: Use when adding or changing code that saves/loads user state to disk in Wintak (the Gantt document JSON, settings, auto-save). Covers atomic writes via Files.move(ATOMIC_MOVE), schema-versioning, the pure Builder/Restorer pair with round-trip tests, debounced auto-save, keeping JavaFX types out of snapshots, and not letting a load look like an edit (dirty flag / undo).
---

# Persistence patterns

Follow the canonical playbook — atomic writes, `schemaVersion`, Builder/Restorer + round-trip test,
debounce, identity preservation, and the load-must-not-dirty rule:

➡️ **[`Process/playbooks/persistence.md`](../../../Process/playbooks/persistence.md)**

Single source of truth — edit it there, not here.
