GradPath – Execution README（for IDE AI）
用途说明

本文档用于指导「编码执行型 AI（如 Cursor / Trae / Qoder）」参与 GradPath 项目开发。
目标是：在尊重既有设计逻辑与工程事实的前提下，高效产出可演进的真实业务代码。

本 README 不是架构宪法，而是当前执行视角下的项目事实源（Source of Truth）。
所有代码生成与修改，必须以本文档为准。

1. 项目概述（What）

GradPath 是一个面向高校推免（保研）场景的 材料提交与审核管理系统（后端）。

业务背景来源于真实院系流程：

学生提交多类加分材料
→ 多名审核员对材料进行审核
→ 审核事实累积
→ 系统基于规则给出最终申请结果

项目完整业务背景与产品分析参考文档：

GradPath 保研加分材料线上审核平台 – 产品分析与设计文档

2. 当前已实现的核心设计逻辑（Design Logic）
2.1 核心对象（已冻结）
Application

表示“一次学生的推免申请”

当前系统中的核心业务聚合

唯一状态机承载者

审核的最终结论作用于 Application

Material

从属于 Application 的领域对象

表示“可被审核的材料证据”

审核的直接对象

不参与 Application 状态机

ReviewRecord（已实现）

独立的审核事实实体

表示“一次已经发生的审核行为”

append-only（只新增，不修改）

针对 Material，而不是 Application

不直接驱动状态变化

2.2 Application 状态机（当前版本）
DRAFT
→ SUBMITTED
→ UNDER_REVIEW
→ APPROVED / REJECTED

含义说明

状态单向流转

Application 状态 不在 ReviewService 中修改

状态迁移仅发生在 ApplicationService

3. 当前项目进展（Where we are）
已完成（事实）
Application

创建、提交

进入审核（UNDER_REVIEW）

基于审核事实的最终通过 / 驳回

Material 子域（最小闭环）

Material Entity + DDL

按 Application 查询材料（GET）

新增材料（POST）

删除材料（DELETE）

Material 仅作为数据型领域对象存在，不参与状态机

Review 域（Day 5 关键进展）

ReviewRecord Entity + DDL（append-only）

ReviewRecordRepository

创建审核事实（ReviewService.createReviewRecord）

按 Material 查询审核记录：

GET /materials/{materialId}/reviews


ApplicationService 中实现：

evaluateAfterReview(applicationId)


使用 ReviewRecord 的事实集合，显式评估并更新 Application 状态

尚未实现（但已明确规划）

多审核员投票 / 冲突规则

多轮审核 / 批次（Batch）

NEED_SUPPLEMENT 状态（当前阶段明确不引入）

审核记录失效 / 复议

权限 / 用户体系

Material 的以下能力：

MaterialStatus

材料级审核 / 评分

材料修改（UPDATE）

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
│   ├── domain          // ReviewRecord, ReviewDecision
│   ├── repository     // ReviewRecordRepository
│   ├── service        // 创建审核事实
│   └── controller     // 只读查询接口
│
├── common
│   ├── response
│   └── exception
│
├── auth            // 占位，未实现
└── user            // 占位，未实现

结构原则（当前事实）

Controller：HTTP 接口与参数解析

Service：业务动作、规则评估

Repository：数据库访问

Entity：领域数据与事实承载

5. 当前业务逻辑约定（Business Logic）
5.1 审核的当前语义（重要更新）

审核 ≠ 状态迁移

当前系统采用以下拆分：

审核事实

由 ReviewRecord 表示

由 ReviewService 创建

审核结论

由 ApplicationService 评估

通过 evaluateAfterReview(applicationId) 显式触发

ReviewService 禁止修改 Application 状态。

5.2 Application 状态评估规则（v1，已实现）

只要存在任意一条 ReviewRecord.decision = REJECT
→ Application → REJECTED

当且仅当：

Application 下所有 Material

至少有 1 条 ReviewRecord

且所有 decision = PASS
→ Application → APPROVED

其他情况：

Application 保持 UNDER_REVIEW

5.3 关于 NEED_SUPPLEMENT（当前结论）

当前阶段 明确不引入 NEED_SUPPLEMENT

原因：

该状态隐含材料版本、审核轮次、审核失效规则

当前结构尚未具备承载条件

禁止仅通过“新增枚举值”的方式引入该状态

6. 已踩过的坑（Important Lessons）

以下问题真实发生过，必须避免重复：

假设不存在的类 / 方法
（在未建模前假设 ReviewRecord、Snapshot 等存在）

实现先于建模
（代码先行导致结构被迫迁就）

职责漂移
（Controller / ReviewService 承担状态判断）

DDL 与 Entity 不一致
（未明确表结构即引入 Entity）

这些不是“禁止事项”，而是已验证的失败路径。

7. 演进方向（Where this is going）
短期（下一阶段）

在现有 ReviewRecord 基础上：

引入多审核员规则

明确冲突 / 投票策略

为 NEED_SUPPLEMENT 做结构级准备（非直接实现）

中期

引入审核轮次 / 批次（Batch）

审核记录与最终结论进一步解耦

审核历史完整留档

长期

批次公示（Announcement）

权限 / 角色体系

评分汇总与历史追溯

演进将是版本化的，目标是完整系统，而非停留在简陋 MVP。

8. 对编码 AI 的协作期望（How to Work）

当你（IDE AI）执行编码任务时，请遵循以下默认假设：

当前设计是 可演进的中间态

允许新增模块 / 实体，但必须：

职责清晰

不破坏 Application 状态机语义

若发现当前结构不足以承载新需求：

提出结构调整建议

而不是在原结构上强行堆功能

9. 一句话总结

GradPath 不是一个“CRUD 练习项目”，
而是一个已经把“审核”建模为
“事实 + 规则 + 状态评估”的可演进工程样本。