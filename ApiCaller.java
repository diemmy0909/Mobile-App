package com.example.helloworld;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.helloworld.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ApiCaller {
    private static final String TAG = "ApiCaller";
    private static final String BASE_URL = "http://192.168.100.115:8080"; // đổi IP theo máy bạn
    private final RequestQueue requestQueue;

    public ApiCaller(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface OnRawResponse {
        void onSuccess(String response);
        void onError(String error);
    }

    // =============== GET with Params ===============
    public void getItems(String endpoint, Map<String, String> params, OnRawResponse callback) {
        String fullUrl = buildUrlWithParams(BASE_URL + endpoint, params);
        StringRequest request = new StringRequest(
                Request.Method.GET,
                fullUrl,
                callback::onSuccess,
                error -> callback.onError(error.toString())
        );
        addToQueueWithRetry(request);
    }

    private String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) return baseUrl;
        StringBuilder url = new StringBuilder(baseUrl);
        url.append("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            url.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        url.setLength(url.length() - 1); // remove last '&'
        return url.toString();
    }

    // =============== GET All Items ===============
    public void getItems() {
        StringRequest request = new StringRequest(
                Request.Method.GET,
                BASE_URL + "/getall",
                response -> Log.d(TAG, "GET response: " + response),
                error -> Log.e(TAG, "GET error: " + error.toString())
        );
        addToQueueWithRetry(request);
    }

    // =============== Create Item ===============
    public void createItem(String name, int quantity) {
        try {
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("quantity", quantity);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    BASE_URL,
                    data,
                    response -> Log.d(TAG, "CREATE response: " + response),
                    error -> Log.e(TAG, "CREATE error: " + error.toString())
            );

            addToQueueWithRetry(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // =============== Update Item ===============
    public void updateItem(int id, String name, int quantity) {
        try {
            JSONObject data = new JSONObject();
            data.put("name", name);
            data.put("quantity", quantity);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    BASE_URL + "/" + id,
                    data,
                    response -> Log.d(TAG, "UPDATE response: " + response),
                    error -> Log.e(TAG, "UPDATE error: " + error.toString())
            );

            addToQueueWithRetry(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // =============== Delete Item by ID ===============
    public void deleteItem(int id) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.DELETE,
                BASE_URL + "/" + id,
                null,
                response -> Log.d(TAG, "DELETE response: " + response),
                error -> Log.e(TAG, "DELETE error: " + error.toString())
        );
        addToQueueWithRetry(request);
    }

    // =============== Delete Item by Endpoint (Generic) ===============
    public void deleteItemByEndpoint(String endpoint, OnRawResponse callback) {
        String url = BASE_URL + endpoint;
        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                callback::onSuccess,
                error -> callback.onError(error.toString())
        );
        addToQueueWithRetry(request);
    }

    // =============== Register User ===============
    public void registerUser(User userData, OnRawResponse callback) {
        String url = BASE_URL + "/user/register";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", userData.getId());
            jsonObject.put("firstName", userData.getFirstName());
            jsonObject.put("lastName", userData.getLastName());
            jsonObject.put("userName", userData.getUserName());
            jsonObject.put("emailId", userData.getEmailId());
            jsonObject.put("password", userData.getPassword());
            jsonObject.put("active", userData.isActive());
            if (userData.getCreatedTime() != null)
                jsonObject.put("createdTime", userData.getCreatedTime());
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> {
                    Log.d("REGISTER_SUCCESS", response.toString());
                    callback.onSuccess(response.toString());
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorMsg = new String(error.networkResponse.data);
                        Log.e("REGISTER_ERROR", "Status: " + error.networkResponse.statusCode + ", message: " + errorMsg);
                        callback.onError("Status: " + error.networkResponse.statusCode + ", message: " + errorMsg);
                    } else {
                        Log.e("REGISTER_ERROR", error.toString());
                        callback.onError(error.toString());
                    }
                }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        addToQueueWithRetry(request);
    }

    // =============== Login User ===============
    public void Login(String emailId, String password, OnRawResponse callback) {
        String url = BASE_URL + "/user/login";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("emailId", emailId);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                response -> callback.onSuccess(response.toString()),
                error -> callback.onError(error.toString())
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        addToQueueWithRetry(request);
    }

    // =============== Update User ===============
    public void updateUser(User userData, OnRawResponse callback) {
        String url = BASE_URL + "/user/update";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", userData.getId());
            jsonObject.put("firstName", userData.getFirstName());
            jsonObject.put("lastName", userData.getLastName());
            jsonObject.put("userName", userData.getUserName());
            jsonObject.put("emailId", userData.getEmailId());
            jsonObject.put("password", userData.getPassword());
            jsonObject.put("active", userData.isActive());
            jsonObject.put("createdTime", userData.getCreatedTime());
        } catch (JSONException e) {
            callback.onError("JSON error: " + e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                jsonObject,
                response -> callback.onSuccess(response.toString()),
                error -> callback.onError(error.toString())
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        addToQueueWithRetry(request);
    }

    // =============== Add to Cart ===============
    public void addToCart(int uid, int pid, int qty, OnRawResponse callback) {
        String url = BASE_URL + "/products/addtocart/" + uid + "/" + pid + "/" + qty;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                null,
                response -> {
                    if (callback != null) {
                        callback.onSuccess(response.toString());
                    }
                },
                error -> {
                    if (callback != null) {
                        String errorMsg = (error.networkResponse != null && error.networkResponse.data != null)
                                ? new String(error.networkResponse.data)
                                : error.toString();
                        callback.onError(errorMsg);
                    }
                }
        );

        addToQueueWithRetry(request);
    }

    // =============== Utility: Add Retry ===============
    private <T> void addToQueueWithRetry(Request<T> request) {
        int timeoutMs = 15000; // 15 giây
        request.setRetryPolicy(new DefaultRetryPolicy(
                timeoutMs,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        requestQueue.add(request);
    }
}
