---
name: verify
description: Verify that a code change actually does what it's supposed to by running the app and observing behavior. Use when asked to verify a PR, confirm a fix works, test a change manually, check that a feature works, or validate local changes before pushing.
---

# Verify — JavaFX / Wintak runtime verification

Follow the canonical playbook — two tiers (headless FX harness vs live Win32 automation),
when to use each, helper snippets, window layout reference, and the report format:

➡️ **[`Process/playbooks/verify.md`](../../../Process/playbooks/verify.md)**

**TL;DR decision:**
- User is on the machine / display not free → **Tier 1** (headless `FxThread` + `FxToolkitExtension`)
- Display is free and pixel rendering must be observed → **Tier 2** (Win32 screenshot + mouse automation)

Single source of truth — edit the playbook, not this wrapper.
