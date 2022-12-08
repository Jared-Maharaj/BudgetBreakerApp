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
import org.joda.time.Weeks;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class WeeklyAnalyticsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_weekly_analytics);

        //toolbar
        settingsToolbar = findViewById(R.id.dailyAnalyticsToolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle("This Week's Analytics");

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

        getTotalWeeklyTransportExpenses();
        getTotalWeeklyFoodExpenses();
        getTotalWeeklyHouseExpenses();
        getTotalWeeklyEntertainmentExpenses();
        getTotalWeeklyEducationExpenses();
        getTotalWeeklyCharityExpenses();
        getTotalWeeklyClothingExpenses();
        getTotalWeeklyHealthExpenses();
        getTotalWeeklyPersonalExpenses();
        getTotalWeeklyOtherExpenses();
        getTotalWeekSpending();

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

    //calculate and display total spending for that particular week
    private void getTotalWeekSpending() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("week").equalTo(weeks.getWeeks());
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
                    totalAmountSpentOnDailyTv.setText("Total Week's Spending: R" + totalAmount);
                    dailySpentAmount.setText("Total Spent: R" + totalAmount);
                } else{
                    totalAmountSpentOnDailyTv.setText("You have not spent on any item this week.");
                    pieChart.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //calculate and display total expenses allocated to each budget category
    private void getTotalWeeklyTransportExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Transport"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekTrans").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutTransport.setVisibility(View.GONE);
                    personalRef.child("weekTrans").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyFoodExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Food"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekFood").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutFood.setVisibility(View.GONE);
                    personalRef.child("weekFood").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyHouseExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "House"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekHouse").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutHouse.setVisibility(View.GONE);
                    personalRef.child("weekHouse").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyEntertainmentExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Entertainment"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekEnt").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutEntertainment.setVisibility(View.GONE);
                    personalRef.child("weekEnt").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyEducationExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Education"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekEdu").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutEducation.setVisibility(View.GONE);
                    personalRef.child("weekEdu").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyCharityExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Charity"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekCha").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutCharity.setVisibility(View.GONE);
                    personalRef.child("weekCha").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyClothingExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Clothing"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekApp").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutClothing.setVisibility(View.GONE);
                    personalRef.child("weekApp").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyHealthExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Health"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekHea").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutHealth.setVisibility(View.GONE);
                    personalRef.child("weekHea").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyPersonalExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Personal"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekPer").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutPersonal.setVisibility(View.GONE);
                    personalRef.child("weekPer").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getTotalWeeklyOtherExpenses() {
        MutableDateTime epoch = new MutableDateTime(); //get the week the item was posted
        epoch.setDate(0);
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        String itemNweek = "Other"+weeks.getWeeks();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("itemNweek").equalTo(itemNweek);
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
                    personalRef.child("weekOther").setValue(totalAmount); //save to db as child
                } else{ //if no records
                    relativeLayoutOther.setVisibility(View.GONE);
                    personalRef.child("weekOther").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupPieChart(){
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Weekly Spending Analysis");
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
                    if (snapshot.hasChild("weekTrans")){
                        traTotal = Integer.parseInt(snapshot.child("weekTrans").getValue().toString());
                    } else{
                        traTotal = 0;
                    }
                    int foodTotal;
                    if (snapshot.hasChild("weekFood")){
                        foodTotal = Integer.parseInt(snapshot.child("weekFood").getValue().toString());
                    } else{
                        foodTotal = 0;
                    }
                    int houseTotal;
                    if (snapshot.hasChild("weekHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("weekHouse").getValue().toString());
                    } else{
                        houseTotal = 0;
                    }
                    int entTotal;
                    if (snapshot.hasChild("weekEnt")){
                        entTotal = Integer.parseInt(snapshot.child("weekEnt").getValue().toString());
                    } else{
                        entTotal = 0;
                    }
                    int eduTotal;
                    if (snapshot.hasChild("weekEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("weekEdu").getValue().toString());
                    } else{
                        eduTotal = 0;
                    }
                    int chaTotal;
                    if (snapshot.hasChild("weekCha")){
                        chaTotal = Integer.parseInt(snapshot.child("weekCha").getValue().toString());
                    } else{
                        chaTotal = 0;
                    }
                    int appTotal;
                    if (snapshot.hasChild("weekApp")){
                        appTotal = Integer.parseInt(snapshot.child("weekApp").getValue().toString());
                    } else{
                        appTotal = 0;
                    }
                    int heaTotal;
                    if (snapshot.hasChild("weekHea")){
                        heaTotal = Integer.parseInt(snapshot.child("weekHea").getValue().toString());
                    } else{
                        heaTotal = 0;
                    }
                    int perTotal;
                    if (snapshot.hasChild("weekPer")){
                        perTotal = Integer.parseInt(snapshot.child("weekPer").getValue().toString());
                    } else{
                        perTotal = 0;
                    }
                    int othTotal;
                    if (snapshot.hasChild("weekOther")){
                        othTotal = Integer.parseInt(snapshot.child("weekOther").getValue().toString());
                    } else{
                        othTotal = 0;
                    }


                    //MPAndroidChart
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    if (traTotal > 0){ //if expense amount is 0, don't add pie chart entry
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
                    Toast.makeText(WeeklyAnalyticsActivity.this, "No analytics data for this week", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(WeeklyAnalyticsActivity.this, "No analytics data for this week", Toast.LENGTH_SHORT).show();
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
                    if (snapshot.hasChild("weekTrans")){
                        traTotal = Integer.parseInt(snapshot.child("weekTrans").getValue().toString());
                    }else{ //if no records
                        traTotal = 0;
                    }
                    float foodTotal;
                    if (snapshot.hasChild("weekFood")){
                        foodTotal = Integer.parseInt(snapshot.child("weekFood").getValue().toString());
                    }else{ //if no records
                        foodTotal = 0;
                    }
                    float houseTotal;
                    if (snapshot.hasChild("weekHouse")){
                        houseTotal = Integer.parseInt(snapshot.child("weekHouse").getValue().toString());
                    }else{ //if no records
                        houseTotal = 0;
                    }
                    float entTotal;
                    if (snapshot.hasChild("weekEnt")){
                        entTotal = Integer.parseInt(snapshot.child("weekEnt").getValue().toString());
                    }else{ //if no records
                        entTotal = 0;
                    }
                    float eduTotal;
                    if (snapshot.hasChild("weekEdu")){
                        eduTotal = Integer.parseInt(snapshot.child("weekEdu").getValue().toString());
                    }else{ //if no records
                        eduTotal = 0;
                    }
                    float chaTotal;
                    if (snapshot.hasChild("weekCha")){
                        chaTotal = Integer.parseInt(snapshot.child("weekCha").getValue().toString());
                    }else{ //if no records
                        chaTotal = 0;
                    }
                    float appTotal;
                    if (snapshot.hasChild("weekApp")){
                        appTotal = Integer.parseInt(snapshot.child("weekApp").getValue().toString());
                    }else{ //if no records
                        appTotal = 0;
                    }
                    float heaTotal;
                    if (snapshot.hasChild("weekHea")){
                        heaTotal = Integer.parseInt(snapshot.child("weekHea").getValue().toString());
                    }else{ //if no records
                        heaTotal = 0;
                    }
                    float perTotal;
                    if (snapshot.hasChild("weekPer")){
                        perTotal = Integer.parseInt(snapshot.child("weekPer").getValue().toString());
                    }else{ //if no records
                        perTotal = 0;
                    }
                    float othTotal;
                    if (snapshot.hasChild("weekOther")){
                        othTotal = Integer.parseInt(snapshot.child("weekOther").getValue().toString());
                    }else{ //if no records
                        othTotal = 0;
                    }
                    float dayTotalSpentAmount;
                    if (snapshot.hasChild("week")){
                        dayTotalSpentAmount = Integer.parseInt(snapshot.child("week").getValue().toString());
                    }else{ //if no records
                        dayTotalSpentAmount = 0;
                    }

                    //getting ratios
                    float traRatio;
                    if (snapshot.hasChild("weekTransRatio")){
                        traRatio = Integer.parseInt(snapshot.child("weekTransRatio").getValue().toString());
                    } else{
                        traRatio = 0;
                    }
                    float foodRatio;
                    if (snapshot.hasChild("weekFoodRatio")){
                        foodRatio = Integer.parseInt(snapshot.child("weekFoodRatio").getValue().toString());
                    } else{
                        foodRatio = 0;
                    }
                    float houseRatio;
                    if (snapshot.hasChild("weekHouseRatio")){
                        houseRatio = Integer.parseInt(snapshot.child("weekHouseRatio").getValue().toString());
                    } else{
                        houseRatio = 0;
                    }
                    float entRatio;
                    if (snapshot.hasChild("weekEntRatio")){
                        entRatio = Integer.parseInt(snapshot.child("weekEntRatio").getValue().toString());
                    } else{
                        entRatio = 0;
                    }
                    float eduRatio;
                    if (snapshot.hasChild("weekEduRatio")){
                        eduRatio = Integer.parseInt(snapshot.child("weekEduRatio").getValue().toString());
                    } else{
                        eduRatio = 0;
                    }
                    float chaRatio;
                    if (snapshot.hasChild("weekCharRatio")){
                        chaRatio = Integer.parseInt(snapshot.child("weekCharRatio").getValue().toString());
                    } else{
                        chaRatio = 0;
                    }
                    float appRatio;
                    if (snapshot.hasChild("weekAppRatio")){
                        appRatio = Integer.parseInt(snapshot.child("weekAppRatio").getValue().toString());
                    } else{
                        appRatio = 0;
                    }
                    float heaRatio;
                    if (snapshot.hasChild("weekHealthRatio")){
                        heaRatio = Integer.parseInt(snapshot.child("weekHealthRatio").getValue().toString());
                    } else{
                        heaRatio = 0;
                    }
                    float perRatio;
                    if (snapshot.hasChild("weekPerRatio")){
                        perRatio = Integer.parseInt(snapshot.child("weekPerRatio").getValue().toString());
                    } else{
                        perRatio = 0;
                    }
                    float othRatio;
                    if (snapshot.hasChild("weekOtherRatio")){
                        othRatio = Integer.parseInt(snapshot.child("weekOtherRatio").getValue().toString());
                    } else{
                        othRatio = 0;
                    }
                    float dayTotalSpentAmountRatio;
                    if (snapshot.hasChild("weeklyBudget")){
                        dayTotalSpentAmountRatio = Integer.parseInt(snapshot.child("weeklyBudget").getValue().toString());
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
                    Toast.makeText(WeeklyAnalyticsActivity.this, "Error loading image resource", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}