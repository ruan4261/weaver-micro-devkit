package weaver.micro.devkit.http;

/**
 * 面向对象的http工具类, 用于代替CommonHttpAPI
 * 基于apache.httpclient实现
 *
 * @author ruan4261
 * @since 1.0.4
 */
public class SimpleHttpClient {

    private SimpleHttpClient() {
    }

    int connectionRequestTimeout;

    /* Usage */



    /* Builder */

    public static Builder custom() {
        return new Builder();
    }

    public static class Builder {

        final SimpleHttpClient client;

        public Builder() {
            this.client = new SimpleHttpClient();
        }

        public SimpleHttpClient build() {
            return this.client;
        }

        public void setConnectionRequestTimeout(int connectionRequestTimeout) {
            this.client.connectionRequestTimeout = connectionRequestTimeout;
        }

    }

}
