package com.hissp.distribution.module.infra.framework.file.core.client.s3;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.hissp.distribution.module.infra.framework.file.core.client.AbstractFileClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;

/**
 * 基于 S3 协议的文件客户端，实现 MinIO、阿里云、腾讯云、七牛云、华为云等云服务
 * <p>
 * S3 协议的客户端，采用亚马逊提供的 software.amazon.awssdk.s3 库
 *
 * @author 芋道源码
 */
public class S3FileClient extends AbstractFileClient<S3FileClientConfig> {

    private S3Client client;
    private S3Presigner presigner;
    private Region region;
    private URI endpointUri;

    public S3FileClient(Long id, S3FileClientConfig config) {
        super(id, config);
    }

    @Override
    protected void doInit() {
        // 补全 domain
        if (StrUtil.isEmpty(config.getDomain())) {
            config.setDomain(buildDomain());
        }
        AwsBasicCredentials credentials = AwsBasicCredentials.create(config.getAccessKey(), config.getAccessSecret());
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        region = Region.of(resolveRegion());
        endpointUri = buildEndpointUri();

        S3ClientBuilder clientBuilder = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .region(region);

        S3Configuration.Builder serviceConfiguration = S3Configuration.builder();
        if (shouldUsePathStyle(config.getEndpoint())) {
            serviceConfiguration.pathStyleAccessEnabled(true);
        }
        clientBuilder.serviceConfiguration(serviceConfiguration.build());

        if (endpointUri != null) {
            clientBuilder.endpointOverride(endpointUri);
        }
        client = clientBuilder.build();

        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region);
        if (endpointUri != null) {
            presignerBuilder.endpointOverride(endpointUri);
        }
        presigner = presignerBuilder.build();
    }

    /**
     * 基于 bucket + endpoint 构建访问的 Domain 地址
     *
     * @return Domain 地址
     */
    private String buildDomain() {
        // 如果已经是 http 或者 https，则不进行拼接.主要适配 MinIO
        if (HttpUtil.isHttp(config.getEndpoint()) || HttpUtil.isHttps(config.getEndpoint())) {
            return StrUtil.format("{}/{}", config.getEndpoint(), config.getBucket());
        }
        // 阿里云、腾讯云、华为云都适合。七牛云比较特殊，必须有自定义域名
        return StrUtil.format("https://{}.{}", config.getBucket(), config.getEndpoint());
    }

    @Override
    public String upload(byte[] content, String path, String type) throws Exception {
        // 元数据，主要用于设置文件类型
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .contentType(type)
                .contentLength((long) content.length)
                .build();
        client.putObject(request, RequestBody.fromBytes(content));

        // 拼接返回路径
        return config.getDomain() + "/" + path;
    }

    @Override
    public void delete(String path) throws Exception {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        client.deleteObject(request);
    }

    @Override
    public byte[] getContent(String path) throws Exception {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        ResponseBytes<GetObjectResponse> response = client.getObjectAsBytes(request);
        return response.asByteArray();
    }

    @Override
    public FilePresignedUrlRespDTO getPresignedObjectUrl(String path) throws Exception {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(config.getBucket())
                .key(path)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();
        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
        return new FilePresignedUrlRespDTO(presignedRequest.url().toString(), config.getDomain() + "/" + path);
    }

    private String resolveRegion() {
        if (StrUtil.isNotEmpty(config.getRegion())) {
            return config.getRegion();
        }
        String host = extractHost(config.getEndpoint());
        String regionId = extractRegionFromHost(host);
        if (StrUtil.isNotEmpty(regionId)) {
            return regionId;
        }
        return Region.US_EAST_1.id();
    }

    private URI buildEndpointUri() {
        String endpoint = config.getEndpoint();
        if (StrUtil.isEmpty(endpoint)) {
            return null;
        }
        String target = endpoint;
        if (!HttpUtil.isHttp(endpoint) && !HttpUtil.isHttps(endpoint)) {
            target = "https://" + endpoint;
        }
        try {
            return URI.create(target);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean shouldUsePathStyle(String endpoint) {
        if (!HttpUtil.isHttp(endpoint) && !HttpUtil.isHttps(endpoint)) {
            return false;
        }
        try {
            URI uri = URI.create(endpoint);
            String host = uri.getHost();
            if (StrUtil.isEmpty(host)) {
                return false;
            }
            if ("localhost".equalsIgnoreCase(host) || Validator.isIpv4(host) || Validator.isIpv6(host)) {
                return true;
            }
            return uri.getPort() != -1;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String extractHost(String endpoint) {
        if (StrUtil.isEmpty(endpoint)) {
            return null;
        }
        if (HttpUtil.isHttp(endpoint) || HttpUtil.isHttps(endpoint)) {
            try {
                return URI.create(endpoint).getHost();
            } catch (IllegalArgumentException ex) {
                return endpoint;
            }
        }
        return endpoint;
    }

    private String extractRegionFromHost(String host) {
        if (StrUtil.isEmpty(host)) {
            return null;
        }
        String lowerHost = host.toLowerCase();
        if (lowerHost.endsWith(".aliyuncs.com")) {
            String prefix = StrUtil.subBefore(lowerHost, ".aliyuncs.com", false);
            int idx = prefix.indexOf("oss-");
            if (idx >= 0 && idx + 4 < prefix.length()) {
                return prefix.substring(idx + 4);
            }
        }
        if (lowerHost.endsWith(".myqcloud.com")) {
            String prefix = StrUtil.subBefore(lowerHost, ".myqcloud.com", false);
            String[] segments = prefix.split("\\.");
            if (segments.length > 0) {
                String candidate = segments[segments.length - 1];
                if (isRegionCandidate(candidate)) {
                    return candidate;
                }
            }
        }
        if (lowerHost.endsWith(".qiniucs.com")) {
            String prefix = StrUtil.subBefore(lowerHost, ".qiniucs.com", false);
            if (prefix.startsWith("s3-") && prefix.length() > 3) {
                String candidate = prefix.substring(3);
                if (isRegionCandidate(candidate)) {
                    return candidate;
                }
            }
        }
        if (lowerHost.endsWith(".myhuaweicloud.com")) {
            String prefix = StrUtil.subBefore(lowerHost, ".myhuaweicloud.com", false);
            String[] segments = prefix.split("\\.");
            if (segments.length > 0) {
                String candidate = segments[segments.length - 1];
                if (isRegionCandidate(candidate)) {
                    return candidate;
                }
            }
        }
        if (lowerHost.endsWith(".volces.com")) {
            String prefix = StrUtil.subBefore(lowerHost, ".volces.com", false);
            int idx = prefix.indexOf('-');
            if (idx >= 0 && idx + 1 < prefix.length()) {
                String candidate = prefix.substring(idx + 1);
                if (isRegionCandidate(candidate)) {
                    return candidate;
                }
            }
        }
        if (lowerHost.contains("amazonaws.com")) {
            String prefix = lowerHost;
            if (lowerHost.contains(".amazonaws.com.cn")) {
                prefix = StrUtil.subBefore(lowerHost, ".amazonaws.com.cn", false);
            } else if (lowerHost.contains(".amazonaws.com")) {
                prefix = StrUtil.subBefore(lowerHost, ".amazonaws.com", false);
            }
            String[] segments = prefix.split("\\.");
            for (int i = segments.length - 1; i >= 0; i--) {
                if (isRegionCandidate(segments[i])) {
                    return segments[i];
                }
            }
        }
        return null;
    }

    private boolean isRegionCandidate(String value) {
        return StrUtil.isNotEmpty(value) && value.contains("-");
    }

}
