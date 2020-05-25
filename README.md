# FISCO BCOS 密钥管理服务介绍

FISCO-Key-Manager作为密钥管理服务，提供身份认证基础上的私钥托管、私钥找回、密码重置等一系列私钥管理功能。

本文档用于密钥管理服务的介绍性说明，密钥管理服务的使用请参考[部署文档](./kms_deployment.md)及[接口设计](./interfaces.md)，同时也提供[控制台](./console_manual.md)的方式访问密钥管理服务。

## 1. 使用群体

- 密钥管理服务的使用者分两类：`管理员`和`访客`。
- `管理员`负责系统的用户管理，可新增其他管理员及访客。系统内置管理员`admin`。
- `访客`进行私钥的相关操作。

## 2. 功能列表

1. 访客可将的私钥上传密钥管理服务进行托管；
2. 密钥管理服务提供访客私钥的找回及加密密码重置功能；
3. 上述操作的身份认证实现；
4. 托管的私钥包括通过国密或非国密算法生成的私钥。

密钥管理服务通过控制台进行了封装，控制台提供以下命令，面向不同使用群体。

| 控制台命令 | 描述 | 管理员 | 访客 | 调用的密钥管理服务接口 |
| ---------- | ---- | :----: |:--: | -------------------- |
| addAdminAccount   | 新增管理员账号 | √      |      | addAccount |
| addVisitorAccount | 新增访客账号   | √      |      | addAccount |
| deleteAccount     | 删除子账号     | √      |      | deleteAccount |
| listAccount       | 查询子账号列表 | √      |      | accountList |
| updatePassword    | 更改当前密码   | √      | √    | updatePassword |
| uploadPrivateKey  | 上传私钥       |        | √    | getPublicKey/addKey |
| listPrivateKey    | 查询私钥列表   |        | √    | keyList |
| exportPrivateKey  | 导出私钥       |        | √    | queryKey |
| deletePrivateKey  | 删除私钥       |        | √    | deleteKey |
| restorePrivateKey | 恢复私钥       | √      |      | queryKey |

## 3. 部署架构

在业务应用场景中，我们基于安全考虑，建议将存有私钥信息的密钥管理服务部署在企业内网，并通过Nginx反向代理实现网络隔离、多活配置及负载均衡。

![](https://fisco-bcos-doc-chaychen.readthedocs.io/en/feature-kms/_images/recommend_deployment.png)

为实现快速体验，可如下部署：

![](https://fisco-bcos-doc-chaychen.readthedocs.io/en/feature-kms/_images/simple_depolyment.png)

## 4. 私钥管理核心操作介绍

### 4.1 私钥信息表结构

```text
-- ----------------------------
-- Table structure for tb_key_info
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_key_info (
  account varchar(50) binary NOT NULL COMMENT '系统账号',
  key_alias varchar(255) NOT NULL COMMENT '私钥标识',
  cipher_text text NOT NULL COMMENT '经管理员公钥加密的私钥',
  private_key text NOT NULL COMMENT '用户托管的信息，经访客加密密码加密的私钥',
  create_time datetime DEFAULT NULL COMMENT '托管私钥的时间',
  PRIMARY KEY (account,key_alias)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='私钥信息表';
```

### 4.2 上传私钥

在业务应用场景中，用户的私钥信息以文件形式存储于本地。这种存储方式存在私钥丢失或泄露、私钥管理不便等问题，因此密钥管理服务面向访客提供私钥托管功能，保管访客上传的私钥。

访客通过控制台上传密钥管理服务的私钥信息包括：

- 私钥标识：用于唯一标识该访客下的私钥，可由访客指定，如缺失将设置为该私钥生成的以太坊地址
- 密文1：使用该访客指定的密码进行加密的密文，可用于访客导出私钥
- 密文2：使用创建该访客的管理员的公钥进行加密的密文，可用于访客遗失加密密码后由管理员恢复私钥

![](https://fisco-bcos-doc-chaychen.readthedocs.io/en/feature-kms/_images/upload_key.png)

### 4.3 导出私钥

访客在妥善保管加密密码的情况下，可自行通过控制台恢复私钥。

![](https://fisco-bcos-doc-chaychen.readthedocs.io/en/feature-kms/_images/export_key.png)

### 4.4 恢复私钥

访客如遗失加密私钥的密码，可通过管理员恢复该私钥。

![](https://fisco-bcos-doc-chaychen.readthedocs.io/en/feature-kms/_images/restore_key.png)

管理员恢复私钥的过程中将涉及与访客的交互及对访客信息的验证，上述的交互及验证流程在控制台外进行。

### 4.5 访客重置加密密码

访客可先删除密钥管理服务中已有的私钥信息，使用新密码加密私钥信息后再上传密钥管理服务，实现重置加密密码的操作。