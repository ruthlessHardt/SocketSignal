# Socket 通信Demo
## 解决问题:
> 心跳机制
> * 由客户端向服务端发送心跳💓,心跳单独一个进程
> 
> 断线重连
> * 服务端断线了，客户端每3秒请求一次连接...直到连接上服务器

<div style="text-align: center">
    <h3>心跳💓</h3>
    <img src="https://img.gejiba.com/images/7002b38e8fbe6b841ce17ae6021b342f.png" 
alt=""  style="with:550px;height: 550px;margin: 20px 0">
    <h3>断线重连</h3>
    <img src="https://img.gejiba.com/images/c5f80fdaf7f4811edf860dbeeac04d51.png" 
alt=""  style="with:550px;height: 550px;margin: 20px 0">
    <h3>服务端监测到客户端断线</h3>
    <img src="https://img.gejiba.com/images/cb3e5cf7a9f652b9cc90fbebb894a94e.png" 
alt=""  style="with:550px;height: 550px;margin: 20px 0">
</div>