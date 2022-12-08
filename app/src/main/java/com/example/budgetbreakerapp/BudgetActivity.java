package com.example.budgetbreakerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.Calendar;
import java.util.Map;

public class BudgetActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private FloatingActionButton fab;

    private DatabaseReference budgetRef, personalRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;

    private TextView totalBudgetAmountTextView;
    private RecyclerView recyclerView;

    private String post_key = "";
    private String item = "";
    private int amount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Set A Budget");

        mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(mAuth.getCurrentUser().getUid()); //firebase initialization
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(mAuth.getCurrentUser().getUid()); //firebase initialization
        loader = new ProgressDialog(this);

        totalBudgetAmountTextView = findViewById(R.id.totalBudgetAmountTextView);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        budgetRef.addValueEventListener(new ValueEventListener() { //calculates sum of all items as total budget amount
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAmount = 0;

                for (DataSnapshot snap: snapshot.getChildren()){
                    Data data = snap.getValue(Data.class); //create instance of data class
                    totalAmount += data.getAmount(); //performs sum calculations of all itemAmounts
                    String sTotal = String.valueOf("Monthly Budget: R" + totalAmount); //change text & set value
                    totalBudgetAmountTextView.setText(sTotal); //change totalAmountTextView to display sTotal and sum
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additem();
            }
        });

        //analytics
        budgetRef.addValueEventListener(new ValueEventListener() { //calculate total monthly budget day month week ratios
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { //calculate sum budget items
                if (snapshot.exists() && snapshot.getChildrenCount()>0){
                    int totalamount = 0;
                    for (DataSnapshot snap : snapshot.getChildren()){
                        Data data = snap.getValue(Data.class);
                        totalamount+= data.getAmount();
                        String sTotal = String.valueOf("Month Budget: "+totalamount);
                        totalBudgetAmountTextView.setText(sTotal);
                    }
                    int weeklyBudget = totalamount/4; //weekly
                    int dailyBudget = totalamount/30; //daily
                    personalRef.child("budget").setValue(totalamount);
                    personalRef.child("weeklyBudget").setValue(weeklyBudget);
                    personalRef.child("dailyBudget").setValue(dailyBudget);
                } else{ //if no records
                    personalRef.child("budget").setValue(0);
                    personalRef.child("weeklyBudget").setValue(0);
                    personalRef.child("dailyBudget").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        getMonthTransportBudgetRatio();
        getMonthFoodBudgetRatio();
        getMonthHouseBudgetRatio();
        getMonthEntertainmentBudgetRatio();
        getMonthEducationBudgetRatio();
        getMonthCharityBudgetRatio();
        getMonthClothingBudgetRatio();
        getMonthHealthBudgetRatio();
        getMonthPersonalBudgetRatio();
        getMonthOtherBudgetRatio();

    }

    private void getMonthTransportBudgetRatio() { //for analytics
        Query query = budgetRef.orderByChild("item").equalTo("Transport"); //get amount allocated for Transport in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { //calculate day week month allocated spending ratio for budget item
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayTransRatio = pTotal/30; //daily
                    int weekTransRatio = pTotal/4; //weekly
                    int monthTransRatio = pTotal; //monthly

                    personalRef.child("dayTransRatio").setValue(dayTransRatio);
                    personalRef.child("weekTransRatio").setValue(weekTransRatio);
                    personalRef.child("monthTransRatio").setValue(monthTransRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayTransRatio").setValue(0);
                    personalRef.child("weekTransRatio").setValue(0);
                    personalRef.child("monthTransRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthFoodBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Food"); //get amount allocated for Food in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayFoodRatio = pTotal/30; //daily
                    int weekFoodRatio = pTotal/4; //weekly
                    int monthFoodRatio = pTotal; //monthly

                    personalRef.child("dayFoodRatio").setValue(dayFoodRatio);
                    personalRef.child("weekFoodRatio").setValue(weekFoodRatio);
                    personalRef.child("monthFoodRatio").setValue(monthFoodRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayFoodRatio").setValue(0);
                    personalRef.child("weekFoodRatio").setValue(0);
                    personalRef.child("monthFoodRatio").setValue(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthHouseBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("House"); //get amount allocated for House in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayHouseRatio = pTotal/30; //daily
                    int weekHouseRatio = pTotal/4; //weekly
                    int monthHouseRatio = pTotal; //monthly

                    personalRef.child("dayHouseRatio").setValue(dayHouseRatio);
                    personalRef.child("weekHouseRatio").setValue(weekHouseRatio);
                    personalRef.child("monthHouseRatio").setValue(monthHouseRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayHouseRatio").setValue(0);
                    personalRef.child("weekHouseRatio").setValue(0);
                    personalRef.child("monthHouseRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthEntertainmentBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Entertainment"); //get amount allocated for Entertainment in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayEntRatio = pTotal/30; //daily
                    int weekEntRatio = pTotal/4; //weekly
                    int monthEntRatio = pTotal; //monthly

                    personalRef.child("dayEntRatio").setValue(dayEntRatio);
                    personalRef.child("weekEntRatio").setValue(weekEntRatio);
                    personalRef.child("monthEntRatio").setValue(monthEntRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayEntRatio").setValue(0);
                    personalRef.child("weekEntRatio").setValue(0);
                    personalRef.child("monthEntRatio").setValue(0);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthEducationBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Education"); //get amount allocated for Education in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayEduRatio = pTotal/30; //daily
                    int weekEduRatio = pTotal/4; //weekly
                    int monthEduRatio = pTotal; //monthly

                    personalRef.child("dayEduRatio").setValue(dayEduRatio);
                    personalRef.child("weekEduRatio").setValue(weekEduRatio);
                    personalRef.child("monthEduRatio").setValue(monthEduRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayEduRatio").setValue(0);
                    personalRef.child("weekEduRatio").setValue(0);
                    personalRef.child("monthEduRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthCharityBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Charity"); //get amount allocated for Charity in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayCharRatio = pTotal/30; //daily
                    int weekCharRatio = pTotal/4; //weekly
                    int monthCharRatio = pTotal; //monthly

                    personalRef.child("dayCharRatio").setValue(dayCharRatio);
                    personalRef.child("weekCharRatio").setValue(weekCharRatio);
                    personalRef.child("monthCharRatio").setValue(monthCharRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayCharRatio").setValue(0);
                    personalRef.child("weekCharRatio").setValue(0);
                    personalRef.child("monthCharRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthClothingBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Clothing"); //get amount allocated for Clothing in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayAppRatio = pTotal/30; //daily
                    int weekAppRatio = pTotal/4; //weekly
                    int monthAppRatio = pTotal; //monthly

                    personalRef.child("dayAppRatio").setValue(dayAppRatio);
                    personalRef.child("weekAppRatio").setValue(weekAppRatio);
                    personalRef.child("monthAppRatio").setValue(monthAppRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayAppRatio").setValue(0);
                    personalRef.child("weekAppRatio").setValue(0);
                    personalRef.child("monthAppRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthHealthBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Health"); //get amount allocated for Health in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayHealthRatio = pTotal/30; //daily
                    int weekHealthRatio = pTotal/4; //weekly
                    int monthHealthRatio = pTotal; //monthly

                    personalRef.child("dayHealthRatio").setValue(dayHealthRatio);
                    personalRef.child("weekHealthRatio").setValue(weekHealthRatio);
                    personalRef.child("monthHealthRatio").setValue(monthHealthRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayHealthRatio").setValue(0);
                    personalRef.child("weekHealthRatio").setValue(0);
                    personalRef.child("monthHealthRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthPersonalBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Personal"); //get amount allocated for Personal in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayPerRatio = pTotal/30; //daily
                    int weekPerRatio = pTotal/4; //weekly
                    int monthPerRatio = pTotal; //monthly

                    personalRef.child("dayPerRatio").setValue(dayPerRatio);
                    personalRef.child("weekPerRatio").setValue(weekPerRatio);
                    personalRef.child("monthPerRatio").setValue(monthPerRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayPerRatio").setValue(0);
                    personalRef.child("weekPerRatio").setValue(0);
                    personalRef.child("monthPerRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getMonthOtherBudgetRatio() {
        Query query = budgetRef.orderByChild("item").equalTo("Other"); //get amount allocated for Other in budget
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()){
                        Map<String, Object> map = (Map<String, Object>)ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }
                    int dayOtherRatio = pTotal/30; //daily
                    int weekOtherRatio = pTotal/4; //weekly
                    int monthOtherRatio = pTotal; //monthly

                    personalRef.child("dayOtherRatio").setValue(dayOtherRatio);
                    personalRef.child("weekOtherRatio").setValue(weekOtherRatio);
                    personalRef.child("monthOtherRatio").setValue(monthOtherRatio);
                } else{ //if budget item does not exist
                    personalRef.child("dayOtherRatio").setValue(0);
                    personalRef.child("weekOtherRatio").setValue(0);
                    personalRef.child("monthOtherRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void additem() { //method to add a budget item
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView); //change layout based on cardview selected

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        final Spinner itemSpinner = myView.findViewById(R.id.itemSpinner);
        final EditText amount = myView.findViewById(R.id.amount);

        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);

        save.setOnClickListener(new View.OnClickListener() { //save budget item to firebase database
            @Override
            public void onClick(View view) {

                String budgetAmount = amount.getText().toString(); //get amount from edittext
                String budgetItem = itemSpinner.getSelectedItem().toString(); //get selection from spinner

                if (TextUtils.isEmpty(budgetAmount)){ //validation
                    amount.setError("Amount is required");
                }
                if (budgetItem.equals("Select Item")){ //validation
                    Toast.makeText(BudgetActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                }

                else{

                    loader.setMessage("Adding a Budget Item");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    String id = budgetRef.push().getKey();
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //set date format
                    Calendar cal = Calendar.getInstance();
                    String date = dateFormat.format(cal.getTime()); //get time from device clock

                    MutableDateTime epoch = new MutableDateTime(); //using joda-time(epoch) to calculate date from 01/01/1970
                    epoch.setDate(0);
                    DateTime now = new DateTime();
                    Weeks weeks = Weeks.weeksBetween(epoch, now);
                    Months months = Months.monthsBetween(epoch, now);

                    //analytics data
                    String itemNday = budgetItem+date;
                    String itemNweek = budgetItem+weeks.getWeeks();
                    String itemNmonth = budgetItem+months.getMonths();

                    Data data = new Data(budgetItem, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(budgetAmount), weeks.getWeeks(), months.getMonths(), null); //create budget item data
                    budgetRef.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() { //post budget item to firebase
                        @Override
                        public void onComplete(@NonNull Task<Void> task) { //error checking
                           if (task.isSuccessful()){
                               Toast.makeText(BudgetActivity.this, "Budget Item Added Successfully", Toast.LENGTH_SHORT).show(); //upon success
                           } 
                           else {
                               Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show(); //upon failure
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

    @Override
    protected void onStart() { //override onStart method
        super.onStart();

        FirebaseRecyclerOptions<Data> options = new FirebaseRecyclerOptions.Builder<Data>()
                .setQuery(budgetRef, Data.class)
                .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>(options) { //link firebase database to viewholder
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull final Data model) {

                holder.setItemAmount("Allocated Amount: R" + model.getAmount()); //get and display itemamount in viewholder
                holder.setDate("On:" + model.getDate()); //get and display dateposted in viewholder
                holder.setItemName("Budget Item: " + model.getItem()); //get and display category in viewholder

                holder.notes.setVisibility(View.GONE);

                switch (model.getItem()){ //change imageview icon based on selected itemname
                    case "Transport":
                        holder.imageView.setImageResource(R.drawable.ic_transport);
                        break;
                    case "Food":
                        holder.imageView.setImageResource(R.drawable.ic_food);
                        break;
                    case "House":
                        holder.imageView.setImageResource(R.drawable.ic_house);
                        break;
                    case "Entertainment":
                        holder.imageView.setImageResource(R.drawable.ic_entertainment);
                        break;
                    case "Education":
                        holder.imageView.setImageResource(R.drawable.ic_education);
                        break;
                    case "Charity":
                        holder.imageView.setImageResource(R.drawable.ic_consultancy);
                        break;
                    case "Clothing":
                        holder.imageView.setImageResource(R.drawable.ic_shirt);
                        break;
                    case "Health":
                        holder.imageView.setImageResource(R.drawable.ic_health);
                        break;
                    case "Personal":
                        holder.imageView.setImageResource(R.drawable.ic_personalcare);
                        break;
                    case "Other":
                        holder.imageView.setImageResource(R.drawable.ic_other);
                        break;
                }

                holder.mView.setOnClickListener(new View.OnClickListener() { //update item function for myBudget
                    @Override
                    public void onClick(View view) {
                        post_key = getRef(position).getKey();
                        item = model.getItem();
                        amount = model.getAmount();
                        updateData();
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //output to display in retrieve_layout
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_layout, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    public class MyViewHolder extends RecyclerView.ViewHolder{ //used to diplay items from database
        View mView;
        public ImageView imageView;
        public TextView notes, date;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            imageView = itemView.findViewById(R.id.imageView);
            notes = itemView.findViewById(R.id.note);
            date = itemView.findViewById(R.id.date);
        }

        public void setItemName (String itemName){ //set viewholder itemname to text in db
            TextView item = mView.findViewById(R.id.item);
            item.setText(itemName);
        }

        public void setItemAmount(String itemAmount){ //set viewholder itemamount to value in db
            TextView amount = mView.findViewById(R.id.amount);
            amount.setText(itemAmount);
        }

        public void setDate(String itemDate){ //set viewholder date to posted date in db
            TextView date = mView.findViewById(R.id.date);
            date.setText(itemDate);
        }

    }

    private void updateData(){ //edit already existing database instances
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.update_layout, null);

        myDialog.setView(mView);
        final AlertDialog dialog = myDialog.create();

        final TextView mItem = mView.findViewById(R.id.itemName);
        final EditText mAmount = mView.findViewById(R.id.amount);
        final EditText mNotes = mView.findViewById(R.id.note);

        mNotes.setVisibility(View.GONE);

        mItem.setText(item); //change text to selected itemName

        mAmount.setText(String.valueOf(amount)); //change text to selected itemName amount value
        mAmount.setSelection(String.valueOf(amount).length());

        Button delBut = mView.findViewById(R.id.btnDelete);
        Button btnUpdate = mView.findViewById(R.id.btnUpdate);

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = Integer.parseInt(mAmount.getText().toString());//get allocated amount

                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); //to capture date and time of update to data
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch, now);
                Months months = Months.monthsBetween(epoch, now);

                //analytics data
                String itemNday = item+date;
                String itemNweek = item+weeks.getWeeks();
                String itemNmonth = item+months.getMonths();

                Data data = new Data(item, date, post_key, itemNday, itemNweek, itemNmonth, amount, weeks.getWeeks(), months.getMonths(), null); //use data class to reference items in the database
                budgetRef.child(post_key).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() { //get values from database to be emended
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(BudgetActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show(); //values have been updated
                        }
                        else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show(); //values have not been updated
                        }
                    }
                });
                dialog.dismiss();
            }
        });

        delBut.setOnClickListener(new View.OnClickListener() { //delete existing database instance
            @Override
            public void onClick(View view) {
                budgetRef.child(post_key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() { //get values from database to be deleted
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(BudgetActivity.this, "Deleted Successfully", Toast.LENGTH_SHORT).show(); //values have been deleted
                        }
                        else {
                            Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show(); //values have not been deleted
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}