本地测试的resource.properties中配置文件为:
#工作目录
WORKDIR=F:\\nettyFile\\workdir
#文件要写入的目录
file_write_path=F:\\nettyFile
#发布出来的url
file_base_url=http://192.168.1.8
#每次预读取的字节数
readBufferSize=16384
#每次最少读取的字节数
minReadBufferSize=8192
#每次最大读取的字节数
maxReadBufferSize=32678
#端口号
port=10012


在nginx中的配置如下:
location ^~ /testApp/ {
           root   F:/nettyFile/0/;

        }
        
生成的图片路径格式如下:        
http://192.168.1.8/testApp/86384658-552c-4cf2-bbd4-8f447c92132d.jpg

经测试，均可正常访问。        