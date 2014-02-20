Webit Generator -- 这是一个自动生成代码的工具
====
<a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=7be9d8a59a8533b7c2837bdc22295b4b47c65384eda323971cf5f3b9943ad9db"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="QQ群: 302505483" title="QQ群: 302505483" /></a>


> 如果您在使用或者定制自己的模版的时候遇到问题欢迎加入到Webit Script的QQ群进行讨论。

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

## 开源协议

> **Webit Generator** 依据 BSD许可证发布。详细请看 [LICENSE][license] 文件。

## Bug report

> [github-issue][new_issue_github]

[new_issue_github]: https://github.com/zqq90/webit-generator/issues/new
[new_issue_osc]: http://git.oschina.net/zqq90/webit-generator/issues/new

[license]: https://github.com/zqq90/webit-generator/blob/master/LICENSE

