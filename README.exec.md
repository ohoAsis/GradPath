GradPath – Execution README（for IDE AI）
用途说明（非常重要）

本文档用于指导「编码执行型 AI（如 Cursor / Trae / Qoder）」参与 GradPath 项目开发。

目标只有一个：
在 不破坏既有工程事实与设计共识 的前提下，高效产出 可演进的真实业务代码。

本 README 是当前阶段的 执行级事实源（Source of Truth）。
所有代码生成与修改，必须以本文档为准。
若与 README.human.md 冲突，以 README.human.md 的设计共识为上位约束。

1. 项目概述（What）

GradPath 是一个面向高校推免（保研）场景的 材料提交与审核管理系统（后端）。

业务背景来自真实院系流程：

学生提交多类加分材料
→ 多名审核员对材料进行审核
→ 审核事实累积
→ 系统基于规则评估并给出最终申请结果

项目完整业务背景参考文档：

GradPath 保研加分材料线上审核平台 – 产品分析与设计文档

2. 当前已实现的核心设计逻辑（Design Logic）
2.1 核心对象（已冻结，不可随意修改）
Application

表示“一次学生的推免申请”

当前系统中的 核心业务聚合

唯一状态机承载者

审核的最终结论 只作用于 Application

Material

从属于 Application

表示“可被审核的材料证据”

审核的 直接对象

不参与 Application 状态机

当前已引入 version，用于审核隔离

ReviewRecord（已实现，append-only）

独立的审核事实实体

表示“一次已经发生的审核行为”

append-only（只新增，不修改、不删除）

针对 Material + Material.version

不直接驱动状态变化

不表达是否有效、是否生效

ReviewRecord 只表达一句话：

“某个审核员，在某个时间，对某个材料的某个版本，做了一次判断。”

2.2 Application 状态机（当前版本）
DRAFT
→ SUBMITTED
→ UNDER_REVIEW
→ APPROVED / REJECTED


执行约束（必须遵守）

状态单向流转

Application 状态 禁止在 ReviewService 中修改

状态迁移 仅允许发生在 ApplicationService

状态评估通过 evaluateAfterReview(applicationId) 显式触发

3. 当前项目进展（Where we are）
已完成（事实）
Application

创建（DRAFT）

提交（SUBMITTED）

进入审核（UNDER_REVIEW）

基于审核事实的最终通过 / 驳回（APPROVED / REJECTED）

Material 子域（最小闭环）

Material Entity + DDL

按 Application 查询材料（GET）

新增材料（POST）

删除材料（DELETE）

引入 version，用于审核隔离

Material 仅作为 数据型领域对象，不参与状态机

Review 域（Day 6 后稳定）

ReviewRecord Entity + DDL（append-only）

ReviewRecordRepository

创建审核事实：ReviewService.createReviewRecord

查询审核记录（只读）：

GET /materials/{materialId}/reviews

ApplicationService 中已实现的审核解释能力

evaluateAfterReview(applicationId) 显式评估状态

材料版本隔离（只评估当前 version）

同一审核员 last-write-wins

最小审核人数约束（MIN_REVIEWERS = 2）

冲突识别（PASS / REJECT 并存）

内部阻塞原因区分（人数不足 / 冲突）

冲突可裁决（仲裁一票定音，Role v0）

尚未实现（明确推迟）

以下内容 禁止在当前阶段实现：

NEED_SUPPLEMENT 状态

多轮审核 / 批次（Batch）

审核记录失效 / 复议

权限 / 登录 / 用户体系

ReviewRecord 落角色 / 落状态

MaterialStatus / 材料级状态机

4. 项目结构（How it is organized）
edu.xmu.gradpath
├── application
│   ├── controller
│   ├── service
│   ├── repository
│   └── domain
│
├── material
│   ├── controller
│   ├── service
│   ├── repository
│   └── domain
│
├── review
│   ├── domain          // ReviewRecord, ReviewDecision（事实）
│   ├── repository     // ReviewRecordRepository
│   ├── service        // 仅负责创建审核事实
│   └── controller     // 只读查询接口
│
├── common
│   ├── response
│   └── exception
│
├── auth        // 占位，未实现
└── user        // 占位，未实现

结构原则（当前事实）

Controller：HTTP 接口与参数解析

Service：业务规则、流程控制、解释逻辑

Repository：数据库访问

Entity / Domain：事实与领域数据

5. 当前审核业务逻辑约定（非常重要）
5.1 审核的当前语义

审核 ≠ 状态迁移

当前系统采用以下拆分：

审核事实（Fact）

由 ReviewRecord 表示

由 ReviewService 创建

append-only

永不修改、不作废

审核结论（Interpretation）

由 ApplicationService 解释

基于 ReviewRecord 的集合

显式触发、可重复计算

ReviewService 禁止 修改 Application 状态。

5.2 审核解释规则（当前可执行版本）
材料级规则（Material）

只评估当前 material.version

同一 reviewer：last-write-wins

不同 reviewer：

reviewer 数 < 2 → INCOMPLETE

PASS / REJECT 并存 → CONFLICT

否则 → ALL_PASS / HAS_REJECT

仲裁规则（Role v0）

引入 ReviewerRole（NORMAL / ARBITER）

ReviewerRole 不落库、不进 domain

仅存在于 ApplicationService 的解释规则中

若存在 ARBITER 的审核记录：

直接裁决

绕过 MIN_REVIEWERS 与普通冲突规则

5.3 Application 状态评估规则（保持不变）

任意 Material → HAS_REJECT
→ Application = REJECTED

所有 Material → ALL_PASS
→ Application = APPROVED

其他情况
→ Application = UNDER_REVIEW

5.4 审核阶段合法性与终态冻结规则

为保证审核流程的语义一致性，当前系统已补齐审核事实写入的阶段边界规则：

允许创建 ReviewRecord 的 Application 状态：

SUBMITTED

UNDER_REVIEW

明确禁止创建 ReviewRecord 的 Application 状态：

DRAFT

APPROVED

REJECTED

该规则统一在 ReviewService.createReviewRecord 中以单点防御方式实现。

语义说明：

审核事实仅允许在“审核阶段”内产生

一旦 Application 进入终态（APPROVED / REJECTED），即视为本轮审核流程结束

终态下禁止继续写入新的审核事实，但历史事实仍可查询与解释

6. 已踩过的坑（必须避免）

以下问题 真实发生过，禁止重复：

假设不存在的类 / 方法

未建模就实现

让 ReviewService / Controller 修改 Application 状态

把规则语义放进 domain

引入 Service 间循环依赖

DDL 与 Entity 不一致

这些是 已验证的失败路径。

7. 演进方向（仅供参考，不是执行指令）
短期（下一阶段）

完善 SUBMITTED 的边界规则

明确材料何时允许 version++

为 NEED_SUPPLEMENT 做结构准备（不实现）

中期

审核轮次 / 批次（Batch）

审核历史与最终结论进一步解耦

长期

权限 / 用户体系

批次公示

评分与历史追溯

补充说明：审核模块当前已完成一次完整生命周期的建模与封口。

后续任何新增需求，若涉及以下行为：

在终态继续写入审核事实

回滚、覆盖或作废既有 ReviewRecord

在原审核流程中引入复议语义

均视为“新时间线需求”，不得在当前审核模块内直接实现。

如需支持上述能力，应通过：

新一轮审核（Batch / Round）

新的 Application 实例

等方式演进，而不是在旧流程中回写事实。

8. 对编码 AI 的协作期望（必须遵守）

当你（IDE AI）执行任务时，请遵循：

当前设计是 可演进的中间态

不得引入：

新状态

新表

新权限体系

跨 Service 依赖

若发现现有结构不足以承载新需求：

停止实现

提出结构性调整建议

不得强行堆功能

9. 一句话总结（更新）

GradPath 已将审核流程稳定建模为：

事实（ReviewRecord） + 解释规则（ApplicationService） + 状态投影（Application），

并显式限定审核事实的写入窗口，仅允许在审核阶段内产生，终态冻结事实写入，

这是一个已完成阶段性封口、可持续演进的工程内核，而非 CRUD 练习。