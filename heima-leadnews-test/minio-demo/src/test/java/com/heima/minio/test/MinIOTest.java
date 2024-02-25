package com.heima.minio.test;


import com.heima.file.service.FileStorageService;
import com.heima.minio.MinIOApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


@SpringBootTest(classes = MinIOApplication.class)
@RunWith(SpringRunner.class)
public class MinIOTest {




    @Autowired
    private FileStorageService fileStorageService;

    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("D:\\leadnews\\leadnews.html");
        String path = fileStorageService.uploadHtmlFile("", "leadnews.html", fileInputStream);
        System.out.println(path);
    }


    /**
     * 上传文件 把list.html上传到minio中，并且可以访问
     * @param args
     */
    public static void main(String[] args) {
        try {
            FileInputStream fileInputStream = new FileInputStream("G:\\springcloud\\头条\\黑马头条\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\js\\axios.min.js");
            //1.或取minio的连接信息 创建一个Minio的客户端
            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123")
                    .endpoint("http://192.168.200.130:9000").build();
            //2.上传文件
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("plugins/js/axios.min.js")
                    .contentType("text/js")
                    .bucket("leadnews")
                    .stream(fileInputStream,fileInputStream.available(),-1).build();
            minioClient.putObject(putObjectArgs);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}