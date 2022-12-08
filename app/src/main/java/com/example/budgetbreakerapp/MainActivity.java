package com.example.budgetbreakerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView budgetTv, todayTv, weekTv, monthTv, savingsTv;

    private ImageView budgetBtnImageView, todayBtnImageView, weekBtnImageView, monthBtnImageView, analyticsImageView, historyImageView;

    private FirebaseAuth mAuth;
    private DatabaseReference budgetRef, expensesRef, personalRef;
    private String onlineUserId = "";

    private int totalAmountMonth = 0;
    private int totalAmountBudget = 0;
    private int totalAmountBudgetB = 0;
    private int totalAmountBudgetC = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Budget Breaker");

        //textViews
        budgetTv = findViewById(R.id.budgetTv);
        todayTv = findViewById(R.id.todayTv);
        weekTv = findViewById(R.id.weekTv);
        monthTv = findViewById(R.id.monthTv);
        savingsTv = findViewById(R.id.savingsTv);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        onlineUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); //get current user id
        budgetRef = FirebaseDatabase.getInstance().getReference("budget").child(onlineUserId); //node link
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //node link
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(onlineUserId); //node link

        //views redirect
        budgetBtnImageView = findViewById(R.id.budgetBtnImageView);
        todayBtnImageView = findViewById(R.id.todayBtnImageView);
        weekBtnImageView = findViewById(R.id.weekBtnImageView);
        monthBtnImageView = findViewById(R.id.monthBtnImageView);
        analyticsImageView = findViewById(R.id.analyticsImageView);
        historyImageView = findViewById(R.id.historyImageView);

        budgetBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BudgetActivity.class); //redirect
                startActivity(intent);
            }
        });

        todayBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TodaySpendingActivity.class); //redirect
                startActivity(intent);
            }
        });

        weekBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeekSpendingActivity.class); //redirect
                intent.putExtra("type", "week"); //because same activity is used for weekly and monthly
                startActivity(intent);
            }
        });

        monthBtnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeekSpendingActivity.class); //redirect
                intent.putExtra("type", "month"); //because same activity is used for weekly and monthly
                startActivity(intent);
            }
        });

        analyticsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChooseAnalyticActivity.class); //redirect
                startActivity(intent);
            }
        });

        historyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class); //redirect
                startActivity(intent);
            }
        });

        budgetRef.addValueEventListener(new ValueEventListener() { //calculate and display total budget amount
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue(); //use map to add all amount node values
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //total budget amount
                        totalAmountBudgetB+=pTotal;
                    }
                    totalAmountBudgetC = totalAmountBudgetB;
                    personalRef.child("budget").setValue(totalAmountBudgetC); //total budget amount saved to use for savings
                } else{ //if budget not set
                    personalRef.child("budget").setValue(0);
                    Toast.makeText(MainActivity.this, "Please set a budget", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getBudgetAmount(); //sum of budget item amounts
        getTodaySpentAmount(); //sum of daily expenses
        getWeekSpentAmount(); //sum of weekly expenses
        getMonthSpentAmount(); //sum of monthly expenses
        getSavings(); //sum of budget items minus sum of monthly expenses

    }

    private void getSavings() { //calculate savings based on set budget sub expenses
        personalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    int budget; //get set budget amount
                    if (snapshot.hasChild("budget")){
                        budget = Integer.parseInt(snapshot.child("budget").getValue().toString());
                    } else{
                        budget = 0; // if there are no records
                    }
                    int monthSpending; //get total spent within month
                    if (snapshot.hasChild("month")){
                        monthSpending = Integer.parseInt(Objects.requireNonNull(snapshot.child("month").getValue().toString()));
                    } else{
                        monthSpending = 0; // if there are no records
                    }

                    int savings = budget - monthSpending; //calculate total savings
                    savingsTv.setText("R" + savings);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthSpentAmount() { //calculate and display total spent in the month
        MutableDateTime epoch = new MutableDateTime(); //get date time
        epoch.setDate(0); //set to epoch time
        DateTime now = new DateTime();
        Months months = Months.monthsBetween(epoch, now); //get months as an integer

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //set reference point to expenses node
        Query query = reference.orderByChild("month").equalTo(months.getMonths()); //order records by month
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { //calculate total expenditure
                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)ds.getValue(); //use map to compile amounts
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total)); //sum of amounts
                    totalAmount+=pTotal;
                    monthTv.setText("R" + totalAmount); //display
                }
                personalRef.child("month").setValue(totalAmount); //saving total to database under personal node
                totalAmountMonth = totalAmount;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getWeekSpentAmount() { //calculate and display total spent in the week
        MutableDateTime epoch = new MutableDateTime();
        epoch.setDate(0); //set to epoch time
        DateTime now = new DateTime();
        Weeks weeks = Weeks.weeksBetween(epoch, now);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("week").equalTo(weeks.getWeeks());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)ds.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount+=pTotal;
                    weekTv.setText("R" + totalAmount);
                }
                personalRef.child("week").setValue(totalAmount); //saving total to database
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTodaySpentAmount() { //calculate and display total amount spent today
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //get today's date data
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()){
                    Map<String, Object> map = (Map<String, Object>)ds.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount+=pTotal;
                    todayTv.setText("R" + totalAmount);
                }
                personalRef.child("today").setValue(totalAmount); //saving total to database
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBudgetAmount() { //calculate and display total budget amount
        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue(); //use map to find values
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total)); //calculate total
                        totalAmountBudget+=pTotal;
                        budgetTv.setText("R" + String.valueOf(totalAmountBudget));
                    }
                } else{ //if budget not set
                    totalAmountBudget+=0;
                    budgetTv.setText("R" + String.valueOf(totalAmountBudget));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //menu holds redirect button to account activity
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //to account activity
        if (item.getItemId() == R.id.account){
            Intent intent = new Intent(MainActivity.this, AccountActivity.class); //redirect
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}