package com.qy.cloud.network.retrofitTool;

import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * Created by david.d on 26/01/2017.
 * RxJava errors handling
 * From http://bytes.babbel.com/en/articles/2016-03-16-retrofit2-rxjava-error-handling.html
 */
public class RetrofitException extends RuntimeException {

    public static RetrofitException httpError(String url, Response response, Retrofit retrofit) {
        String message;
        Map errorBody = null;
        try {
            errorBody = new ObjectMapper().readValue(response.errorBody().byteStream(), Map.class);
            Map errors = (Map) errorBody.get("errors");
            message = errors.get("detail").toString();
        } catch (Exception e) {
            Timber.e(e);
            message = response.message();
        }
        if (message.isEmpty()) {
            message = response.code() + " - Error";
        }
        return new RetrofitException(message, url, response, Kind.HTTP, null, retrofit, errorBody);
    }

    public static RetrofitException networkError(IOException exception) {
        return new RetrofitException(exception.getMessage(), null, null, Kind.NETWORK, exception, null, null);
    }

    public static RetrofitException jsonError(JsonProcessingException exception) {
        return new RetrofitException(exception.getMessage(), null, null, Kind.JSON, exception, null, null);
    }

    public static RetrofitException unexpectedError(Throwable exception) {
        return new RetrofitException(exception.getMessage(), null, null, Kind.UNEXPECTED, exception, null, null);
    }

    /**
     * Identifies the event kind which triggered a {@link RetrofitException}.
     */
    public enum Kind {
        /**
         * An {@link IOException} occurred while communicating to the server.
         */
        NETWORK,
        /**
         * A non-200 HTTP status code was received from the server.
         */
        HTTP,
        /**
         * A JSON processing error occurred.
         */
        JSON,
        /**
         * An internal error occurred while attempting to execute a request. It is best practice to
         * re-throw this exception so your application crashes.
         */
        UNEXPECTED
    }

    private final String url;
    private final Response response;
    private final Kind kind;
    private final Retrofit retrofit;
    private final Map errorBody;

    private RetrofitException(String message, String url, Response response, Kind kind, Throwable exception, Retrofit retrofit, Map errorBody) {
        super(message, exception);
        this.url = url;
        this.response = response;
        this.kind = kind;
        this.retrofit = retrofit;
        this.errorBody = errorBody;
    }

    /**
     * The request URL which produced the error.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Response object containing status code, headers, body, etc.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * The event kind which triggered this error.
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * The Retrofit this request was executed on
     */
    public Retrofit getRetrofit() {
        return retrofit;
    }

    /**
     * Error body
    * */
    @Nullable
    public Map getErrorBody() {
        return errorBody;
    }

    /**
     * HTTP response body converted to specified {@code type}. {@code null} if there is no
     * response.
     *
     * @throws IOException if unable to convert the body to the specified {@code type}.
     */
    @Nullable
    public <T> T getErrorBodyAs(Class<T> type) throws IOException {
        if (response == null || response.errorBody() == null) {
            return null;
        }
        Converter<ResponseBody, T> converter = retrofit.responseBodyConverter(type, new Annotation[0]);
        //noinspection ConstantConditions
        return converter.convert(response.errorBody());
    }

}