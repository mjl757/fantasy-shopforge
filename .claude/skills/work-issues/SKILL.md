---
name: work-issues
description: Process multiple GitHub issues in parallel using worktree-isolated agents. Resolves dependencies, plans execution waves, and spawns issue-worker agents that code, test, review, commit, and raise PRs.
argument-hint: [<issue-numbers...>] [--all] [--dry-run]
---

# Work Issues — Parallel GitHub Issue Processor

Process one or more GitHub issues autonomously. Each issue is handled by a worktree-isolated `issue-worker` agent that implements the feature, writes tests, self-reviews, commits, pushes, and creates a pull request.

## Parse Arguments

Extract from `$ARGUMENTS`:
- **Issue numbers**: space-separated integers (e.g., `3 4 5`)
- **--all**: Process ALL open GitHub issues
- **--dry-run**: If present, show the execution plan without launching agents

If no issue numbers and no `--all` flag are provided, default to `--all` (process all open issues).

## Execution

### Step 1: Gather and Validate Issues

**If `--all` or no issue numbers provided**, fetch all open issues:

```bash
gh issue list --repo mjl757/fantasy-shopforge --state open --json number,title,body,labels --limit 100
```

Use all returned issue numbers as the working set.

**If specific issue numbers provided**, fetch metadata for each:

```bash
gh issue view <number> --repo mjl757/fantasy-shopforge --json number,title,body,labels,state
```

- Verify all issues exist and are in `OPEN` state
- Report any closed, missing, or invalid issues and ask if the user wants to continue with the valid ones

### Step 2: Resolve Dependencies into Waves

Read the dependency map at `references/dependency-map.md`.

For each requested issue, classify its blockers:
- **External blocker**: A blocker issue that is NOT in the requested set → assumed already merged to `main`
- **Internal blocker**: A blocker issue that IS in the requested set → must complete first

Sort issues into execution waves using this algorithm:

1. **Wave 1**: Issues with NO internal blockers (all their dependencies are external or they have none)
2. **Wave 2**: Issues whose internal blockers are ALL in Wave 1
3. **Wave N**: Issues whose internal blockers are ALL in Waves 1 through N-1
4. If any issues remain unplaced after all waves are computed, there is a circular dependency — report it and stop

### Step 3: Present Execution Plan

Display the plan clearly:

```
Execution Plan:
  Wave 1 (parallel): #3 Shop generation use cases, #4 Inventory generation logic, #5 Shop CRUD use cases
    Base branch: main
  Wave 2 (parallel): #8 Repository implementations
    Base branch: feature branch from #6 (Wave 1)

Total: 4 issues in 2 waves
```

If `--dry-run` was specified, stop here.

Otherwise, ask the user to confirm before proceeding using AskUserQuestion.

### Step 4: Execute Waves

Process waves sequentially. Within each wave, launch all agents in parallel.

For each issue in the current wave, determine the base branch:
- If the issue has NO internal blockers → base branch is `main`
- If the issue has internal blockers → base branch is the feature branch created by the most recent blocker from a prior wave
  - Check prior wave results for the branch name
  - If multiple internal blockers exist, use the branch from the blocker that completed last (it should contain all prior changes if they were chained correctly)

Launch each agent using the Agent tool:
- `subagent_type`: `"issue-worker"`
- `isolation`: `"worktree"`
- `run_in_background`: `true`
- `prompt`: Include the issue number, base branch, and repo:
  ```
  Implement GitHub issue #<NUMBER> for the Fantasy ShopForge project.

  ISSUE_NUMBER: <NUMBER>
  BASE_BRANCH: <BRANCH>
  REPO: mjl757/fantasy-shopforge
  ```

Wait for ALL agents in the current wave to complete before moving to the next wave.

After each wave, collect and record:
- Branch name created by each agent
- PR URL created by each agent
- Success or failure status
- Any error messages from failed agents

If an agent fails:
- Report the failure with details
- Mark any issues in later waves that depend on the failed issue as **skipped**
- Continue with non-dependent issues in later waves

### Step 5: Report Results

After all waves complete, present a summary table:

```
Results:
| Issue | Title                          | Branch                              | PR     | Status    |
|-------|--------------------------------|--------------------------------------|--------|-----------|
| #3    | Shop generation use cases      | feature/3-shop-generation-use-cases  | #15    | Success   |
| #4    | Inventory generation logic     | feature/4-inventory-generation-logic | #16    | Success   |
| #5    | Shop CRUD use cases            | feature/5-shop-crud-use-cases        | #17    | Failed    |
| #8    | Repository implementations     | —                                    | —      | Skipped   |
```

If any issues failed:
- Show the error details
- Offer to retry failed issues: "Would you like to retry the failed issues?"

## Error Handling

- **Agent timeout**: If an agent takes more than 10 minutes, note it in the results
- **Push conflicts**: If a push fails due to conflicts, report it — do not force push
- **Build failures**: The issue-worker agent handles build failures internally; if it still fails, it will report the error
- **Partial success**: Always complete all possible waves even if some agents fail; only skip issues with failed dependencies
