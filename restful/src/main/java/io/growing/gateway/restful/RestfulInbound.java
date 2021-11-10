//package io.growing.gateway.restful;
//
//import com.google.common.collect.Sets;
//import com.google.common.net.HttpHeaders;
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import io.growing.gateway.config.OAuth2Config;
//import io.growing.gateway.context.RuntimeContext;
//import io.growing.gateway.http.HttpApi;
//import io.growing.gateway.meta.ServiceMetadata;
//import io.growing.gateway.pipeline.Inbound;
//import io.growing.gateway.pipeline.Outgoing;
//import io.growing.gateway.restful.config.RestfulConfig;
//import io.growing.gateway.restful.enums.DataTypeFormat;
//import io.growing.gateway.restful.handler.RestfulException;
//import io.growing.gateway.restful.idl.RestfulApi;
//import io.growing.gateway.restful.idl.RestfulBuilder;
//import io.growing.gateway.restful.idl.RestfulHttpApi;
//import io.growing.gateway.restful.idl.RestfulRequest;
//import io.growing.gateway.restful.utils.RestfulConstants;
//import io.growing.gateway.restful.utils.RestfulResult;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.Operation;
//import io.swagger.v3.oas.models.PathItem;
//import io.swagger.v3.oas.models.Paths;
//import io.swagger.v3.oas.models.media.ArraySchema;
//import io.swagger.v3.oas.models.media.Schema;
//import io.swagger.v3.oas.models.media.StringSchema;
//import io.swagger.v3.oas.models.parameters.Parameter;
//import io.swagger.v3.parser.OpenAPIV3Parser;
//import io.swagger.v3.parser.core.models.SwaggerParseResult;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.http.HttpServerRequest;
//import io.vertx.core.http.HttpServerResponse;
//import io.vertx.core.json.Json;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.web.client.WebClient;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.nio.charset.StandardCharsets;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.Set;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.stream.Collectors;
//
//public class RestfulInbound implements Inbound {
//
//    private final Logger logger = LoggerFactory.getLogger(RestfulInbound.class);
//    private final AtomicReference<Set<RestfulApi>> restfulApiAtomicReference = new AtomicReference<>();
//
//    private final Gson gson;
//    private final RestfulConfig config;
//    private final WebClient webClient;
//    private final OAuth2Config oAuth2Config;
//
//    public RestfulInbound(RestfulConfig config, WebClient webClient, OAuth2Config oAuth2Config) {
//        this.config = config;
//        this.gson = new GsonBuilder().serializeNulls().create();
//        this.webClient = webClient;
//        this.oAuth2Config = oAuth2Config;
//    }
//
//    @Override
//    public void reload(List<ServiceMetadata> services, Set<Outgoing> outgoings, RuntimeContext context) {
//        RestfulBuilder restfulBuilder = RestfulBuilder.newBuilder();
//        restfulApiAtomicReference.set(restfulBuilder.outgoings(outgoings).services(services).build());
//    }
//
//    @Override
//    public Set<HttpApi> apis(List<ServiceMetadata> services) {
//        // build all service apis in progate runtime context
//        final String basePath = config.getPath();
//        Set<HttpApi> httpApis = Sets.newHashSet();
//        if (Objects.nonNull(services)) {
//            services.forEach(serviceMetadata -> {
//                serviceMetadata.restfulDefinitions().forEach(endpointDefinition -> {
//                    final OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
//                    final SwaggerParseResult swaggerParseResult = openAPIV3Parser.readContents(new String(endpointDefinition.getContent(), StandardCharsets.UTF_8));
//                    final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
//                    final Paths paths = openAPI.getPaths();
//                    if (Objects.nonNull(paths) && !paths.isEmpty()) {
//                        paths.forEach((key, pathItem) -> {
//                            final Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
//                            operationMap.forEach((httpMethod, operation) -> {
//                                final Object endpoint = operation.getExtensions().get(RestfulConstants.X_GRPC_ENDPOINT);
//                                if (Objects.isNull(endpoint)) {
//                                    throw new RestfulException("x-grpc-endpoint must defined in you proto file");
//                                }
//                                RestfulHttpApi restfulHttpApi = new RestfulHttpApi();
//                                Set<HttpMethod> methods = Sets.newHashSet();
//                                final Map<String, Schema> properties = operation.getRequestBody().getContent().get(RestfulConstants.OPENAPI_MEDIA_TYPE).getSchema().getProperties();
//                                final List<Parameter> parameters = operation.getParameters();
//                                final RestfulRequest restfulRequest = new RestfulRequest(parameters, properties);
//                                final String path = key.replace(RestfulConstants.REST_PATH_KEY, RestfulConstants.VERTX_PATH_KEY);
//                                restfulHttpApi.setPath(basePath + path);
//                                methods.add(new HttpMethod(httpMethod.name()));
//                                restfulHttpApi.setRestfulRequest(restfulRequest);
//                                restfulHttpApi.setMethods(methods);
//                                restfulHttpApi.setGrpcDefinition(endpoint.toString());
//                                restfulHttpApi.setApiResponses(operation.getResponses());
//                                httpApis.add(restfulHttpApi);
//                            });
//                        });
//                    }
//                });
//            });
//        }
//        return httpApis;
//    }
//
//    @Override
//    public void handle(HttpServerRequest request) {
//        // donating
//    }
//
//    @Override
//    public void handle(HttpApi httpApi, HttpServerRequest request) {
//        final HttpServerResponse response = request.response();
//        if (Objects.isNull(httpApi)) {
//            response.end(gson.toJson(RestfulResult.error("Illegal Request")));
//        }
//        request.pause();
//        final String oauthToken = request.getHeader(RestfulConstants.AUTHORIZE);
//        logger.info("restful request entrance，request：{},head：{}", httpApi, oauthToken);
//        if (StringUtils.isBlank(oauthToken)) {
//            response.end(gson.toJson(RestfulResult.error(RestfulConstants.TOKEN_AUTHORIZER_FIAL)));
//        }
//        // validate oauth2 token
//        webClient.get(oAuth2Config.getAuthServer(), oAuth2Config.getTokenCheckUrl())
//            .addQueryParam(RestfulConstants.TOKEN, oauthToken)
//            .bearerTokenAuthentication(oauthToken)
//            .send()
//            .onSuccess(handler -> {
//                logger.info("token authorizer success。:{}", handler);
//                request.resume();
//                final JsonObject jsonObject = handler.bodyAsJsonObject();
//                final String clientId = jsonObject.getString(RestfulConstants.CLIENT_ID);
//                doHandle(httpApi, request, clientId);
//            })
//            .onFailure(error -> {
//                logger.error(RestfulConstants.TOKEN_AUTHORIZER_FIAL, error);
//                response.end(gson.toJson(RestfulResult.error(RestfulConstants.TOKEN_AUTHORIZER_FIAL)));
//            });
//
//    }
//
//    /***
//     * @description: 解析执行
//     **/
//    private void doHandle(HttpApi httpApi, HttpServerRequest request, String clientId) {
//        request.bodyHandler(handle -> {
//            HttpServerResponse response = request.response();
//            response.headers().set(HttpHeaders.CONTENT_TYPE, RestfulConstants.CONTENT_TYPE);
//            Map<String, Object> params = new HashMap<>();
//            if (Objects.nonNull(handle)) {
//                params = Json.decodeValue(handle, Map.class);
//            }
//            Map<String, Object> finalParams = params;
//            request.params().forEach(param -> finalParams.put(param.getKey(), param.getValue()));
//            finalParams.put(RestfulConstants.X_REQUEST_ID, request.getHeader(RestfulConstants.X_REQUEST_ID));
//            if (httpApi instanceof RestfulHttpApi) {
//                final RestfulHttpApi restfulHttpApi = (RestfulHttpApi) httpApi;
//                final String projectId = request.getParam(RestfulConstants.PROJECT_KEY);
//                if (StringUtils.isNotBlank(projectId)) {
//                    finalParams.put(RestfulConstants.PROJECT_KEY, projectId);
//                }
//                paramsTranscode(restfulHttpApi.getRestfulRequest(), finalParams);
//                Optional<RestfulApi> restfulApi = restfulApiAtomicReference.get().stream().filter(api -> {
//                    return api.getGrpcDefinition().equalsIgnoreCase(restfulHttpApi.getGrpcDefinition());
//                }).collect(Collectors.toList()).stream().findFirst();
//                if (restfulApi.isPresent()) {
//                    final CompletableFuture<Object> completableFuture = restfulApi.get().execute(config.getPath(), restfulHttpApi, finalParams);
//                    completableFuture.whenComplete((result, throwable) -> {
//                        if (Objects.nonNull(throwable)) {
//                            logger.warn("request exception: {}", httpApi.getPath());
//                            response.end(gson.toJson(RestfulResult.error(throwable.getMessage())));
//                        }
//                        response.end(gson.toJson(result));
//                    });
//                } else {
//                    logger.warn("restful current request path not allowed : {}", httpApi.getPath());
//                    response.end(gson.toJson(RestfulResult.error("restful current request path not allowed")));
//                }
//            } else {
//                logger.error("current request path is not a restful request，please check");
//                response.end(gson.toJson(RestfulResult.error("current request path is not a restful request，please check")));
//            }
//        });
//    }
//
//    /***
//     * @description: 说明在做参数处理的时候，有可能存在套娃的情况存在，目前只处理参数本身，套娃不处理（无法估计到参数嵌套的层数）。
//    无法估计到参数嵌套的层数     **/
//    private void paramsTranscode(RestfulRequest restfulRequest, Map<String, Object> params) {
//        if (Objects.nonNull(restfulRequest.getMaps()) && !restfulRequest.getMaps().isEmpty()) {
//            restfulRequest.getMaps().forEach((key, schema) -> {
//                if (schema instanceof StringSchema) {
//                    final String format = StringUtils.isBlank(schema.getFormat()) ? "" : schema.getFormat();
//                    if (DataTypeFormat.HASHID.getName().equalsIgnoreCase(format)) {
//                        params.put(key,"//TODO" );//hashIdCodec.decode(params.get(key).toString()));
//                    }
//                }
//                if (schema instanceof ArraySchema) {
//                    final ArraySchema arraySchema = (ArraySchema) schema;
//                    arraySchemaWrapper(arraySchema, params, key);
//                }
//            });
//        }
//        if (Objects.nonNull(restfulRequest.getParameters()) && !restfulRequest.getParameters().isEmpty()) {
//            restfulRequest.getParameters().forEach(parameter -> {
//                final Schema parameterSchema = parameter.getSchema();
//                if (parameterSchema instanceof StringSchema) {
//                    final String format = StringUtils.isBlank(parameterSchema.getFormat()) ? "" : parameterSchema.getFormat();
//                    if (DataTypeFormat.HASHID.getName().equalsIgnoreCase(format)) {
//                        params.put(parameter.getName(), "//TODO");//hashIdCodec.decode(params.get(parameter.getName()).toString()));
//                    }
//                }
//                if (parameterSchema instanceof ArraySchema) {
//                    final ArraySchema arraySchema = (ArraySchema) parameterSchema;
//                    arraySchemaWrapper(arraySchema, params, parameter.getName());
//                }
//            });
//        }
//    }
//
//    private void arraySchemaWrapper(ArraySchema arraySchema, Map<String, Object> params, String key) {
//        final String format = StringUtils.isBlank(arraySchema.getItems().getFormat()) ? "" : arraySchema.getItems().getFormat();
//        if (DataTypeFormat.HASHID.getName().equalsIgnoreCase(format)) {
//            Long[] decodes = new Long[]{};
//            final String[] originals = params.get(key).toString().split(",");
//            for (int i = 0; i < originals.length; i++) {
//                decodes[i] = -1L;// hashIdCodec.decode(originals[i]);
//            }
//            params.put(key, decodes);
//        }
//    }
//
//}