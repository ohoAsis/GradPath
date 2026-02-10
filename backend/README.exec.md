GradPath – Execution README（for IDE AI）

用途说明（非常重要）

本文档用于指导「编码执行型 AI（如 Cursor / Trae / Qoder）」参与 GradPath 项目开发。

目标只有一个：
**在不破坏既有工程事实与设计共识的前提下，高效产出可演进的真实业务代码。**

本 README 是当前阶段的 **执行级事实源（Source of Truth）**。
所有代码生成与修改，必须以本文档为准。

> 若与 README.human.md 冲突，**以 README.human.md 的设计共识为上位约束**。

---

## 1. 项目概述（What）

GradPath 是一个面向高校推免（保研）场景的 **材料提交、审核与加分裁决管理系统（后端）**。

业务背景来自真实院系流程：

学生提交多类加分材料
→ 多名审核员对材料进行审核
→ 审核事实累积
→ 系统基于解释规则评估结论
→ 在此基础上进行分值裁决与后续排名

---

## 2. 核心对象（Design Logic · 当前已冻结）

以下对象 **已被代码与设计验证**，禁止随意修改语义或职责。

### 2.1 Application

* 表示“一次学生的推免申请”
* 当前系统中的 **核心业务聚合**
* **唯一状态机承载者**
* 审核与加分的最终结果只作用于 Application

### 2.2 Material

* 从属于 Application
* 表示“可被审核的材料证据”
* 是审核与加分的直接对象
* **不参与 Application 状态机**
* 当前已引入 `version`，作为审核与加分的隔离边界

### 2.3 ReviewRecord（已实现，append-only）

* 独立的审核事实实体
* 表示“一次已经发生的审核行为”
* append-only（只新增，不修改、不删除）
* 针对 **Material + Material.version**
* 不直接驱动ffected Application 状态

ReviewRecord 只表达一句话：

> “某个审核员，在某个时间，对某个材料的某个版本，做了一次判断。”

### 2.4 MaterialScore（重要新增 · 已实现）

* 表示“**针对某一材料版本的一次分值裁决结果**”
* 承载：

  * 学生申报分值（declaredScore）
  * 审核确认 / 修正分值（approvedScore）
* 显式绑定：

  * materialId
  * materialVersion
* 结果型实体：

  * 不引入流程状态
  * 不驱动 Application 状态迁移

该模型是后续 **总分计算 / 排名 / 公示** 的唯一合法数据来源。

---

## 3. Application 状态机（当前版本）

```
DRAFT
→ SUBMITTED
→ UNDER_REVIEW
→ APPROVED / REJECTED
```

### 执行约束（必须遵守）

* 状态单向流转
* **禁止在 ReviewService 中修改 Application 状态**
* 状态迁移仅允许发生在 ApplicationService
* 状态评估通过 `evaluateAfterReview(applicationId)` 显式触发

---

## 4. 当前项目进展（Where we are）

### 已完成（事实）

#### Application

* 创建（DRAFT）
* 提交（SUBMITTED）
* 审核中（UNDER_REVIEW）
* 基于审核事实的最终通过 / 驳回（APPROVED / REJECTED）

#### Material 子域

* Material Entity + DDL
* 按 Application 查询材料（GET）
* 新增材料（POST）
* 删除材料（DELETE）
* 引入 version，用于审核与加分隔离

Material 当前仅作为 **数据型领域对象**，不参与状态机。

#### Review 域（稳定）

* ReviewRecord Entity + DDL（append-only）
* ReviewRecordRepository
* 创建审核事实：ReviewService.createReviewRecord
* 查询审核记录（只读）：

  * GET /materials/{materialId}/reviews

#### 解释模型（Read / Explain Models，已完成）

以下模型 **只读、只解释、不写库、不改状态**：

* ApplicationReviewSummary
* ApplicationLifecycleSummary
* SubmissionCheckSummary
* ApplicationOverview

这些模型是前端展示、用户理解与权限判断的 **唯一数据来源**。

---

## 5. 审核与解释的执行级约定（非常重要）

### 5.1 审核的当前语义

审核 ≠ 状态迁移。

当前系统采用以下拆分：

**审核事实（Fact）**

* ReviewRecord
* 由 ReviewService 创建
* append-only，永不修改、不作废

**审核解释（Interpretation）**

* 由 ApplicationService 统一解释
* 基于 ReviewRecord 集合
* 可重复计算

ReviewService **禁止** 修改 Application 状态。

---

### 5.2 审核解释规则（当前可执行版本）

**材料级规则**

* 只评估当前 material.version
* 同一 reviewer：last-write-wins
* 不同 reviewer：

  * reviewer 数 < 2 → INCOMPLETE
  * PASS / REJECT 并存 → CONFLICT
  * 否则 → ALL_PASS / HAS_REJECT

**仲裁规则（Role v0）**

* ReviewerRole（NORMAL / ARBITER）
* 不落库、不进 domain
* 若存在 ARBITER 审核记录：

  * 直接裁决
  * 绕过 MIN_REVIEWERS 与普通冲突规则

---

### 5.3 Application 状态评估规则（保持不变）

* 任意 Material → HAS_REJECT → Application = REJECTED
* 所有 Material → ALL_PASS → Application = APPROVED
* 其他情况 → Application = UNDER_REVIEW

---

### 5.4 审核阶段合法性与终态冻结

允许创建 ReviewRecord 的状态：

* SUBMITTED
* UNDER_REVIEW

明确禁止创建 ReviewRecord 的状态：

* DRAFT
* APPROVED
* REJECTED

该规则在 ReviewService.createReviewRecord 中以 **单点防御** 实现。

---

## 6. 明确禁止在当前阶段实现的内容

以下内容 **禁止实现**：

* NEED_SUPPLEMENT 状态
* 多轮审核 / Batch
* 审核记录失效 / 复议
* 权限 / 登录 / 用户体系
* ReviewRecord 落角色 / 落状态
* 自动算分规则引擎

若需求涉及：

* 在终态继续写入审核事实
* 回滚 / 覆盖 ReviewRecord
* 在原流程中引入复议语义

→ 视为 **新时间线需求**，必须通过新模型演进，不得在现有模块内硬塞。

---

## 7. 已踩过的坑（必须避免）

以下问题真实发生过，禁止重复：

* 假设不存在的类 / 方法
* 未建模就实现
* 让 ReviewService / Controller 修改 Application 状态
* 把规则语义放进 domain
* 引入 Service 间循环依赖
* DDL 与 Entity 不一致

---

## 8. 对编码 AI 的协作期望（必须遵守）

当你（IDE AI）执行任务时，请遵循：

* 当前设计是 **可演进的中间态**，不是最终答案
* 不得引入：

  * 新状态
  * 新权限体系
  * 跨 Service 依赖

若发现现有结构不足以承载新需求：

* 停止实现
* 提出结构性调整建议
* 不得强行堆功能

---

## 9. 一句话总结（更新）

GradPath 当前已稳定形成以下工程内核：

> **审核被建模为事实（ReviewRecord） + 解释模型（ApplicationService / Read Models） + 状态投影（Application），**
> **并在此基础上引入独立的分值裁决模型（MaterialScore），**
> **为后续加分汇总、排名与公示提供清晰、可演进的数据基础。**