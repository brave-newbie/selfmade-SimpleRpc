# Git 仓库提交计划 (已获取远程地址)

太好了，既然你已经有了仓库地址 `https://github.com/brave-newbie/selfmade-SimpleRpc.git`，那我们就直接一气呵成！

## 1. 准备工作：创建 .gitignore
首先，我们要屏蔽掉 `target/`, `.idea/`, `.vscode/`, `*.class` 等垃圾文件，保持仓库干净。

## 2. 本地操作
*   `git init`：初始化仓库。
*   `git add .`：添加所有文件。
*   `git commit -m "Initial commit: High-performance RPC framework based on Netty+Zookeeper+Spring"`：提交代码。

## 3. 推送远程
*   `git branch -M main`：将分支重命名为 main（GitHub 现在默认主分支叫 main）。
*   `git remote add origin https://github.com/brave-newbie/selfmade-SimpleRpc.git`：关联你的远程仓库。
*   `git push -u origin main`：把代码推上去。

我会帮你执行所有命令。如果遇到权限验证（比如需要登录 GitHub），终端可能会提示你输入账号密码或者 Token，到时候需要你配合一下。

准备开始了吗？
