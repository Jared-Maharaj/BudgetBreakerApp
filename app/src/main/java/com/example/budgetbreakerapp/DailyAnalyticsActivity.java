package com.example.budgetbreakerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DailyAnalyticsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef, personalRef;

    private TextView totalAmountSpentOnDailyTv, analyticsTransportAmount, analyticsFoodAmount, analyticsHouseAmount, analyticsEntertainmentAmount, analyticsEducationAmount;
    private TextView analyticsCharityAmount, analyticsClothingAmount, analyticsHealthAmount, analyticsPersonalAmount, analyticsOtherAmount, dailySpentAmount, dailyRatioSpending;

    private RelativeLayout relativeLayoutTransport, relativeLayoutFood, relativeLayoutHouse, relativeLayoutEntertainment, relativeLayoutEducation, relativeLayoutCharity;
    private RelativeLayout relativeLayoutClothing, relativeLayoutHealth, relativeLayoutPersonal, relativeLayoutOther, relativeLayoutAnalysis;

    private PieChart pieChart;
    private ImageView transport_status, food_status, house_status, entertainment_status, education_status, charity_status, clothing_status, health_status, personal_status, other_status, dailyRatioSpending_Image;
    private TextView progress_status_transport, progress_status_food, progress_status_house, progress_status_entertainment, progress_status_education, progress_status_charity, progress_status_clothing, progress_status_health, progress_status_personal, progress_status_other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_analytics);

        //toolbar
        settingsToolbar = findViewById(R.id.dailyAnalyticsToolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("Today's Analytics");

        //MPAndroidChart
        pieChart = findViewById(R.id.pie_chart);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId);

        //general
        dailyRatioSpending_Image = findViewById(R.id.dailyRatioSpending_Image);

        totalAmountSpentOnDailyTv = findViewById(R.id.totalAmountSpentOnDailyTv);
        dailySpentAmount = findViewById(R.id.dailySpentAmount);
        dailyRatioSpending = findViewById(R.id.dailyRatioSpending);

        relativeLayoutAnalysis = findViewById(R.id.relativeLayoutAnalysis);

        //analytics textViews
        analyticsTransportAmount = findViewById(R.id.analyticsTransportAmount);
        analyticsFoodAmount = findViewById(R.id.analyticsFoodAmount);
        analyticsHouseAmount = findViewById(R.id.analyticsHouseAmount);
        analyticsEntertainmentAmount = findViewById(R.id.analyticsEntertainmentAmount);
        analyticsEducationAmount = findViewById(R.id.analyticsEducationAmount);
        analyticsCharityAmount = findViewById(R.id.analyticsCharityAmount);
        analyticsClothingAmount = findViewById(R.id.analyticsClothingAmount);
        analyticsHealthAmount = findViewById(R.id.analyticsHealthAmount);
        analyticsPersonalAmount = findViewById(R.id.analyticsPersonalAmount);
        analyticsOtherAmount = findViewById(R.id.analyticsOtherAmount);

        //progress status textViews
        progress_status_transport = findViewById(R.id.progress_status_transport);
        progress_status_food = findViewById(R.id.progress_status_food);
        progress_status_house = findViewById(R.id.progress_status_house);
        progress_status_entertainment = findViewById(R.id.progress_status_entertainment);
        progress_status_education = findViewById(R.id.progress_status_education);
        progress_status_charity = findViewById(R.id.progress_status_charity);
        progress_status_clothing = findViewById(R.id.progress_status_clothing);
        progress_status_health = findViewById(R.id.progress_status_health);
        progress_status_personal = findViewById(R.id.progress_status_personal);
        progress_status_other = findViewById(R.id.progress_status_other);

        //imageViews
        transport_status = findViewById(R.id.transport_status);
        food_status = findViewById(R.id.food_status);
        house_status = findViewById(R.id.house_status);
        entertainment_status = findViewById(R.id.entertainment_status);
        education_status = findViewById(R.id.education_status);
        charity_status = findViewById(R.id.charity_status);
        clothing_status = findViewById(R.id.clothing_status);
        health_status = findViewById(R.id.health_status);
        personal_status = findViewById(R.id.personal_status);
        other_status = findViewById(R.id.other_status);

        //relativelayouts
        relativeLayoutTransport = findViewById(R.id.relativeLayoutTransport);
        relativeLayoutFood = findViewById(R.id.relativeLayoutFood);
        relativeLayoutHouse = findViewById(R.id.relativeLayoutHouse);
        relativeLayoutEntertainment = findViewById(R.id.relativeLayoutEntertainment);
        relativeLayoutEducation = findViewById(R.id.relativeLayoutEducation);
        relativeLayoutCharity = findViewById(R.id.relativeLayoutCharity);
        relativeLayoutClothing = findViewById(R.id.relativeLayoutClothing);
        relativeLayoutHealth = findViewById(R.id.relativeLayoutHealth);
        relativeLayoutPersonal = findViewById(R.id.relativeLayoutPersonal);
        relativeLayoutOther = findViewById(R.id.relativeLayoutOther);

        //get total amount user has spent on each item for the day
        getTotalDailyTransportExpenses();
        getTotalDailyFoodExpenses();
        getTotalDailyHouseExpenses();
        getTotalDailyEntertainmentExpenses();
        getTotalDailyEducationExpenses();
        getTotalDailyCharityExpenses();
        getTotalDailyClothingExpenses();
        getTotalDailyHealthExpenses();
        getTotalDailyPersonalExpenses();
        getTotalDailyOtherExpenses();
        getTotalDaySpending();

        //load in methods after some time to ensure relevant variables have been populated first
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setupPieChart();
                loadGraph();
                setStatusAndImageResource();
            }
        }, 2000);

    }

    //calculate and display total spending for that particular day
    private void getTotalDaySpending() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //get date
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                    }
                    totalAmountSpentOnDailyTv.setText("Total Day's Spending: R" + totalAmount);
                    dailySpentAmount.setText("Total Spent: R" + totalAmount);
                } else{ //if no records
                    totalAmountSpentOnDailyTv.setText("You have not spent on any item today.");
                    pieChart.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //calculate and display total expenses allocated to each budget category
    private void getTotalDailyTransportExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Transport"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsTransportAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayTrans").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutTransport.setVisibility(View.GONE);
                    personalRef.child("dayTrans").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyFoodExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Food"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsFoodAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayFood").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("dayFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyHouseExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "House"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsHouseAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayHouse").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("dayHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyEntertainmentExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Entertainment"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsEntertainmentAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayEnt").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutEntertainment.setVisibility(View.GONE);
                    personalRef.child("dayEnt").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyEducationExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Education"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsEducationAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayEdu").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("dayEdu").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyCharityExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Charity"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsCharityAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayCha").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutCharity.setVisibility(View.GONE);
                    personalRef.child("dayCha").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyClothingExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Clothing"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsClothingAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayApp").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutClothing.setVisibility(View.GONE);
                    personalRef.child("dayApp").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyHealthExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Health"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsHealthAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayHea").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("dayHea").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyPersonalExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Personal"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsPersonalAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayPer").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("dayPer").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalDailyOtherExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the day the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        String itemNday = "Other"+date; //analytics data

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //index db to find records
        Query query = reference.orderByChild("itemNday").equalTo(itemNday);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){ //find instances
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //sum amounts
                        totalAmount += pTotal;
                        analyticsOtherAmount.setText("Spent: R" + totalAmount); //display
                    }
                    personalRef.child("dayOther").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutOther.setVisibility(View.GONE);
                    personalRef.child("dayOther").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //render pie chart and legend with relevant settings
    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Daily Spending Analysis");
        pieChart.setCenterTextSize(20);
        pieChart.getDescription().setEnabled(false);

        Legend legend = pieChart.getLegend();

        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
    }

    // pie chart
    private void loadGraph() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    int traTotal;
                    if (snapshot.hasChild("dayTrans")){
                        traTotal = Integer.parseInt(snapshot.child("dayTrans").getValue().toString());
                    } else{
                        traTotal = 0;
                    }
                    int foodTotal;
                    if (snapshot.hasChild("dayFood")){
                        foodTotal = Integer.parseInt(snapshot.child("dayFood").getValue().toString());
                    } else{
                        foodTotal = 0;
                    }
                    int houseTotal;
                    if (snapshot.hasChild("dayHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("dayHouse").getValue().toString());
                    } else{
                        houseTotal = 0;
                    }
                    int entTotal;
                    if (snapshot.hasChild("dayEnt")){
                        entTotal = Integer.parseInt(snapshot.child("dayEnt").getValue().toString());
                    } else{
                        entTotal = 0;
                    }
                    int eduTotal;
                    if (snapshot.hasChild("dayEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("dayEdu").getValue().toString());
                    } else{
                        eduTotal = 0;
                    }
                    int chaTotal;
                    if (snapshot.hasChild("dayCha")){
                        chaTotal = Integer.parseInt(snapshot.child("dayCha").getValue().toString());
                    } else{
                        chaTotal = 0;
                    }
                    int appTotal;
                    if (snapshot.hasChild("dayApp")){
                        appTotal = Integer.parseInt(snapshot.child("dayApp").getValue().toString());
                    } else{
                        appTotal = 0;
                    }
                    int heaTotal;
                    if (snapshot.hasChild("dayHea")){
                        heaTotal = Integer.parseInt(snapshot.child("dayHea").getValue().toString());
                    } else{
                        heaTotal = 0;
                    }
                    int perTotal;
                    if (snapshot.hasChild("dayPer")){
                        perTotal = Integer.parseInt(snapshot.child("dayPer").getValue().toString());
                    } else{
                        perTotal = 0;
                    }
                    int othTotal;
                    if (snapshot.hasChild("dayOther")){
                        othTotal = Integer.parseInt(snapshot.child("dayOther").getValue().toString());
                    } else{
                        othTotal = 0;
                    }


                    //MPAndroidChart
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    if (traTotal > 0){
                        entries.add(new PieEntry(traTotal, "Transport"));
                    } else {}
                    if (foodTotal > 0){
                        entries.add(new PieEntry(foodTotal, "Food"));
                    } else {}
                    if (houseTotal > 0){
                        entries.add(new PieEntry(houseTotal, "House"));
                    } else {}
                    if (entTotal > 0){
                        entries.add(new PieEntry(entTotal, "Entertainment"));
                    } else {}
                    if (eduTotal > 0){
                        entries.add(new PieEntry(eduTotal, "Education"));
                    } else {}
                    if (chaTotal > 0){
                        entries.add(new PieEntry(chaTotal, "Charity"));
                    } else {}
                    if (appTotal > 0){
                        entries.add(new PieEntry(appTotal, "Clothing"));
                    } else {}
                    if (heaTotal > 0){
                        entries.add(new PieEntry(heaTotal, "Health"));
                    } else {}
                    if (perTotal > 0){
                        entries.add(new PieEntry(perTotal, "Personal"));
                    } else {}
                    if (othTotal > 0){
                        entries.add(new PieEntry(othTotal, "Other"));
                    } else {}

                    ArrayList<Integer> colors = new ArrayList<>();
                    for (int color: ColorTemplate.MATERIAL_COLORS){
                        colors.add(color);
                    }
                    for (int color: ColorTemplate.VORDIPLOM_COLORS){
                        colors.add(color);
                    }

                    PieDataSet dataSet = new PieDataSet(entries, "Expenses Category");
                    dataSet.setColors(colors);

                    PieData data = new PieData(dataSet);
                    data.setDrawValues(true);
                    data.setValueFormatter(new PercentFormatter(pieChart));
                    data.setValueTextSize(12f);
                    data.setValueTextColor(Color.BLACK);

                    pieChart.setData(data);
                    pieChart.invalidate();

                    pieChart.animateY(1400, Easing.EaseInOutQuad);


                } else{
                    Toast.makeText(DailyAnalyticsActivity.this, "No analytics data for today", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DailyAnalyticsActivity.this, "No analytics data for today", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //status image flags
    private void setStatusAndImageResource() {
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    float traTotal;
                    if (snapshot.hasChild("dayTrans")){
                        traTotal = Integer.parseInt(snapshot.child("dayTrans").getValue().toString());
                    }else{ //if no records
                        traTotal = 0;
                    }
                    float foodTotal;
                    if (snapshot.hasChild("dayFood")){
                        foodTotal = Integer.parseInt(snapshot.child("dayFood").getValue().toString());
                    }else{ //if no records
                        foodTotal = 0;
                    }
                    float houseTotal;
                    if (snapshot.hasChild("dayHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("dayHouse").getValue().toString());
                    }else{ //if no records
                        houseTotal = 0;
                    }
                    float entTotal;
                    if (snapshot.hasChild("dayEnt")){
                        entTotal = Integer.parseInt(snapshot.child("dayEnt").getValue().toString());
                    }else{ //if no records
                        entTotal = 0;
                    }
                    float eduTotal;
                    if (snapshot.hasChild("dayEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("dayEdu").getValue().toString());
                    }else{ //if no records
                        eduTotal = 0;
                    }
                    float chaTotal;
                    if (snapshot.hasChild("dayCha")){
                        chaTotal = Integer.parseInt(snapshot.child("dayCha").getValue().toString());
                    }else{ //if no records
                        chaTotal = 0;
                    }
                    float appTotal;
                    if (snapshot.hasChild("dayApp")){
                        appTotal = Integer.parseInt(snapshot.child("dayApp").getValue().toString());
                    }else{ //if no records
                        appTotal = 0;
                    }
                    float heaTotal;
                    if (snapshot.hasChild("dayHea")){
                        heaTotal = Integer.parseInt(snapshot.child("dayHea").getValue().toString());
                    }else{ //if no records
                        heaTotal = 0;
                    }
                    float perTotal;
                    if (snapshot.hasChild("dayPer")){
                        perTotal = Integer.parseInt(snapshot.child("dayPer").getValue().toString());
                    }else{ //if no records
                        perTotal = 0;
                    }
                    float othTotal;
                    if (snapshot.hasChild("dayOther")){
                        othTotal = Integer.parseInt(snapshot.child("dayOther").getValue().toString());
                    }else{ //if no records
                        othTotal = 0;
                    }
                    float dayTotalSpentAmount;
                    if (snapshot.hasChild("today")){
                        dayTotalSpentAmount = Integer.parseInt(snapshot.child("today").getValue().toString());
                    }else{ //if no records
                        dayTotalSpentAmount = 0;
                    }

                    //getting ratios
                    float traRatio;
                    if (snapshot.hasChild("dayTransRatio")){
                        traRatio = Integer.parseInt(snapshot.child("dayTransRatio").getValue().toString());
                    } else{
                        traRatio = 0;
                    }
                    float foodRatio;
                    if (snapshot.hasChild("dayFoodRatio")){
                        foodRatio = Integer.parseInt(snapshot.child("dayFoodRatio").getValue().toString());
                    } else{
                        foodRatio = 0;
                    }
                    float houseRatio;
                    if (snapshot.hasChild("dayHouseRatio")){
                        houseRatio = Integer.parseInt(snapshot.child("dayHouseRatio").getValue().toString());
                    } else{
                        houseRatio = 0;
                    }
                    float entRatio;
                    if (snapshot.hasChild("dayEntRatio")){
                        entRatio = Integer.parseInt(snapshot.child("dayEntRatio").getValue().toString());
                    } else{
                        entRatio = 0;
                    }
                    float eduRatio;
                    if (snapshot.hasChild("dayEduRatio")){
                        eduRatio = Integer.parseInt(snapshot.child("dayEduRatio").getValue().toString());
                    } else{
                        eduRatio = 0;
                    }
                    float chaRatio;
                    if (snapshot.hasChild("dayCharRatio")){
                        chaRatio = Integer.parseInt(snapshot.child("dayCharRatio").getValue().toString());
                    } else{
                        chaRatio = 0;
                    }
                    float appRatio;
                    if (snapshot.hasChild("dayAppRatio")){
                        appRatio = Integer.parseInt(snapshot.child("dayAppRatio").getValue().toString());
                    } else{
                        appRatio = 0;
                    }
                    float heaRatio;
                    if (snapshot.hasChild("dayHealthRatio")){
                        heaRatio = Integer.parseInt(snapshot.child("dayHealthRatio").getValue().toString());
                    } else{
                        heaRatio = 0;
                    }
                    float perRatio;
                    if (snapshot.hasChild("dayPerRatio")){
                        perRatio = Integer.parseInt(snapshot.child("dayPerRatio").getValue().toString());
                    } else{
                        perRatio = 0;
                    }
                    float othRatio;
                    if (snapshot.hasChild("dayOtherRatio")){
                        othRatio = Integer.parseInt(snapshot.child("dayOtherRatio").getValue().toString());
                    } else{
                        othRatio = 0;
                    }
                    float dayTotalSpentAmountRatio;
                    if (snapshot.hasChild("dailyBudget")){
                        dayTotalSpentAmountRatio = Integer.parseInt(snapshot.child("dailyBudget").getValue().toString());
                    } else{
                        dayTotalSpentAmountRatio = 0;
                    }

                    //set appropriate image based on expense ratio
                    float dayPercent = (dayTotalSpentAmount/dayTotalSpentAmountRatio)*100;
                    if (dayPercent<50){
                        dailyRatioSpending.setText(dayPercent + "%" + " used of R" + dayTotalSpentAmountRatio + "   Status:");
                        dailyRatioSpending_Image.setImageResource(R.drawable.green);
                    } else if (dayPercent>=50 && dayPercent<100){
                        dailyRatioSpending.setText(dayPercent + "%" + " used of R" + dayTotalSpentAmountRatio + "   Status:");
                        dailyRatioSpending_Image.setImageResource(R.drawable.brown);
                    } else {
                        dailyRatioSpending.setText(dayPercent + "%" + " used of R" + dayTotalSpentAmountRatio + "   Status:");
                        dailyRatioSpending_Image.setImageResource(R.drawable.red);
                    }
                    float transportPercent = (traTotal/traRatio)*100;
                    if (transportPercent<50){
                        progress_status_transport.setText(transportPercent + "%" + " used of R" + traRatio + "   Status:");
                        transport_status.setImageResource(R.drawable.green);
                    } else if (transportPercent>=50 && transportPercent<100){
                        progress_status_transport.setText(transportPercent + "%" + " used of R" + traRatio + "   Status:");
                        transport_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_transport.setText(transportPercent + "%" + " used of R" + traRatio + "   Status:");
                        transport_status.setImageResource(R.drawable.red);
                    }
                    float foodPercent = (foodTotal/foodRatio)*100;
                    if (foodPercent<50){
                        progress_status_food.setText(foodPercent + "%" + " used of R" + foodRatio + "   Status:");
                        food_status.setImageResource(R.drawable.green);
                    } else if (foodPercent>=50 && foodPercent<100){
                        progress_status_food.setText(foodPercent + "%" + " used of R" + foodRatio + "   Status:");
                        food_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_food.setText(foodPercent + "%" + " used of R" + foodRatio + "   Status:");
                        food_status.setImageResource(R.drawable.red);
                    }
                    float housePercent = (houseTotal/houseRatio)*100;
                    if (housePercent<50){
                        progress_status_house.setText(housePercent + "%" + " used of R" + houseRatio + "   Status:");
                        house_status.setImageResource(R.drawable.green);
                    } else if (housePercent>=50 && housePercent<100){
                        progress_status_house.setText(housePercent + "%" + " used of R" + houseRatio + "   Status:");
                        house_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_house.setText(housePercent + "%" + " used of R" + houseRatio + "   Status:");
                        house_status.setImageResource(R.drawable.red);
                    }
                    float entPercent = (entTotal/entRatio)*100;
                    if (entPercent<50){
                        progress_status_entertainment.setText(entPercent + "%" + " used of R" + entRatio + "   Status:");
                        entertainment_status.setImageResource(R.drawable.green);
                    } else if (entPercent>=50 && entPercent<100){
                        progress_status_entertainment.setText(entPercent + "%" + " used of R" + entRatio + "   Status:");
                        entertainment_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_entertainment.setText(entPercent + "%" + " used of R" + entRatio + "   Status:");
                        entertainment_status.setImageResource(R.drawable.red);
                    }
                    float eduPercent = (eduTotal/eduRatio)*100;
                    if (eduPercent<50){
                        progress_status_education.setText(eduPercent + "%" + " used of R" + eduRatio + "   Status:");
                        education_status.setImageResource(R.drawable.green);
                    } else if (eduPercent>=50 && eduPercent<100){
                        progress_status_education.setText(eduPercent + "%" + " used of R" + eduRatio + "   Status:");
                        education_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_education.setText(eduPercent + "%" + " used of R" + eduRatio + "   Status:");
                        education_status.setImageResource(R.drawable.red);
                    }
                    float chaPercent = (chaTotal/chaRatio)*100;
                    if (chaPercent<50){
                        progress_status_charity.setText(chaPercent + "%" + " used of R" + chaRatio + "   Status:");
                        charity_status.setImageResource(R.drawable.green);
                    } else if (chaPercent>=50 && chaPercent<100){
                        progress_status_charity.setText(chaPercent + "%" + " used of R" + chaRatio + "   Status:");
                        charity_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_charity.setText(chaPercent + "%" + " used of R" + chaRatio + "   Status:");
                        charity_status.setImageResource(R.drawable.red);
                    }
                    float appPercent = (appTotal/appRatio)*100;
                    if (appPercent<50){
                        progress_status_clothing.setText(appPercent + "%" + " used of R" + appRatio + "   Status:");
                        clothing_status.setImageResource(R.drawable.green);
                    } else if (appPercent>=50 && appPercent<100){
                        progress_status_clothing.setText(appPercent + "%" + " used of R" + appRatio + "   Status:");
                        clothing_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_clothing.setText(appPercent + "%" + " used of R" + appRatio + "   Status:");
                        clothing_status.setImageResource(R.drawable.red);
                    }
                    float heaPercent = (heaTotal/heaRatio)*100;
                    if (heaPercent<50){
                        progress_status_health.setText(heaPercent + "%" + " used of R" + heaRatio + "   Status:");
                        health_status.setImageResource(R.drawable.green);
                    } else if (heaPercent>=50 && heaPercent<100){
                        progress_status_health.setText(heaPercent + "%" + " used of R" + heaRatio + "   Status:");
                        health_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_health.setText(heaPercent + "%" + " used of R" + heaRatio + "   Status:");
                        health_status.setImageResource(R.drawable.red);
                    }
                    float perPercent = (perTotal/perRatio)*100;
                    if (perPercent<50){
                        progress_status_personal.setText(perPercent + "%" + " used of R" + perRatio + "   Status:");
                        personal_status.setImageResource(R.drawable.green);
                    } else if (perPercent>=50 && perPercent<100){
                        progress_status_personal.setText(perPercent + "%" + " used of R" + perRatio + "   Status:");
                        personal_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_personal.setText(perPercent + "%" + " used of R" + perRatio + "   Status:");
                        personal_status.setImageResource(R.drawable.red);
                    }
                    float otherPercent = (othTotal/othRatio)*100;
                    if (otherPercent<50){
                        progress_status_other.setText(otherPercent + "%" + " used of R" + othRatio + "   Status:");
                        other_status.setImageResource(R.drawable.green);
                    } else if (otherPercent>=50 && otherPercent<100){
                        progress_status_other.setText(otherPercent + "%" + " used of R" + othRatio + "   Status:");
                        other_status.setImageResource(R.drawable.brown);
                    } else {
                        progress_status_other.setText(otherPercent + "%" + " used of R" + othRatio + "   Status:");
                        other_status.setImageResource(R.drawable.red);
                    }
                } else{
                    Toast.makeText(DailyAnalyticsActivity.this, "Error loading image resource", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}