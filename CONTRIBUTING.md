# Contributing

感谢你考虑为 WHUerMap 提交改进。  
Thank you for contributing to WHUerMap.

## 开始之前 / Before You Start

请先查看现有 [Issues](https://github.com/thisisac/WHUerMap/issues)，避免重复
提交。对于较大的功能，请先创建 issue 讨论范围、服务端契约和兼容性影响。

Please search the [issue tracker](https://github.com/thisisac/WHUerMap/issues)
before starting work. For a larger feature, open an issue first and describe
the scope, backend contract, and compatibility impact.

## 开发流程 / Development Workflow

```bash
git clone https://github.com/thisisac/WHUerMap.git
cd WHUerMap
gradlew.bat assembleDebug
gradlew.bat test
```

从 `main` 创建功能分支，例如 `feature/improve-route-parser`。保持提交
聚焦，使用 Conventional Commits 风格，例如
`fix: handle empty route response`。

Create a feature branch from `main`, such as
`feature/improve-route-parser`. Keep commits focused and use Conventional
Commits, for example `fix: handle empty route response`.

## Pull Request / 拉取请求

- 说明改动目的、影响模块和测试方式。
- 如果改动服务端请求，请说明 WMS/WFS/GeoJSON 契约变化。
- 不提交 `local.properties`、密钥、签名文件、构建产物或个人材料。
- 确保 Debug 构建和相关测试通过。
- UI 改动请附截图或录屏，并说明测试的 Android 版本。

Pull requests should include motivation, affected modules, verification steps,
and any WMS/WFS/GeoJSON contract change. Do not commit local properties,
credentials, signing keys, build artifacts, or personal documents.

## Code Expectations

Keep Android UI work in activities/layouts, projection work in `WebMecator`,
network work in `WhuRouteTask`, and response conversion in `WhuRoute`. Avoid
blocking the main thread. Update README or `docs/` when behavior or deployment
requirements change.

