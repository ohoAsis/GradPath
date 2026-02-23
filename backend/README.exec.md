GradPath – Execution README（for IDE AI）
用途说明（非常重要）

本文档用于指导「编码执行型 AI（如 Cursor / Trae / Qoder）」参与 GradPath 项目开发。

目标只有一个：

在不破坏既有工程事实与设计共识的前提下，高效产出可演进的真实业务代码。

本 README 是当前阶段的 执行级事实源（Source of Truth）。

若与 README.human.md 冲突：

以 README.human.md 为上位约束。

1. 项目概述（What）

GradPath 是一个面向高校推免场景的：

材料提交 → 审核事实累积 → 规则解释 → 分值裁决 → 统一进度解释

的后端系统。

当前版本已经：

跑通完整审核闭环

实现材料版本隔离

实现自动审核启动

实现分值裁决模型

实现统一解释模型体系

2. 核心对象（已冻结语义）

以下语义禁止随意修改。

2.1 Application（核心聚合）

表示“一次推免申请”

唯一状态机承载者

审核与加分最终作用对象

只有 ApplicationService 可以修改其状态

2.2 Material

从属于 Application

审核直接对象

不参与状态机

引入 version 作为审核隔离边界

当前字段（语义级）：

id

applicationId

category

content

attachmentRef

declaredScore（BigDecimal）

scoreMode（NONE / DECLARED）

version

2.3 ReviewRecord（事实实体 · append-only）

表示一次审核行为

绑定 materialId + materialVersion

不包含 applicationId

不包含状态

不表达有效性

永不修改、不删除

创建规则：

DRAFT 禁止

SUBMITTED 允许（并自动启动审核）

UNDER_REVIEW 允许

APPROVED / REJECTED 禁止

2.4 MaterialScore（结果实体）

表示：

某材料某版本的最终计分裁决结果

字段语义：

materialId

materialVersion

approvedScore（BigDecimal）

decidedAt

约束：

unique(material_id, material_version)

仅当：

scoreMode == DECLARED

聚合结果 == ALL_PASS

尚未存在 score

才允许创建

MaterialScore：

不驱动流程

不修改 Application 状态

不影响审核规则

3. 状态机（当前版本）
DRAFT
→ SUBMITTED
→ UNDER_REVIEW
→ APPROVED / REJECTED
执行约束

状态单向流转

只有 ApplicationService 可修改状态

ReviewService 禁止直接 setStatus

自动审核启动发生在：

SUBMITTED 状态下创建首条 ReviewRecord 时

调用 ApplicationService.startReview()

4. 审核内核（不可破坏）
4.1 审核模型三层结构
事实层         ReviewRecord
解释层         ApplicationService 聚合规则
状态投影层     Application.status

禁止：

在 Controller 写状态判断

在 ReviewService 修改状态

在 Entity 写规则逻辑

4.2 材料版本隔离

规则：

ReviewRecord 创建时复制 material.version

聚合时只评估当前版本

修订材料时 version++

旧事实自动失效（解释层忽略）

4.3 材料修订规则

允许修订条件：

Application.status == UNDER_REVIEW

当前材料聚合结果 == HAS_REJECT

修订行为：

version++

更新 declaredScore / 内容 / 附件

不删除旧 ReviewRecord

不修改 Application 状态

4.4 审核聚合规则

材料级：

reviewer 数 < 2 → INCOMPLETE

PASS / REJECT 并存 → CONFLICT

否则 → ALL_PASS / HAS_REJECT

同一 reviewer：

last-write-wins

仲裁规则：

ARBITER 直接裁决

不落库

4.5 Application 级评估规则

任一材料 HAS_REJECT → REJECTED

全部材料 ALL_PASS → APPROVED

其他 → UNDER_REVIEW

5. 计分语义规则（重要）
5.1 scoreMode 语义
NONE

不计分材料

declaredScore 必须为 0

禁止创建 MaterialScore

不计入 total

不计入 missingScore

DECLARED

declaredScore >= 0

ALL_PASS 后可创建 MaterialScore

可进入 total 汇总

5.2 分值汇总规则

ApplicationScoreSummary：

totalApprovedScore

missingScoreMaterialIds

仅统计：

scoreMode == DECLARED
5.3 canCreateScore 规则

MaterialReviewSummary 中：

canCreateScore =
    scoreMode == DECLARED
    && aggregationResult == ALL_PASS
    && hasScore == false
6. 只读解释模型体系（禁止写操作）

以下模型只允许组装，不允许：

写库

改状态

调用 evaluateAfterReview

已存在模型：

ApplicationReviewSummary

ApplicationScoreSummary

ApplicationSubmissionCheckSummary

ApplicationOverview

ApplicationDashboard

7. 明确禁止实现的内容（当前阶段）

禁止引入：

NEED_SUPPLEMENT

多轮审核 / Batch

复议 / 回滚

审核记录失效

权限体系

自动算分规则引擎

若新需求需要以上能力：

必须新建模型

禁止在现有模块强行扩展

8. 已踩过的坑（禁止重复）

假设不存在的接口

DDL 与 Entity 不一致

Controller 写业务规则

ReviewService 修改 Application 状态

跨 Service 循环依赖

未验证 version 逻辑就写功能

9. 协作原则（对 IDE AI）

当执行任务时：

优先守住职责边界

不引入跨层耦合

不新增状态

不新增权限系统

若现有结构无法承载需求：

停止实现

提出结构性建议

不得强行堆代码

10. 当前系统定位（Day9）

GradPath 当前已不再是 CRUD 项目。

它已形成：

审核事实引擎

材料版本隔离

自动审核启动

计分语义分层

独立分值裁决模型

统一进度解释接口

这是一个：

可扩展的审核决策内核。