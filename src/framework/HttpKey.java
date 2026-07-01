package framework;

public class HttpKey {

    private String url;
    private String method; // GET ou POST

     @Override
        public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpKey)) return false;

        HttpKey other = (HttpKey) o;

        return url.equals(other.url)
                && method.equals(other.method);
        }

        @Override
        public int hashCode() {
        return url.hashCode() + method.hashCode();
        }

    public HttpKey(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }
}