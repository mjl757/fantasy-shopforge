# Fantasy ShopForge — Issue Dependency Map

## Dependencies

| Issue | Title | Depends On | Module |
|-------|-------|-----------|--------|
| #1 | Project scaffolding: Gradle multi-module setup with KMP | (none) | setup |
| #2 | Domain models, enums & repository interfaces | #1 | domain |
| #3 | Shop generation use cases | #2 | domain |
| #4 | Inventory generation logic | #2 | domain |
| #5 | Shop CRUD use cases | #2 | domain |
| #6 | SQLDelight schema & queries | #2 | data |
| #7 | Built-in item catalog | #6 | data |
| #8 | Repository implementations | #2, #6 | data |
| #9 | Metro DI, Navigation & Theme setup | #1 | app |
| #10 | Shop List screen | #5, #8, #9 | app |
| #11 | Shop Detail screen | #5, #8, #9 | app |
| #12 | Create & Edit Shop screens | #3, #5, #8, #9 | app |
| #13 | Generate Shop screen | #3, #4, #8, #9 | app |
| #14 | Add Item to Shop screen | #5, #8, #9 | app |

## Execution Tiers

When processing all issues, they naturally fall into these tiers of parallelism:

- **Tier 0**: #1 (prerequisite for everything)
- **Tier 1**: #2, #9 (parallel — both depend only on #1)
- **Tier 2**: #3, #4, #5, #6 (parallel — all depend on #2)
- **Tier 3**: #7, #8 (parallel — #7 needs #6, #8 needs #2+#6)
- **Tier 4**: #10, #11, #12, #13, #14 (parallel — UI screens, depend on tiers 1-3)

## Branch Strategy

- Issues whose dependencies are **not in the current run** → branch from `main` (assume dependencies already merged)
- Issues whose dependencies **are in the current run** → branch from the dependency's feature branch
- When an issue has **multiple in-run dependencies**, branch from the one that was most recently completed (the others should already be ancestors)
