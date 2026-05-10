# 八股助手 (bagu)

Java 后端面试八股文记忆工具 — 终端 CLI，主动回忆 + SM-2 间隔重复。

## 快速开始

```bash
# 构建
mvn package -q

# 启动
./bagu           # 随机出题（优先待复习）
./bagu help      # 查看所有命令
```

## 使用指南

### 添加题目

```bash
bagu add "什么是 volatile？" "可见性、有序性、不保证原子性..." -t Java,JMM,并发 -d 3
```

支持交互式添加：

```bash
bagu add -i
```

### 刷题

```bash
bagu             # 随机一题（优先待复习题目）
bagu quiz -c 5   # Quiz 模式，连续 5 题
bagu review      # 复习到期的题目
```

流程：看题 → 心里回答 → 按 Enter 看答案 → 自评 **对/错** → SM-2 算法安排下次复习时间。

### 管理

```bash
bagu list          # 全部题目
bagu list --due    # 待复习题目
bagu list --tag JVM    # 按标签筛选
bagu list -a       # 显示详细内容
bagu stats         # 学习统计
```

## 间隔重复算法

采用 SM-2 算法：

| 结果 | 行为 |
|------|------|
| 正确 | 间隔递增：1天 → 6天 → 翻倍 → 上限180天 |
| 错误 | 重置为1天，降低 ease factor |

## 数据存储

SQLite 数据库自动保存在 `~/.bagu/bagu.db`。

## 技术栈

- Java 17
- Picocli (CLI 框架)
- SQLite (嵌入式数据库)
- Maven (构建)

## 命令速查

| 命令 | 别名 | 用途 |
|------|------|------|
| `bagu` | — | 随机出题 |
| `bagu add` | `a` | 添加题目 |
| `bagu list` | `ls`, `l` | 列出题目 |
| `bagu quiz` | `q` | Quiz 模式 |
| `bagu review` | `r` | 复习到期题目 |
| `bagu stats` | `st` | 学习统计 |
| `bagu help` | — | 帮助信息 |
