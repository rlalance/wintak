# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Wintak** is a JavaFX desktop Gantt-chart editor (see `Process/Backlog.md` EPIC-1). Stack: **Java 17**,
**JavaFX 21** (FXML, MVC), build with **Maven**, tests with **JUnit 5**.

## The Process — NON-NEGOTIABLE

**RED → GREEN → REVIEW → (repeat until the review is satisfied) → UX REVIEW (if UI) → COMMIT → next task**

1. **RED** — write the JUnit test first and watch it fail (a compile error naming the missing type
   counts). For a visual behaviour, observe the broken/absent state via `mvn javafx:run`.
2. **GREEN** — implement the minimum to pass; build clean + run the relevant tests.
3. **REVIEW** — review the diff before committing: correctness, style conformance, simplification /
   reuse opportunities, and the playbook gotchas. Fix the findings and re-verify GREEN. **Repeat
   review → fix → green until the review comes back clean** — only then move on.
4. **UX REVIEW (UI-touching changes only)** — judge the *look*, not the diff: `mvn javafx:run` and
   reconcile the scene you *see* against the UI conventions (layout, alignment, spacing, colours).
   Screenshots capture the **app window region only — never the full desktop** (other windows /
   personal content must not land in artifacts or context). Fix what reads wrong and re-loop; say
   "no UI touched" when nothing visual changed.
5. **COMMIT** — one item, one commit, immediately after the review is satisfied. Never commit a
   failing build/test or an unreviewed diff.
6. **Next task** — propose it and wait for a go-ahead.

## Text conventions

Unless specified otherwise: **UTF-8** encoding (already pinned via `project.build.sourceEncoding`) and
**English** for all text (Java, comments, docs, UI strings).

## Lexicon 🖖 — command verbs

Shorthand the user may fire; honour each exactly: **Engage** (green-light the proposed next item, start
now) · **Make it so** (approve the last plan as stated) · **Away team: \<q\>** (background research
agent(s); main thread carries on) · **Captain's Log: \<note\>** (dated note in the right `Process/` doc;
no action) · **Red Alert: \<bug\>** (file in `Process/Bugs.md` AND drop work to repro it now) ·
**Yellow Alert: \<bug\>** (file it; hold course) · **Jettison \<item\>** (won't-do; archive with a
one-line why) · **Scan \<area\>** (read-only recon; change nothing) · **Stand down** (stop cleanly,
tidy tree, report state) · **Night Duty** (autonomous backlog execution: work FIFO through open items,
commit each, no confirmation pauses between items; active until the Admiral says **Daytime Shift**) ·
**Daytime Shift** (cancel Night Duty, return to the normal propose-then-wait cycle) ·
**Memory Alpha** (archive completed items: move `[x]`/resolved entries — with their done-notes and dates
— from `Process/Backlog.md`, `Process/Bugs.md`, and `Process/Tasks.md` into `Process/Archive.md`;
bare form = sweep everything completed; with a specific item = archive just that one; **append-only at
the END of `Archive.md`** — no sorting or reorganising; named for the Federation's central library) ·
**Dilithium report** (token fuel gauge: report session/week tokens consumed; tokens are dilithium) ·
**Docked** (the running Wintak JavaFX app has been closed — safe to run `mvn test`, `mvn package`,
`mvn javafx:run`, or other Maven goals without conflicts) ·
**IC** / **OOC** (go In Character — TNG overlay on the chat; flavour only, the engineering stays exact;
OOC drops the act instantly) ·
**Report** (read `Process/Backlog.md` + `Process/Bugs.md`; list open bugs first, then open backlog items
grouped by section with ids and titles — formatted, prioritised, read-only). Fleet-wide verbs
(Fleetcast, Hail) live on the flagship, Orchestrai.

## Common Commands

- `mvn test` / `mvn package` — build + tests across both modules (more in the `maven` playbook).
- `mvn -pl wintak-app javafx:run` — run the app (JavaFX plugin lives in the app module).

- **Coordinates:** groupId `com.richardl.wintak`, base package `com.richardl.wintak`.
- **Modules:** `wintak-design` (ComponentSheet, Theme/Manager/Prefs, CSS tokens — reusable library);
  `wintak-app` (Gantt editor, map, controllers, model — the runnable application).
- **Sources:** `wintak-app/src/main/java/…` or `wintak-design/src/main/java/…`; mirror for tests and resources.

## Architecture — MVC

`model` (plain Java + JavaFX properties, UI-free, headless-testable) ← `controller` (mediates,
commands, dialog seams) ← `view` (FXML + thin nodes). Packages: `com.richardl.wintak.{model,view,controller,app}`.
Full rules in the `javafx` playbook.

## Skills & playbooks — how procedures are stored

Repeatable procedures live as **playbooks** in `Process/playbooks/*.md` (single source of truth). Claude
auto-loads the matching playbook through a thin skill in `.claude/skills/<name>/SKILL.md` — edit the
playbook, not the wrapper; don't paste playbook content back into this file.

Current set: `maven`, `javafx`, `ui-design`, `persistence`, `github-ops`, `research-doc` (→
`Research/RESEARCH-PROCESS.md`), `research-tokenbox`. Always-on rules stay in *this* file: the Process,
commands, architecture, and the doc layout.

## Project Document Layout

`Process/` (Backlog.md, Bugs.md, Archive.md, Tasks.md, playbooks/), `Research/`, `Design/` — keep the
root clean.

> **Tasks tier:** quick to-dos below backlog weight live in `Process/Tasks.md` (`Task: …`); graduate to `Backlog.md` if they grow teeth.

> **Backlog placement:** unless told otherwise, file a new item under its matching epic/story (if one
> exists) at the **end** of that list, else append at the end of the file — items with no parent epic
> are fine.

## Research Documents

Process: `Research/RESEARCH-PROCESS.md` (auto-loads via the `research-doc` skill). Register every Full
Research doc here:

| Document | Status | Supersedes | Summary |
| --- | --- | --- | --- |
| `javafx-atak-map-research.md` | Draft | — | MapJFX recommended for OSM map canvas; CoT via UDP multicast (no library); layered StackPane architecture for map+Gantt+tracks+draw. |
