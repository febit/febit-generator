Febit Generator -- 只是个生成代码的工具
====

## 编译方法

~~~~~
mvn install
~~~~~

使用方法：

+ 配置 generator.props
  设置数据源，用户名，密码，输出文件夹，基本包名，版权信息等等，缺省是Mysql的驱动 其他数据库暂未测试

+ （可选）运行init-config.bat
  这一步是采集数据表，生成列表，可以对每列进行特殊配置

+ 运行generator.bat 生成代码

> 1. 生成的过程中会产生日志，查看日志文件即可了解生成情况

> 2. 如果发现生成的内容不正常 可以在命令行中执行这两个批处理 查看请详细的错误日志

## 配置说明

+ 配置文件请参考 `generator-core-xx.jar` 中的 generator-default.props

## License

Febit Generator is distributed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Bug report

> [github-issue][new_issue_github]

[new_issue_github]: https://github.com/febit/febit-generator/issues/new

[license]: https://github.com//blob/master/LICENSE

