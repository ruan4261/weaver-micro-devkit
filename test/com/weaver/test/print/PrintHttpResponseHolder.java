package com.weaver.test.print;

import org.apache.http.impl.client.CloseableHttpClient;
import weaver.micro.devkit.http.CommonHttpAPI;
import weaver.micro.devkit.http.HttpResponseHolder;
import weaver.micro.devkit.util.VisualPrintUtils;

import java.io.IOException;
import java.net.URISyntaxException;

public class PrintHttpResponseHolder {

    public static void main(String[] args) throws IOException, URISyntaxException {
        CloseableHttpClient client = CommonHttpAPI.BUILD_DEFAULT_CLIENT();
        HttpResponseHolder holder = CommonHttpAPI.doGetWithHolder(client, "https://google.com/", null, null);
        holder.closeConnection();
        VisualPrintUtils.print(holder);
        CommonHttpAPI.close(client);
    }

}
