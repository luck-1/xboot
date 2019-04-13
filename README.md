# xboot-plus
- 若更新代码造成报错 请清除Redis缓存 
- 文档见 https://github.com/Exrick/x-boot
- 未经授权请勿直接开二次发，仅提供学习参考，该版本不授权任何人以任何形式使用，作者保留所有权利，否则后果自负。
### [有偿贡献](https://gitlab.com/Exrick/xboot-plus/blob/master/CONTRIBUTING.md)
### Nginx 配置提醒
```
由于路由默认已使用history模式 需加入以下配置

location / {
	if (!-e $request_filename) {
        rewrite ^(.*)$ /index.html?s=$1 last;
        break;
    }
    ...
}

上传文件过大出现413错误 需加入以下配置

client_max_body_size 5m;

关键配置如下

server {
	listen       80;
         server_name  localhost;
         location / {
		if (!-e $request_filename) {
            	rewrite ^(.*)$ /index.html?s=$1 last;
            	break;
        		}

            root   xboot;
            index  index.html;
        }

	location /xboot/ {  
	    proxy_pass http://127.0.0.1:8888;
	}

	proxy_redirect off;
	proxy_set_header Host $host;
	proxy_set_header X-Real-IP $remote_addr;
	proxy_set_header X-Forwarded $proxy_add_x_forwarded_for;

	client_max_body_size 5m;
}

```
