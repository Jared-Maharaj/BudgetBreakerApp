package com.example.budgetbreakerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class TodaySpendingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView totalAmountSpentOn;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef;

    private TodayItemsAdapter todayItemsAdapter;
    private List<Data> myDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_spending);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Today's Spending"); //set title to toolbar

        totalAmountSpentOn = findViewById(R.id.totalAmountSpentOn);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager); //set layout manager to recyclerview

        myDataList = new ArrayList<>();
        todayItemsAdapter = new TodayItemsAdapter(TodaySpendingActivity.this, myDataList); //create new adapter t link list with activity
        recyclerView.setAdapter(todayItemsAdapter); //set adapter to recyclerview

        fab = findViewById(R.id.fab);
        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();
        expensesRef = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);

        fab.setOnClickListener(new View.OnClickListener() { //add item to database once fab is clicked
            @Override
            public void onClick(View view) {
                addItemSpentOn();
            }
        });

        readItems();
    }

    private void readItems() { //fetching items created within day

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //set date format
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime()); //get time from device clock

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId); //create database reference to get items that have been spent on
        Query query = reference.orderByChild("date").equalTo(date); //order by date
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear(); //clear array list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){ //set item data to array list to be displayed
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                todayItemsAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                int totalAmount = 0;
                for (DataSnapshot ds : snapshot.getChildren()){ //calculate and display today's spent total
                    Map<String, Object> map = (Map<String, Object>)ds.getValue();
                    Object total = map.get("amount");
                    int pTotal = Integer.parseInt(String.valueOf(total));
                    totalAmount += pTotal;
                    totalAmountSpentOn.setText("Total Day's Spending: R" + totalAmount);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void addItemSpentOn() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView); //change layout based on cardview selected

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemSpinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final EditText note = myView.findViewById(R.id.note);

        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);

        note.setVisibility(View.VISIBLE); //change to be visible

        save.setOnClickListener(new View.OnClickListener() { //save budget item to firebase database
            @Override
            public void onClick(View view) {

                String Amount = amount.getText().toString(); //get amount from edittext
                String Item = itemSpinner.getSelectedItem().toString(); //get selection from spinner
                String notes = note.getText().toString();

                if (TextUtils.isEmpty(Amount)){ //validation
                    amount.setError("Amount is required");
                }
                if (Item.equals("Select Item")){ //validation
                    Toast.makeText(TodaySpendingActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(notes)){
                    note.setError("Note is required");
                    return;
                }

                else{

                    loader.setMessage("Adding a Budget Item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = expensesRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //set date format
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime()); //get time from device clock

                    MutableDateTime epoch = new MutableDateTime(); //using joda-time(epoch) to calculate date from 01/01/1970
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Weeks weeks = Weeks.weeksBetween(epoch, now);
                    Months months = Months.monthsBetween(epoch, now);

                    //analytics data
                    String itemNday = Item+date;
                    String itemNweek = Item+weeks.getWeeks();
                    String itemNmonth = Item+months.getMonths();

                    Data data = new Data(Item, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(Amount), weeks.getWeeks(), months.getMonths(), notes); //create budget item data
                    expensesRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() { //post budget item to firebase
                        @Override
                        public void onComplete(@NonNull Task<Void> task) { //error checking
                            if (task.isSuccessful()){
                                Toast.makeText(TodaySpendingActivity.this, "Budget Item Added Successfully", Toast.LENGTH_SHORT).show(); //upon success
                            }
                            else {
                                Toast.makeText(TodaySpendingActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show(); //upon failure
                            }
                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss(); //dismiss dialog window
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() { //dismiss dialog window when cancel button clicked
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}