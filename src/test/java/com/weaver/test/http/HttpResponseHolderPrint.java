package com.weaver.test.http;

import org.apache.http.impl.client.CloseableHttpClient;
import weaver.micro.devkit.http.CommonHttpAPI;
import weaver.micro.devkit.http.HttpResponseHolder;
import weaver.micro.devkit.util.VisualPrintUtils;

import java.io.FileOutputStream;
import java.io.IOException;

public class HttpResponseHolderPrint {

    /**
     * 卧槽, 竟然有两百兆
     */
    public static void main(String[] args) throws IOException {
        CloseableHttpClient client = null;
        try {
            client = CommonHttpAPI.BUILD_DEFAULT_CLIENT();
            HttpResponseHolder httpResponseHolder = CommonHttpAPI.doGetWithHolder(
                    client,
                    "https://www.baidu.com/",
                    null,
                    null
            );
            httpResponseHolder.closeConnection();
            VisualPrintUtils.print(httpResponseHolder, new FileOutputStream("/Users/a4261/Downloads/http2.txt"));

            if (!httpResponseHolder.isResolveSuccess()) {
                throw httpResponseHolder.getResolveException();
            }
        } catch (Throwable ignored) {
        } finally {
            if (client != null)
                client.close();
        }
    }

}
