package example.org.spottest;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface IResult {
    public void notifySuccess(int successfulTracks);
    public void notifyError();
}
