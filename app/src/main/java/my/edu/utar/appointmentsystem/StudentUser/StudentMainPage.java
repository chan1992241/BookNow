package my.edu.utar.appointmentsystem.StudentUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import my.edu.utar.appointmentsystem.MISC.Appointment;
import my.edu.utar.appointmentsystem.R;
import my.edu.utar.appointmentsystem.Volley.VolleySingleton;

public class StudentMainPage extends AppCompatActivity {

    private ArrayList<Appointment> appointmentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_main_page);
        // customize action bar
        getSupportActionBar().setTitle("Appointment With Lecturer");
        getData();
    }

    // inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Intent intent1 = getIntent();
        String studentID = intent1.getStringExtra("studentID");
        String role = intent1.getStringExtra("role");
        StudentOptionMenu studentOptionMenu = new StudentOptionMenu(StudentMainPage.this, menu, studentID,role);
        studentOptionMenu.build();
        return true;
    }

    // menu item selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void getData() {
        LinearLayoutCompat progressBar = findViewById(R.id.student_booking_progressBar);
        progressBar.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        String studentID = intent.getStringExtra("studentID");
        String url2 = "https://appointmentmobileapi.herokuapp.com/listAllBookingStudent/" + studentID;
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.GET, url2, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        progressBar.setVisibility(View.GONE);
                        try {
                            appointmentData = formatResponse(response);
                            if (appointmentData == null) {
                                Log.e("Appointment Length", "formatResponse: null");
                                RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_student_main_page);
                                LinearLayoutCompat ll = (LinearLayoutCompat) rl.findViewById(R.id.student_booking_container);
                                TextView tv = new TextView(StudentMainPage.this);
                                tv.setText("No Booking");
                                tv.setGravity(Gravity.CENTER);
                                ll.setGravity(Gravity.CENTER);
                                ll.addView(tv);
                            } else {
                                Log.e("Appointment Length", "formatResponse: " + appointmentData.size());
                                ListView bookingList = (ListView) findViewById(R.id.student_booking_list);
                                StudentBookingAdapter bookingAdapter = new StudentBookingAdapter(StudentMainPage.this, appointmentData, studentID);
                                bookingList.setAdapter(bookingAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONException", "e: " + e.getMessage(), e.getCause());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            byte[] htmlBodyBytes = error.networkResponse.data;
                            JSONObject errorRes =new JSONObject(new String(htmlBodyBytes));
                            //System.out.println(errorRes.getString("status"));
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        VolleySingleton.getInstance(StudentMainPage.this).addToRequestQueue(jsonObjectRequest);
    }

    private ArrayList<Appointment> formatResponse(JSONArray response) throws JSONException {
        // format each appointment into Appointment class
        // place all booking into ArrayList<Appointment>
        Log.e("Response Length", "formatResponse: " + response.length());
        ArrayList<Appointment> appointments = new ArrayList<Appointment>();
        for (int i = 0 ; i < response.length(); i++) {
            JSONObject obj = response.getJSONObject(i);
            appointments.add(new Appointment(obj));
        }

        if (!appointments.isEmpty()) {
            return appointments;
        }
        return null;
    }
}