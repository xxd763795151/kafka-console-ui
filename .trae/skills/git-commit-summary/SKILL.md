---
name: "git-commit-summary"
description: "Generates a structured commit summary of current session changes. Invoke when user says '准备提交git', '提交代码', 'commit summary' or similar commit-prep phrases. NEVER run git commit."
---

# Git Commit Summary Generator

## Purpose
When the user signals intent to prepare a git commit, output a **concise, minimal, ready-to-copy commit message** summarizing changes from the current conversation session. **NEVER execute `git commit`, `git add`, or any mutating git commands.** Only produce text.

## Trigger Conditions (Invoke IMMEDIATELY when any match)
- User says (Chinese or English): "准备提交git"、"提交代码"、"要提交了"、"commit 一下"、"生成commit信息"、"commit summary"、"prepare commit"、"ready to commit"、"what changed"
- User explicitly asks for a summary of changes to copy for commit

## Output Format — MINIMALIST (STRICT)
Keep it SHORT. Follow this structure exactly.

```
<type>(<scope>): <one-line subject, ≤50 chars, Chinese OK>

### 变更概要
- <高层改动点 1>
- <高层改动点 2>
- <高层改动点 3>
(3~5 entries MAX. Never exceed 5. One sentence per line.)

### 涉及文件
- <glob 聚合 1> (×N)
- <glob 聚合 2> (×M)
(Use directory/glob patterns ONLY. Never list individual files unless ≤5 files total.)

### 备注（仅提交风险，无则省略整个区块）
- <gitignore 排除提醒 / 需清缓存 / 需重启服务 等真正影响提交的事项>
```

### Type Legend
| Type    | Usage |
|---------|-------|
| feat    | 新功能 |
| fix     | Bug 修复 |
| refactor| 重构/迁移/适配（功能不变） |
| style   | 纯样式/格式 |
| perf    | 性能优化 |
| chore   | 构建/依赖/配置 |
| docs    | 仅文档 |

### Scope
`ui` / `backend` / `core` / `build` / `*` (cross-cutting).

## Formatting Rules — NON-NEGOTIABLE
1. **变更概要**
   - ✅ 3~5 条，**每一条都是完整的一句话**
   - ✅ 扁平 bullet，**不嵌套**，禁止 `- xxx: - yyy` 二级缩进
   - ❌ 禁止数量统计（"19 处"、"82 个组件"、"14+ 弹窗" 等一律删掉，直接说是什么动作）
   - ❌ 禁止展开具体技术细节的子项列表（如 antdv 语法适配里每个 API 的映射）
2. **涉及文件**
   - ✅ 优先用 glob/目录聚合：`ui/src/views/**/*.vue (×68)`、`ui/src/utils/* (×6)`、`pom.xml`、`README.md`
   - ✅ 可以按新增/修改/删除分大类：`新增: ui/vite.config.ts 等 (×N)；修改: ui/src/views/**/*.vue (×M)；删除: ui/vue.config.js 等 (×K)`
   - ❌ 禁止列几十个单文件；单文件枚举仅限本次改动 ≤5 个时
   - ❌ 禁止展开说明每个文件做了什么
3. **备注**
   - ✅ 仅保留**影响提交本身的风险事项**：需 git add -p 排除的目录（如 dist、.npm-cache、.trae）、提交前需清缓存、改动 .env / vite.config 后需重启 dev server 这一类
   - ❌ **绝对不放**：Node 版本要求、npm install / npm run dev / npm run build 构建命令、代理端口、前端 dev 流程等开发指引
4. **总体**
   - 语言匹配用户（中文）
   - 结束后补一句："以上内容可直接复制使用，需要调整可随时告知。"

## Ground Rules
1. **NO git mutations:** Never call `git commit`, `git add`, `git push`, `git reset`. Read-only inspection (`git status`, `git diff --name-only`) is allowed only to enrich accuracy.
2. **Session-scoped:** Prefer changes explicitly discussed/edited in the **current conversation**; do not pull in unrelated history.
3. **When in doubt, omit.** Accuracy > completeness.
4. **No confirmation before outputting.** Generate immediately on trigger.
