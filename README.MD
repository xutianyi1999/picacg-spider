# picage-spider
哔咔漫画收藏夹抓取

## Config
```json
{
  "email": "xxx",
  "password": "123",
  "imgDirectory": "D:\\pic",
  "startPageIndex": 1,
  "socks5": {
    "isEnable": true,
    "host": "127.0.0.1",
    "port": 1080
  }
}
```
- email: 邮箱/用户名
- password: 密码
- imgDirectory: 下载图片目录
- startPageIndex: 抓取起始页数（从旧到新）
- socks5
    - isEnable: 是否开启

## Usages
与picacg-spider.jar同级目录创建config.json
```shell script
java -jar picacg-spider.jar
```
