# PR Intelligence Platform System Design

This project should be presented as an AI-backed developer productivity platform, not as a chatbot. The strongest story is that it orchestrates multiple analysis strategies around pull requests: deterministic policy scanning, review chunk routing, retrieval-augmented standards lookup, and LLM-style synthesis.

## Component responsibilities

### API edge

- Accept review jobs from a UI, webhook, or manual trigger
- Validate repository and PR metadata
- Return a fast acknowledgement

### Review orchestration

- Build an idempotency key per repository, PR number, head branch, and policy pack
- Prevent duplicate review jobs during retries or webhook replays
- Trigger asynchronous execution

### Review chunking

- Split changed files into bounded review lanes such as `trust-boundary`, `request-edge`, and `runtime-policy`
- Preserve deterministic analysis even when PRs grow larger
- Create a future seam for parallel worker fan-out

### GitHub ingestion

- Load PR metadata, file diffs, and changed-file context
- Future-ready for GitHub App authentication and pagination

### Policy engine

- Run low-latency deterministic checks before invoking any model
- Apply different heuristics based on the selected policy pack
- Catch high-signal issues such as auth bypasses, sensitive logging, and risky architecture drift

### Retrieval layer

- Pull coding standards, ownership notes, or design docs from a vector store
- Ground AI feedback in team-specific engineering context

### AI review layer

- Convert patch context and policy findings into reviewer-friendly recommendations
- Generate an executive summary and architectural guidance

### Persistence and events

- Store review jobs and findings in PostgreSQL
- Persist outbox events so publishing and notifications happen reliably after commit

## Distributed-system talking points

- Idempotency protects against duplicate PR webhook deliveries
- Async workers decouple user latency from analysis latency
- Chunk routing makes it easier to parallelize or shard large PR analysis later
- Outbox events keep side effects reliable and replayable
- Retrieval and AI adapters are isolated to reduce vendor lock-in
- Review findings are structured, which makes downstream analytics and alerting practical

## Resume bullets you can derive from this

- Designed a Java 21 Spring Boot PR intelligence platform with asynchronous review orchestration, policy-based analysis, and AI-generated engineering recommendations.
- Implemented idempotent review job submission, policy-pack-aware rule enforcement, structured finding persistence, and outbox event publishing for reliable downstream integrations.
- Built modular GitHub, review chunking, vector-retrieval, and AI provider boundaries to support scalable code review automation and future multi-provider LLM integration.
