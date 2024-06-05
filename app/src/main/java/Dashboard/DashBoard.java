package Dashboard;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.xperiencelabs.armenu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xperiencelabs.armenu.MainActivity;
import com.xperiencelabs.armenu.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


import Adapters.FoodAdapter;
import Adapters.FoodAdapterStaff;
import Adapters.FoodSetGet;
import Adapters.FoodSetGetStaff;
import Adapters.HistoryAdapter;
import Adapters.HistorySetGet;
import Others.OurTime;

public class DashBoard extends AppCompatActivity {
    private static final int REQUEST_CODE_QR_SCAN = 49374;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    DatePicker dobpk;
    private Tag tag;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    public static Bitmap userPhoto;
    Button next,registerCustomer;
    Spinner gender;
    private Uri imageUri;
    public static String tableStatus;
    public static String staffStatusMenuUpdate="";
    public static String NFCData="";
    public static String login_staff="Incorrect information";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$";
    Pattern pat = Pattern.compile(emailRegex);

    private List<FoodSetGet>foodList=new ArrayList<>();
    private List<HistorySetGet>foodListorder=new ArrayList<>();
    private List<FoodSetGetStaff>foodListStaff=new ArrayList<>();
     HistoryAdapter historyAdapter;
    public static RecyclerView myHistoryRecyclerView;
    public static RecyclerView recyclerView;
    public static RecyclerView recyclerViewOrders;
    public static RecyclerView recyclerViewStaff;
    Thread thread;
    public static AlertDialog dialog,tabledialog;
    TextView meal_clock,meal_status,backCustReg;

    public static String timeStatus="BreakFast";
    public static String cardNumber="null";
    public static String modeController="normal";
    public static String userID="null";
    public static Handler handler;
    public static ProgressDialog progressDialog;
    public static ProgressDialog progressDialog2,progressDialogNFC,progressDialogNFCReg;
    FoodAdapter adapter;
    FoodAdapterStaff adapterStaff;
//    NfcAdapter nfcAdapter;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
//    PendingIntent pendingIntent;
    public static TextView user_Name,user_Pno,ppUsername,ppUsertopphone,ppUserFname,ppUsersmallphone,ppUserLname;

    public static String scanstatus="null";
    public static String userexist="null";
    public static String fullName;
    public static String uploadedPicID;
    public static String user_email;
    public static String phonenumber;
    public static String userPassword;
    public static String user_dob;
    private ImageView imageView;
    ImageView switchMode,homeBtn,scan_qrCode,customerNav,reg_profile;
    public static FoodSetGet foodSetGetMod=new FoodSetGet("","","","","");
    LinearLayout dashBoardlayout,settingsLayout,feedbackLayout,dashbordinsideLayout,profileLayout,myhistoryLayout,customerReg1,customerReg2;
    public static LinearLayout navigationLayout;
    TextView menu_textv,scan_textv,customer_textv,dob,tableNumber;
    ProgressBar progressBar;
    public static String foodtype="";
    public static String accountUserID="";
    public static String dateOnly="";
    EditText searchEditText,fName,confPass,pass,pinNumConf,pinNumber,userEmail,pNumber,lName;;
    public static String official_staffEmail="";
    Button breakfast,dinner,lunch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        OurTime.init(getApplicationContext());
        progressBar=findViewById(R.id.progress_dashboard);

        tableNumber=findViewById(R.id.table_number);
        SharedPreferences sharedPreferences=getSharedPreferences("table_status",MODE_PRIVATE);
        tableStatus=sharedPreferences.getString("table_number",null);
        if (tableStatus==null){

        }else{
            tableNumber.setText(tableStatus);
        }


        Calendar calendar = Calendar.getInstance();
        String currentdate = DateFormat.getInstance().format(calendar.getTime());
        String[] dateSeparation=currentdate.split(" ");
        String dateOnlyFull=dateSeparation[0]+"";
        String[] tarehe=dateOnlyFull.split("/");
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 because January is represented as 0
        int year = calendar.get(Calendar.YEAR);
        dateOnly=day+"-"+month+"-"+year;


        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
        firebaseAuth= FirebaseAuth.getInstance();
        switchMode=findViewById(R.id.db_mode_switch);
        menu_textv=findViewById(R.id.menu_tv);
        scan_textv=findViewById(R.id.scan_tv);
        customer_textv=findViewById(R.id.customer_tv);
        next=(Button) findViewById(R.id.btnNext);
        registerCustomer=(Button) findViewById(R.id.registerCustomer);
        gender=(Spinner)findViewById(R.id.gendersp);
        dobpk=(DatePicker)findViewById(R.id.dobPicker);
        dob=(TextView) findViewById(R.id.dobEt);
        reg_profile=findViewById(R.id.rp_previewImage);
        fName=findViewById(R.id.rp_firstName);
        lName=findViewById(R.id.rp_lastName);
        pNumber=findViewById(R.id.rp_phoneNumber);
        userEmail=findViewById(R.id.rp_email);
        EditText cardNumber=findViewById(R.id.rp_cardNumber);
        pinNumber=findViewById(R.id.rp_pinNumber);
        pinNumConf=findViewById(R.id.rp_pinNumberConf);
        pass=findViewById(R.id.rp_password);
        confPass=findViewById(R.id.rp_confirmPassword);
        TextView dateofBirth=findViewById(R.id.dobEt);

        searchEditText = findViewById(R.id.searchbar);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called when the text is changed
                String query = s.toString().trim();
                searchMenu(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text is changed
            }
        });

        handler=new Handler(Looper.getMainLooper());
//        ImageView topProfilePic=findViewById(R.id.db_topProfilepic);

        recyclerView=(RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));
        adapter=new FoodAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);




        recyclerViewStaff=(RecyclerView) findViewById(R.id.recyclerviewStaff);
        recyclerViewStaff.setLayoutManager(new LinearLayoutManager(DashBoard.this));
        adapterStaff=new FoodAdapterStaff(getApplicationContext(),new ArrayList<>());
        recyclerViewStaff.setAdapter(adapterStaff);

        recyclerViewOrders=(RecyclerView) findViewById(R.id.recyclervieworders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(DashBoard.this));
        historyAdapter=new HistoryAdapter(getApplicationContext(),new ArrayList<>());
        recyclerViewOrders.setAdapter(historyAdapter);

        navigationLayout = (LinearLayout) findViewById(R.id.navigationLayout);
        String intentReceived=getIntent().getStringExtra("stat")+"";
        if (intentReceived.equals("cancel")){

            recyclerView.setVisibility(View.GONE);
            recyclerViewStaff.setVisibility(View.VISIBLE);
            navigationLayout.setVisibility(View.VISIBLE);
        }else{

        }

        meal_clock=(TextView) findViewById(R.id.clocktv);
        meal_status=(TextView) findViewById(R.id.mealStatustv);
       breakfast=(Button)findViewById(R.id.breakfastbtn);
       lunch=(Button)findViewById(R.id.lunchbtn);
       dinner=(Button)findViewById(R.id.dinnerbtn);
       switchMode.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               changeUserMode(DashBoard.this,"normal");
           }
       });


        homeBtn =  findViewById(R.id.homeBtn);
        scan_qrCode =  findViewById(R.id.scan_qrCode);
        customerNav =  findViewById(R.id.customerNav);
        dashBoardlayout = (LinearLayout) findViewById(R.id.dashBoardLayout);
        settingsLayout = (LinearLayout) findViewById(R.id.settingsLayout);
        feedbackLayout = (LinearLayout) findViewById(R.id.feedbackLayout);
        dashbordinsideLayout = (LinearLayout) findViewById(R.id.dashbordInsideLayout);
        profileLayout = (LinearLayout) findViewById(R.id.profileLayout);
        myhistoryLayout = (LinearLayout) findViewById(R.id.myhistoryLayout);
        customerReg1 = (LinearLayout) findViewById(R.id.ll_customerReg1);
        customerReg2 = (LinearLayout) findViewById(R.id.ll_customerReg2);
        backCustReg=findViewById(R.id.customerBack);


        handler.post(() -> {
            progressDialog = new ProgressDialog(DashBoard.this);
            progressDialog.setMessage("Loading, Please wait...Make sure you have a stable internet connection!");
            progressDialog.setCancelable(false);
        });
        handler.post(() -> {
            progressDialog2 = new ProgressDialog(DashBoard.this);
            progressDialog2.setMessage("Loading, Please wait...Make sure you have a stable internet connection!");
            progressDialog2.setCancelable(false);
        });




        TextView historyView = (TextView) findViewById(R.id.historyTv);

        historyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                viewHistoryAll();

            }
        });


        customerNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                scan_textv.setTextColor(getResources().getColor(R.color.white));
                customerNav.setBackgroundResource(R.drawable.time);
                homeBtn.setBackgroundResource(R.drawable.time1);
                customer_textv.setTextColor(getResources().getColor(R.color.white));
               dashbordinsideLayout.setVisibility(View.GONE);
               settingsLayout.setVisibility(View.GONE);
               feedbackLayout.setVisibility(View.GONE);
                profileLayout.setVisibility(View.GONE);
                myhistoryLayout.setVisibility(View.GONE);
                customerReg1.setVisibility(View.VISIBLE);
                customerReg2.setVisibility(View.GONE);
                recyclerViewOrders.setVisibility(View.GONE);
                ImageView topProfile=findViewById(R.id.sa_topProfilePic);
                ImageView cardProfile=findViewById(R.id.sa_cardProfilePic);
                TextView name=findViewById(R.id.sa_user_Fullname);
                TextView email=findViewById(R.id.sa_user_email);
                TextView pNo=findViewById(R.id.sa_user_phone);
                LinearLayout logout=findViewById(R.id.se_logout);
                LinearLayout deposit=findViewById(R.id.dashboard_deposit);
                deposit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        depositDialogue();
                    }
                });
                navigationLayout.setVisibility(View.VISIBLE);
            }
        });

        scan_qrCode.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {

        Calendar calendar = Calendar.getInstance();
        String currentdate = DateFormat.getInstance().format(calendar.getTime());
        String[] dateSeparation=currentdate.split(" ");
        String dateOnlyFull=dateSeparation[0]+"";
        String[] tarehe=dateOnlyFull.split("/");
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 because January is represented as 0
        int year = calendar.get(Calendar.YEAR);
        String dateOnly=day+"-"+month+"-"+year;
        progressBar.setVisibility(View.VISIBLE);
        customerReg1.setVisibility(View.GONE);
        customerReg2.setVisibility(View.GONE);
        scan_textv.setTextColor(getResources().getColor(R.color.white));
        customerNav.setBackgroundResource(R.color.white);
        menu_textv.setTextColor(getResources().getColor(R.color.white));
        homeBtn.setBackgroundResource(R.color.white);
        customer_textv.setTextColor(getResources().getColor(R.color.white));
        scan_qrCode.setBackgroundResource(R.drawable.time);
        dashbordinsideLayout.setVisibility(View.GONE);
        settingsLayout.setVisibility(View.GONE);
        feedbackLayout.setVisibility(View.GONE);
        dashBoardlayout.setVisibility(View.VISIBLE);
        profileLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        myhistoryLayout.setVisibility(View.GONE);
        navigationLayout.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.VISIBLE);

        DatabaseReference historyref=FirebaseDatabase.getInstance().getReference().child("History").child(dateOnly);
        historyref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    List<HistorySetGet> unfilterdlist = new ArrayList<>();
                    foodListorder.clear();
                    unfilterdlist.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        String foodname=dataSnapshot.child("FoodName").getValue(String.class);
                        String foodprice=dataSnapshot.child("FoodPrice").getValue(String.class);
                        String menustatus=dataSnapshot.child("Status").getValue(String.class);
                        String menudate=dataSnapshot.child("Date").getValue(String.class);
                        String orderid=dataSnapshot.child("orderID").getValue(String.class);
                        String tablenum=dataSnapshot.child("tableNumber").getValue(String.class);
                        HistorySetGet historySetGet=new HistorySetGet(foodname+"",foodprice+"",orderid+"",menudate+"",menustatus+"",tablenum+"");
                        foodListorder.add(historySetGet);
                    }
                    for (HistorySetGet historySetGet: foodListorder){
                        if (historySetGet.getCoupon_status().equals("Not served")){
                            unfilterdlist.add(historySetGet);
                        }
                    }
                    for (HistorySetGet historySetGet: foodListorder){
                        if (historySetGet.getCoupon_status().equals("served")){
                            unfilterdlist.add(historySetGet);
                        }
                    }
                    for (HistorySetGet historySetGet: foodListorder){
                        if (historySetGet.getCoupon_status().equals("canceled")){
                            unfilterdlist.add(historySetGet);
                        }
                    }

                    historyAdapter.updateData(unfilterdlist);
                    historyAdapter.notifyDataSetChanged();
//                    Collections.reverse(unfilterdlist);
                    progressBar.setVisibility(View.GONE);
                }else{
                    Toast.makeText(DashBoard.this, "no sold menus for today", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
});
        historyAdapter.setOnItemClickListener(new HistoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, HistorySetGet historySetGet) {
                if (historySetGet.getCoupon_status().equals("Not served")){
                    alterOrder(historySetGet);
                }else{
                    Toast.makeText(DashBoard.this, "Order "+historySetGet.getCoupon_status(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        Button validate_coupon=findViewById(R.id.btn_validateCouponpr);
//        validate_coupon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(DashBoard.this, QRScannerActivity.class);
//                startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
//            }
//        });

    homeBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        customerNav.setBackgroundResource(R.drawable.time1);
        menu_textv.setTextColor(getResources().getColor(R.color.white));
        homeBtn.setBackgroundResource(R.drawable.time);
        scan_qrCode.setBackgroundResource(R.color.white);
        scan_textv.setTextColor(getResources().getColor(R.color.white));
        dashBoardlayout.setVisibility(View.VISIBLE);
        settingsLayout.setVisibility(View.GONE);
        feedbackLayout.setVisibility(View.GONE);
        dashbordinsideLayout.setVisibility(View.VISIBLE);
        profileLayout.setVisibility(View.GONE);
        myhistoryLayout.setVisibility(View.GONE);
        navigationLayout.setVisibility(View.VISIBLE);
        customerReg1.setVisibility(View.GONE);
        customerReg2.setVisibility(View.GONE);
        recyclerViewStaff.setVisibility(View.VISIBLE);
        recyclerViewOrders.setVisibility(View.GONE);

    }
});

       timeStatus=OurTime.getTimeStatus();
        if(timeStatus!=null)
        {
            switch (timeStatus)
            {
                case "BreakFast":
                    staffStatusMenuUpdate="BreakFast";
                    breakfast.setBackgroundResource(R.drawable.foodback);
                    breakfast.setTextColor(getResources().getColor(R.color.white));
                    lunch.setBackgroundResource(R.drawable.viewbalance);
                    lunch.setTextColor(getResources().getColor(R.color.black));
                    dinner.setBackgroundResource(R.drawable.viewbalance);
                    dinner.setTextColor(getResources().getColor(R.color.black));
                    progressBar.setVisibility(View.VISIBLE);
                    foodList.clear();
                    foodListStaff.clear();
                    DatabaseReference breakfastRef = FirebaseDatabase.getInstance().getReference()
                            .child("MENUS")
                            .child("Breakfast");

                    breakfastRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            foodList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String menuPrice = dataSnapshot.child("price").getValue(String.class);
                                String menuName = dataSnapshot.child("foodName").getValue(String.class);
                                String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                                String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                                String snapID=dataSnapshot.getKey().toString();


                                DatabaseReference breakfastRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                        .child("Coupons Used")
                                        .child(dateOnly).child(menuName);
                                breakfastRefsold.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String soldIdadi = snapshot.getValue(String.class);
                                            String[] sep = soldIdadi.split(" ");
                                            String idadi = sep[0];

                                            // Check if the menu item already exists in the list
                                            boolean found = false;
                                            for (FoodSetGetStaff item : foodListStaff) {
                                                if (item.getFoodName().equals(menuName)) {
                                                    // Update the existing item
                                                    item.setFoodPrice(menuPrice + " TZS");
                                                    item.setFoodStatus(menustatus + "");
                                                    item.setItemImage(menuUrl);
                                                    item.setSoldNumber(idadi);
                                                    found = true;
                                                    break;
                                                }
                                            }

                                            // If the menu item is not found, add it to the list
                                            if (!found) {
                                                FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                                FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                                foodList.add(foodSetGet);
                                                foodListStaff.add(foodSetGetStaff);
                                            }
                                        }else{
                                            FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                            FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                            foodList.add(foodSetGet);
                                            foodListStaff.add(foodSetGetStaff);
                                        }
                                        adapter.updateData(foodList);
                                        adapterStaff.updateData(foodListStaff);
                                        adapterStaff.notifyDataSetChanged();
                                        Collections.reverse(foodList);
                                        Collections.reverse(foodListStaff);
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled event if needed
                        }
                    });

                    break;

                case "Lunch":
                    staffStatusMenuUpdate="Lunch";
                    breakfast.setBackgroundResource(R.drawable.viewbalance);
                    breakfast.setTextColor(getResources().getColor(R.color.black));
                    lunch.setBackgroundResource(R.drawable.foodback);
                    lunch.setTextColor(getResources().getColor(R.color.white));
                    dinner.setBackgroundResource(R.drawable.viewbalance);
                    dinner.setTextColor(getResources().getColor(R.color.black));
                    progressBar.setVisibility(View.VISIBLE);
                    foodList.clear();
                    foodListStaff.clear();
                    DatabaseReference lunchRef = FirebaseDatabase.getInstance().getReference()
                            .child("MENUS")
                            .child("Lunch");

                    lunchRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            foodList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String menuPrice = dataSnapshot.child("price").getValue(String.class);
                                String menuName = dataSnapshot.child("foodName").getValue(String.class);
                                String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                                String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                                String snapID=dataSnapshot.getKey().toString();

                                DatabaseReference lunchRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                        .child("Coupons Used")
                                        .child(dateOnly).child(menuName);
                                lunchRefsold.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String soldIdadi = snapshot.getValue(String.class);
                                            String[] sep = soldIdadi.split(" ");
                                            String idadi = sep[0];

                                            // Check if the menu item already exists in the list
                                            boolean found = false;
                                            for (FoodSetGetStaff item : foodListStaff) {
                                                if (item.getFoodName().equals(menuName)) {
                                                    // Update the existing item
                                                    item.setFoodPrice(menuPrice + " TZS");
                                                    item.setFoodStatus(menustatus + "");
                                                    item.setItemImage(menuUrl);
                                                    item.setSoldNumber(idadi);
                                                    found = true;
                                                    break;
                                                }
                                            }

                                            // If the menu item is not found, add it to the list
                                            if (!found) {
                                                FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                                FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                                foodList.add(foodSetGet);
                                                foodListStaff.add(foodSetGetStaff);
                                            }
                                        }else{
                                            FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                            FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                            foodList.add(foodSetGet);
                                            foodListStaff.add(foodSetGetStaff);
                                        }
                                        adapter.updateData(foodList);
                                        adapterStaff.updateData(foodListStaff);
                                        adapterStaff.notifyDataSetChanged();
                                        Collections.reverse(foodList);
                                        Collections.reverse(foodListStaff);
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled event if needed
                        }
                    });

                    break;


                case "Dinner":
                    staffStatusMenuUpdate="Dinner";
                    breakfast.setBackgroundResource(R.drawable.viewbalance);
                    breakfast.setTextColor(getResources().getColor(R.color.black));
                    lunch.setBackgroundResource(R.drawable.viewbalance);
                    lunch.setTextColor(getResources().getColor(R.color.black));
                    dinner.setBackgroundResource(R.drawable.foodback);
                    dinner.setTextColor(getResources().getColor(R.color.white));;
                    progressBar.setVisibility(View.VISIBLE);
                    foodList.clear();
                    foodListStaff.clear();
                    DatabaseReference dinnerRef = FirebaseDatabase.getInstance().getReference()
                            .child("MENUS")
                            .child("Dinner");

                    dinnerRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            foodList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String menuPrice = dataSnapshot.child("price").getValue(String.class);
                                String menuName = dataSnapshot.child("foodName").getValue(String.class);
                                String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                                String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                                String snapID=dataSnapshot.getKey().toString();

                                DatabaseReference dinnerRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                        .child("Coupons Used")
                                        .child(dateOnly).child(menuName);
                                dinnerRefsold.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String soldIdadi = snapshot.getValue(String.class);
                                            String[] sep = soldIdadi.split(" ");
                                            String idadi = sep[0];

                                            // Check if the menu item already exists in the list
                                            boolean found = false;
                                            for (FoodSetGetStaff item : foodListStaff) {
                                                if (item.getFoodName().equals(menuName)) {
                                                    // Update the existing item
                                                    item.setFoodPrice(menuPrice + " TZS");
                                                    item.setFoodStatus(menustatus + "");
                                                    item.setItemImage(menuUrl);
                                                    item.setSoldNumber(idadi);
                                                    found = true;
                                                    break;
                                                }
                                            }

                                            // If the menu item is not found, add it to the list
                                            if (!found) {
                                                FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                                FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                                foodList.add(foodSetGet);
                                                foodListStaff.add(foodSetGetStaff);
                                            }
                                        }else{
                                            FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                            FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                            foodList.add(foodSetGet);
                                            foodListStaff.add(foodSetGetStaff);
                                        }
                                        adapter.updateData(foodList);
                                        adapterStaff.updateData(foodListStaff);
                                        adapterStaff.notifyDataSetChanged();
                                        Collections.reverse(foodList);
                                        Collections.reverse(foodListStaff);
                                        adapter.notifyDataSetChanged();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle onCancelled event if needed
                        }
                    });
                    break;

                default:
                    break;

            }
        }

        adapterStaff.setOnItemClickListener(new FoodAdapterStaff.OnItemClickListener() {
            @Override
            public void onItemClick(int position, FoodSetGetStaff foodSetGetStaffStaff) {
                updateMenu(foodSetGetStaffStaff);
            }
        });

        adapter.setOnItemClickListener(new FoodAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, FoodSetGet foodSetGet) {
                String text=foodSetGet.getMenuAvailability()+"";

                if (text.equals("Available")){
                    alertdialogBuilder(foodSetGet);
                }else{
                    Toast.makeText(DashBoard.this, foodSetGet.getFoodName()+" not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
       breakfast.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               staffStatusMenuUpdate="Breakfast";
               breakfast.setBackgroundResource(R.drawable.foodback);
               breakfast.setTextColor(getResources().getColor(R.color.white));
               lunch.setBackgroundResource(R.drawable.viewbalance);
               lunch.setTextColor(getResources().getColor(R.color.black));
               dinner.setBackgroundResource(R.drawable.viewbalance);
               dinner.setTextColor(getResources().getColor(R.color.black));

               progressBar.setVisibility(View.VISIBLE);
               foodList.clear();
               foodListStaff.clear();
               DatabaseReference breakfastRef = FirebaseDatabase.getInstance().getReference()
                       .child("MENUS")
                       .child("Breakfast");

               breakfastRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       foodList.clear();
                       for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                           String menuPrice = dataSnapshot.child("price").getValue(String.class);
                           String menuName = dataSnapshot.child("foodName").getValue(String.class);
                           String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                           String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                           String snapID=dataSnapshot.getKey().toString();


                           DatabaseReference breakfastRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                   .child("Coupons Used")
                                   .child(dateOnly).child(menuName);
                           breakfastRefsold.addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   if (snapshot.exists()) {
                                       String soldIdadi = snapshot.getValue(String.class);
                                       String[] sep = soldIdadi.split(" ");
                                       String idadi = sep[0];

                                       // Check if the menu item already exists in the list
                                       boolean found = false;
                                       for (FoodSetGetStaff item : foodListStaff) {
                                           if (item.getFoodName().equals(menuName)) {
                                               // Update the existing item
                                               item.setFoodPrice(menuPrice + " TZS");
                                               item.setFoodStatus(menustatus + "");
                                               item.setItemImage(menuUrl);
                                               item.setSoldNumber(idadi);
                                               found = true;
                                               break;
                                           }
                                       }

                                       // If the menu item is not found, add it to the list
                                       if (!found) {
                                           FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                           FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                           foodList.add(foodSetGet);
                                           foodListStaff.add(foodSetGetStaff);
                                       }
                                   }else{
                                       FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                       FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                       foodList.add(foodSetGet);
                                       foodListStaff.add(foodSetGetStaff);
                                   }
                                   adapter.updateData(foodList);
                                   adapterStaff.updateData(foodListStaff);
                                   adapterStaff.notifyDataSetChanged();
                                   Collections.reverse(foodList);
                                   Collections.reverse(foodListStaff);
                                   adapter.notifyDataSetChanged();
                                   progressBar.setVisibility(View.GONE);
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });

                       }
                   }
                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {
                       // Handle onCancelled event if needed
                   }
               });
           }
           });
       lunch.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               staffStatusMenuUpdate="Lunch";
               breakfast.setBackgroundResource(R.drawable.viewbalance);
               breakfast.setTextColor(getResources().getColor(R.color.black));
               lunch.setBackgroundResource(R.drawable.foodback);
               lunch.setTextColor(getResources().getColor(R.color.white));
               dinner.setBackgroundResource(R.drawable.viewbalance);
               dinner.setTextColor(getResources().getColor(R.color.black));;
               progressBar.setVisibility(View.VISIBLE);
               foodList.clear();
               foodListStaff.clear();

               DatabaseReference lunchRef = FirebaseDatabase.getInstance().getReference()
                       .child("MENUS")
                       .child("Lunch");

               lunchRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       foodList.clear();
                       for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                           String menuPrice = dataSnapshot.child("price").getValue(String.class);
                           String menuName = dataSnapshot.child("foodName").getValue(String.class);
                           String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                           String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                           String snapID=dataSnapshot.getKey().toString();

                           DatabaseReference lunchRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                   .child("Coupons Used")
                                   .child(dateOnly).child(menuName);
                           lunchRefsold.addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   if (snapshot.exists()) {
                                       String soldIdadi = snapshot.getValue(String.class);
                                       String[] sep = soldIdadi.split(" ");
                                       String idadi = sep[0];

                                       // Check if the menu item already exists in the list
                                       boolean found = false;
                                       for (FoodSetGetStaff item : foodListStaff) {
                                           if (item.getFoodName().equals(menuName)) {
                                               // Update the existing item
                                               item.setFoodPrice(menuPrice + " TZS");
                                               item.setFoodStatus(menustatus + "");
                                               item.setItemImage(menuUrl);
                                               item.setSoldNumber(idadi);
                                               found = true;
                                               break;
                                           }
                                       }

                                       // If the menu item is not found, add it to the list
                                       if (!found) {
                                           FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                           FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                           foodList.add(foodSetGet);
                                           foodListStaff.add(foodSetGetStaff);
                                       }
                                   }else{
                                       FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                       FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                       foodList.add(foodSetGet);
                                       foodListStaff.add(foodSetGetStaff);
                                   }
                                   adapter.updateData(foodList);
                                   adapterStaff.updateData(foodListStaff);
                                   adapterStaff.notifyDataSetChanged();
                                   Collections.reverse(foodList);
                                   Collections.reverse(foodListStaff);
                                   adapter.notifyDataSetChanged();
                                   progressBar.setVisibility(View.GONE);
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });
                       }
                   }
                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {
                       // Handle onCancelled event if needed
                   }
               });

           }
       });
       dinner.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               staffStatusMenuUpdate="Dinner";
               breakfast.setBackgroundResource(R.drawable.viewbalance);
               breakfast.setTextColor(getResources().getColor(R.color.black));
               lunch.setBackgroundResource(R.drawable.viewbalance);
               lunch.setTextColor(getResources().getColor(R.color.black));
               dinner.setBackgroundResource(R.drawable.foodback);
               dinner.setTextColor(getResources().getColor(R.color.white));;
               progressBar.setVisibility(View.VISIBLE);
               foodList.clear();
               foodListStaff.clear();

               DatabaseReference dinnerRef = FirebaseDatabase.getInstance().getReference()
                       .child("MENUS")
                       .child("Dinner");

               dinnerRef.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                       foodList.clear();
                       for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                           String menuPrice = dataSnapshot.child("price").getValue(String.class);
                           String menuName = dataSnapshot.child("foodName").getValue(String.class);
                           String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                           String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                           String snapID=dataSnapshot.getKey().toString();

                           DatabaseReference dinnerRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                   .child("Coupons Used")
                                   .child(dateOnly).child(menuName);
                           dinnerRefsold.addValueEventListener(new ValueEventListener() {
                               @Override
                               public void onDataChange(@NonNull DataSnapshot snapshot) {
                                   if (snapshot.exists()) {
                                       String soldIdadi = snapshot.getValue(String.class);
                                       String[] sep = soldIdadi.split(" ");
                                       String idadi = sep[0];

                                       // Check if the menu item already exists in the list
                                       boolean found = false;
                                       for (FoodSetGetStaff item : foodListStaff) {
                                           if (item.getFoodName().equals(menuName)) {
                                               // Update the existing item
                                               item.setFoodPrice(menuPrice + " TZS");
                                               item.setFoodStatus(menustatus + "");
                                               item.setItemImage(menuUrl);
                                               item.setSoldNumber(idadi);
                                               found = true;
                                               break;
                                           }
                                       }

                                       // If the menu item is not found, add it to the list
                                       if (!found) {
                                           FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                           FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                           foodList.add(foodSetGet);
                                           foodListStaff.add(foodSetGetStaff);
                                       }
                                   }else{
                                       FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                       FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                       foodList.add(foodSetGet);
                                       foodListStaff.add(foodSetGetStaff);
                                   }
                                   adapter.updateData(foodList);
                                   adapterStaff.updateData(foodListStaff);
                                   adapterStaff.notifyDataSetChanged();
                                   Collections.reverse(foodList);
                                   Collections.reverse(foodListStaff);
                                   adapter.notifyDataSetChanged();
                                   progressBar.setVisibility(View.GONE);
                               }

                               @Override
                               public void onCancelled(@NonNull DatabaseError error) {

                               }
                           });
                       }
                   }
                   @Override
                   public void onCancelled(@NonNull DatabaseError error) {
                       // Handle onCancelled event if needed
                   }
               });

           }
       });
        Thread thread=new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(10);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Calendar calendar = Calendar.getInstance();
                                String currentdate = DateFormat.getInstance().format(calendar.getTime());
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                                String formattedTime = simpleDateFormat.format(new Date());

                                meal_clock.setText(formattedTime);

                                int currentHour=calendar.get(Calendar.HOUR_OF_DAY);
                                if(currentHour>=0 && currentHour<12)
                                {
                                    meal_status.setText("BreakFast");
                                }else if(currentHour>=12 && currentHour<16)
                                {
                                    meal_status.setText("Lunch");
                                } else if (currentHour>=16 && currentHour<24) {
                                    meal_status.setText("Dinner");
                                }else{
                                    meal_status.setText("Ngano");
                                }

                            }
                        });
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        };
        thread.start();

    }


    public void alertdialogBuilder(FoodSetGet foodSetGet){
        AlertDialog.Builder builder=new AlertDialog.Builder(DashBoard.this);
        View popupView = LayoutInflater.from(DashBoard.this).inflate(R.layout.alert_dialogue, null);
        builder.setView(popupView);

        LinearLayout confirm=popupView.findViewById(R.id.ad_confirm_layout);
        LinearLayout error=popupView.findViewById(R.id.ad_error_layout);
        LinearLayout success=popupView.findViewById(R.id.ad_success_layout);
        Button confirmbtn=popupView.findViewById(R.id.ad_confirm_button);
        Button placeOrder=popupView.findViewById(R.id.place_order_btn);
        Button depositbtn=popupView.findViewById(R.id.ad_deposit_button);
        Button viewCouponbtn=popupView.findViewById(R.id.ad_viewCoupon_button);
        ImageView foodImage=popupView.findViewById(R.id.fc_foodImage);
        TextView foodName=popupView.findViewById(R.id.fc_foodName);
        TextView foodprice=popupView.findViewById(R.id.fc_foodPrice);
        TextView dismissbutton=popupView.findViewById(R.id.ad_dismissbtn);
        TextView alertmessage=popupView.findViewById(R.id.fc_alertMessage);

        placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tableStatus==null){
                    AlertDialog.Builder builder_table=new AlertDialog.Builder(DashBoard.this);
                    View popupView = LayoutInflater.from(DashBoard.this).inflate(R.layout.table_alert, null);
                    builder_table.setView(popupView);
                    tabledialog = builder_table.create();
                    tabledialog.setCancelable(true);
                    Button assign=popupView.findViewById(R.id.btn_staffLogin);
                    assign.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            changeUserMode(DashBoard.this,"Assign");
                            tabledialog.dismiss();

                        }
                    });
                    builder_table.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Toast.makeText(DashBoard.this, "Can't place order without setting table number!", Toast.LENGTH_LONG).show();
                        }
                    });
                    tabledialog.show();
                }else {
                    placeorder(foodSetGet);
                    dialog.dismiss();
                }
            }
        });

        alertmessage.setText(foodSetGet.getFoodPrice()+" will be required for this menu.");

        Glide.with(DashBoard.this)
                .load(foodSetGet.getItemImage())
                .into(foodImage);
        foodName.setText(foodSetGet.getFoodName()+"");
        foodprice.setText(foodSetGet.getFoodPrice());

        dismissbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView alertmessageSucces=popupView.findViewById(R.id.fc_foodStatus);

        alertmessageSucces.setText(foodSetGet.getFoodPrice()+" deducted from your account");
        TextView dismissbuttonSucces=popupView.findViewById(R.id.ad_dismisSucces);
        dismissbuttonSucces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView dismissbuttonError=popupView.findViewById(R.id.ad_dismissError);
        dismissbuttonError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(DashBoard.this, MainActivity.class);
                intent.putExtra("foodtype",foodSetGet.getFoodName());
                foodtype=foodSetGet.getFoodName();
                startActivity(intent);
//                startActivity(new Intent(DashBoard.this, MainActivity.class));

//                progressDialogNFC.show();
//                foodSetGetMod=foodSetGet;
//                scanstatus="scan";
//                nfcReader.startListening();
//                progressDialogNFC.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        scanstatus="null";
//                    }
//                });
//
//
//
             }
        });
        viewCouponbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            dialog.dismiss();
//            viewHistoryAll();
            }
        });
        depositbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                confirm.setVisibility(View.VISIBLE);
//                success.setVisibility(View.GONE);
//                error.setVisibility(View.GONE);
                dialog.dismiss();
//                viewHistoryAll();

            }
        });

        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    public void depositDialogue(){

    }
    public void threadDestroy(){
        thread.interrupt();
    }

    public void placeorder(FoodSetGet foodSetGet){
        progressDialog2.show();
        Calendar calendar = Calendar.getInstance();
        String currentdate = DateFormat.getInstance().format(calendar.getTime());
        String[] dateSeparation=currentdate.split(" ");
        String dateOnlyFull=dateSeparation[0]+"";
        String[] tarehe=dateOnlyFull.split("/");
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 because January is represented as 0
        int year = calendar.get(Calendar.YEAR);
        String dateOnly=day+"-"+month+"-"+year;
        DatabaseReference placeord=FirebaseDatabase.getInstance().getReference().child("Tables")
                .child(dateOnly)
                .child(tableStatus).push();
        placeord.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                placeord.child("FoodName").setValue(foodSetGet.getFoodName());
                placeord.child("FoodPrice").setValue(foodSetGet.getFoodPrice());
                placeord.child("Status").setValue("Not served");
                placeord.child("Date").setValue(currentdate+" Hrs");
                placeord.child("orderID").setValue(snapshot.getKey().trim());
                placeord.child("tableNumber").setValue(tableNumber.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        DatabaseReference hist=FirebaseDatabase.getInstance().getReference().child("History").child(dateOnly)
                                .child(snapshot.getKey().toString());
                        hist.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                hist.child("FoodName").setValue(foodSetGet.getFoodName());
                                hist.child("FoodPrice").setValue(foodSetGet.getFoodPrice());
                                hist.child("Status").setValue("Not served");
                                hist.child("Date").setValue(currentdate+" Hrs");
                                hist.child("orderID").setValue(snapshot.getKey().trim());
                                hist.child("tableNumber").setValue(tableNumber.getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        progressDialog2.dismiss();
                                        Toast.makeText(DashBoard.this, "Order placed! please wait a few minutes and it will be served to you!", Toast.LENGTH_LONG).show();
                                        dialog.dismiss();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void changeUserMode(Context context,String tableassign){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.mode_control,null);
        builder.setView(view);
        AlertDialog dialog1=builder.create();

        AlertDialog.Builder builder1=new AlertDialog.Builder(context);
        LayoutInflater inflater2=LayoutInflater.from(context);
        View view2=inflater2.inflate(R.layout.staff_login,null);
        builder1.setView(view2);
        AlertDialog dialog2=builder1.create();

        LinearLayout staffmode=view.findViewById(R.id.staffMode);
        LinearLayout changepass=view.findViewById(R.id.changePassword);
        LinearLayout changetablenumber=view.findViewById(R.id.updatetablenumber);
        ImageView stafficon=view.findViewById(R.id.staffDot);
        TextView stafft=view.findViewById(R.id.staffText);
        if (modeController.equals("normal")){
//            Glide.with(context)
//                    .load(R.drawable.orange_dot)
//                    .into(normalicon);
//            Glide.with(context)
//                    .load(R.drawable.white_dot)
//                    .into(stafficon);
            stafft.setText("Switch to staff mode");
            changepass.setVisibility(View.GONE);
            changetablenumber.setVisibility(View.GONE);
            dialog1.show();
        }else{
//            Glide.with(context)
//                    .load(R.drawable.orange_dot)
//                    .into(stafficon);
//            Glide.with(context)
//                    .load(R.drawable.white_dot)
//                    .into(normalicon);
            stafft.setText("Switch to normal mode");
            changepass.setVisibility(View.VISIBLE);
            changetablenumber.setVisibility(View.VISIBLE);
            changetablenumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog1.dismiss();
                    AlertDialog.Builder builderpass=new AlertDialog.Builder(context);
                    LayoutInflater inflater=LayoutInflater.from(context);
                    View view=inflater.inflate(R.layout.password_update,null);
                    builderpass.setView(view);
                    AlertDialog dialogpass=builderpass.create();
                    dialogpass.show();
                    TextView passtv=view.findViewById(R.id.update_passwordtv);
                    TextView tabletv=view.findViewById(R.id.update_tabletv);
                    EditText passet=view.findViewById(R.id.update_passwordet);
                    EditText tableset=view.findViewById(R.id.update_tableet);
                    passtv.setVisibility(View.GONE);
                    passet.setVisibility(View.GONE);
                    tabletv.setVisibility(View.VISIBLE);
                    tableset.setVisibility(View.VISIBLE);
                    Button upd=view.findViewById(R.id.password_updateButton);
                    upd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String newtablenumber=tableset.getText().toString().trim();
                            if (newtablenumber.isEmpty()){
                                tableset.setError("Required");
                                tableset.requestFocus();
                            } else{
                                SharedPreferences sharedPreferences=getSharedPreferences("table_status",MODE_PRIVATE);
                                SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putString("table_number","TABLE "+newtablenumber);
                                editor.apply();
//                                tableNumber.setText("TABLE "+newtablenumber);
                                tableStatus="TABLE "+newtablenumber;
                                dialogpass.dismiss();
                            }
                        }
                    });
                }
            });
            changepass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog1.dismiss();
                    AlertDialog.Builder builderpass=new AlertDialog.Builder(context);
                    LayoutInflater inflater=LayoutInflater.from(context);
                    View view=inflater.inflate(R.layout.password_update,null);
                    builderpass.setView(view);
                    AlertDialog dialogpass=builderpass.create();
                    dialogpass.show();
                    TextView passtv=view.findViewById(R.id.update_passwordtv);
                    TextView tabletv=view.findViewById(R.id.update_tabletv);
                    EditText passet=view.findViewById(R.id.update_passwordet);
                    EditText tableset=view.findViewById(R.id.update_tableet);
                    passtv.setVisibility(View.VISIBLE);
                    passet.setVisibility(View.VISIBLE);
                    tabletv.setVisibility(View.GONE);
                    tableset.setVisibility(View.GONE);
                    Button upd=view.findViewById(R.id.password_updateButton);
                    upd.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String newpassword=passet.getText().toString().trim();
                            if (newpassword.isEmpty()){
                                passet.setError("Required");
                                passet.requestFocus();
                            } else if (newpassword.length()<6) {
                                passet.setError("Too short,atleast 6 characters!");
                                passet.requestFocus();
                            }else{
                                progressDialog2.show();
                                DatabaseReference staffRefUpd = FirebaseDatabase.getInstance().getReference().child("Staff Members");
                                staffRefUpd.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                                String userEmail=dataSnapshot.child("email").getValue(String.class);
                                                String userpssw=dataSnapshot.child("password").getValue(String.class);
                                                String key=dataSnapshot.getKey();
                                                if (userEmail.trim().equals(official_staffEmail)){
                                                    staffRefUpd.child(key).child("password").setValue(newpassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            dialogpass.dismiss();
                                                            progressDialog2.dismiss();
                                                        }
                                                    });
                                                    break;
                                                }
                                            }
                                        }else{
                                            Toast.makeText(DashBoard.this, "No registered staff!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    });
                }
            });
            dialog1.show();
        }
//        dialog1.show();
        staffmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEditText.setText("");
                dialog1.dismiss();
                if (modeController.equals("normal")){
                    dialog2.setCancelable(false);
                    dialog1.dismiss();
                    ImageView cancel=view2.findViewById(R.id.cancel_dialogue);
                    Button signIn=view2.findViewById(R.id.btn_staffLogin);
                    EditText staffemail=view2.findViewById(R.id.staffUsername);
                    EditText staffpass=view2.findViewById(R.id.staff_password);
                    signIn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String enteredEmail=staffemail.getText().toString().trim();
                            String passwordSt=staffpass.getText().toString().trim();
                            if (enteredEmail.isEmpty()){
                                staffemail.setError("Email required!");
                                staffemail.requestFocus();
                                return;
                            } else if (passwordSt.isEmpty()) {
                                staffpass.setError("Password required!");
                                staffpass.requestFocus();
                                return;
                            }else {
                                DatabaseReference staffRef = FirebaseDatabase.getInstance().getReference().child("Staff Members");
                                staffRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()){
                                            for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                                                String userEmail=dataSnapshot.child("email").getValue(String.class);
                                                if (userEmail.trim().equals(enteredEmail)){
                                                    String key=dataSnapshot.getKey();
                                                    DatabaseReference passwdref=staffRef.child(key);
                                                    passwdref.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            String userpssw=snapshot.child("password").getValue(String.class);

                                                            if (userpssw.trim().equals(passwordSt.trim())){
                                                                official_staffEmail=enteredEmail;
                                                                login_staff="success";
                                                                modeController="staff";
                                                                recyclerView.setVisibility(View.GONE);
                                                                recyclerViewStaff.setVisibility(View.VISIBLE);
                                                                navigationLayout.setVisibility(View.VISIBLE);
                                                                tableNumber.setText("STAFF USE");
                                                                dialog2.dismiss();

                                                            }else{
                                                                Toast.makeText(DashBoard.this, "Incorrect information!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                                    break;
                                                }else{
                                                    Toast.makeText(DashBoard.this, "Incorrect information!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }else{
                                            Toast.makeText(DashBoard.this, "No registered staff!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }

                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            modeController="normal";
                            dialog2.dismiss();
                        }
                    });

                    dialog2.show();
                }else{
                    modeController="normal";

                    dashBoardlayout.setVisibility(View.VISIBLE);
                    settingsLayout.setVisibility(View.GONE);
                    feedbackLayout.setVisibility(View.GONE);
                    dashbordinsideLayout.setVisibility(View.VISIBLE);
                    profileLayout.setVisibility(View.GONE);
                    myhistoryLayout.setVisibility(View.GONE);
                    navigationLayout.setVisibility(View.GONE);
                    customerReg1.setVisibility(View.GONE);
                    customerReg2.setVisibility(View.GONE);
                    recyclerViewStaff.setVisibility(View.GONE);
                    recyclerViewOrders.setVisibility(View.GONE);
                    tableNumber.setText(tableStatus+"");
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerViewStaff.setVisibility(View.GONE);
                    navigationLayout.setVisibility(View.GONE);
                    dialog1.dismiss();
                }


            }
        });

        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams params=dialog1.getWindow().getAttributes();
        params.gravity= Gravity.TOP|Gravity.END;
        params.x=100;
        params.y=200;
        dialog1.getWindow().setAttributes(params);

    }
    private void alterOrder(HistorySetGet historySetGet){
        AlertDialog.Builder builder4=new AlertDialog.Builder(DashBoard.this);
        LayoutInflater inflater=LayoutInflater.from(DashBoard.this);
        View view=inflater.inflate(R.layout.order,null);
        builder4.setView(view);
        AlertDialog dialog4 = builder4.create();
        dialog4.setCancelable(false);
        dialog4.show();

        Button cancelorder=view.findViewById(R.id.cancelOrder);
        Button serveorder=view.findViewById(R.id.serveOrder);
        ImageView dismiss=view.findViewById(R.id.orderdismiss);
        TextView texttv=view.findViewById(R.id.toptext);
        texttv.setText(historySetGet.getFood_name()+" at "+historySetGet.getCoupon_serveTime());
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog4.dismiss();
            }
        });
        serveorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog2.show();
                Calendar calendar = Calendar.getInstance();
                String currentdate = DateFormat.getInstance().format(calendar.getTime());
                String[] dateSeparation=currentdate.split(" ");
                String dateOnlyFull=dateSeparation[0]+"";
                String[] tarehe=dateOnlyFull.split("/");
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 because January is represented as 0
                int year = calendar.get(Calendar.YEAR);
                String dateOnly=day+"-"+month+"-"+year;
                DatabaseReference placeord=FirebaseDatabase.getInstance().getReference().child("Tables")
                        .child(dateOnly)
                        .child(historySetGet.getCoupon_serveTime()).child(historySetGet.getCoupon_reference_Number());
                placeord.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        placeord.child("Status").setValue("served").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                DatabaseReference hist=FirebaseDatabase.getInstance().getReference().child("History").child(dateOnly)
                                        .child(historySetGet.getCoupon_reference_Number());
                                hist.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        hist.child("Status").setValue("served").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog2.dismiss();
                                                Toast.makeText(DashBoard.this, "Success!", Toast.LENGTH_LONG).show();
                                                dialog4.dismiss();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        cancelorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog2.show();
                Calendar calendar = Calendar.getInstance();
                String currentdate = DateFormat.getInstance().format(calendar.getTime());
                String[] dateSeparation=currentdate.split(" ");
                String dateOnlyFull=dateSeparation[0]+"";
                String[] tarehe=dateOnlyFull.split("/");
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH) + 1; // Adding 1 because January is represented as 0
                int year = calendar.get(Calendar.YEAR);
                String dateOnly=day+"-"+month+"-"+year;
                DatabaseReference placeord=FirebaseDatabase.getInstance().getReference().child("Tables")
                        .child(dateOnly)
                        .child(historySetGet.getCoupon_serveTime()).child(historySetGet.getCoupon_reference_Number());
                placeord.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        placeord.child("Status").setValue("canceled").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                DatabaseReference hist=FirebaseDatabase.getInstance().getReference().child("History").child(dateOnly)
                                        .child(historySetGet.getCoupon_reference_Number());
                                hist.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        hist.child("Status").setValue("canceled").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                progressDialog2.dismiss();
                                                Toast.makeText(DashBoard.this, "Success!", Toast.LENGTH_LONG).show();
                                                dialog4.dismiss();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
    public void updateMenu(FoodSetGetStaff foodSetGetStaff){
        AlertDialog.Builder builder3=new AlertDialog.Builder(DashBoard.this);
        LayoutInflater inflater=LayoutInflater.from(DashBoard.this);
        View view=inflater.inflate(R.layout.staff_update_menu,null);
        // Retrieve the RadioGroup and RadioButtons from the inflated view
        RadioGroup modeRadioGroup = view.findViewById(R.id.modeRadioGroup);
        RadioButton availableRadioButton = view.findViewById(R.id.availableRadioButton);
        RadioButton finishedRadioButton = view.findViewById(R.id.finishedRadioButton);

        // Capture the foodstatus from the FoodSetGetStaff object
        String foodStatus = foodSetGetStaff.getFoodStatus();

        // Set the checked state of the corresponding RadioButton based on the foodstatus
        if (foodStatus.equals("Available")) {
            availableRadioButton.setChecked(true);
        } else if (foodStatus.equals("Finished")) {
            finishedRadioButton.setChecked(true);
        }else{
            finishedRadioButton.setChecked(true);
        }
        Button update_menu=view.findViewById(R.id.btn_staffUpdateMenu);
        update_menu.setVisibility(View.GONE);
        builder3.setView(view);
        AlertDialog dialog3 = builder3.create();
        dialog3.show();

        // Set OnClickListener for the RadioGroup to get the checked RadioButton
        modeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Retrieve the text of the checked RadioButton
                RadioButton checkedRadioButton = view.findViewById(checkedId);
                String checkedText = checkedRadioButton.getText().toString();

                if (checkedText.equals(foodStatus)){
                    update_menu.setVisibility(View.GONE);
                }else{
                    update_menu.setVisibility(View.VISIBLE);
                    update_menu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            DatabaseReference updtMenuref=FirebaseDatabase.getInstance().getReference()
                                    .child("MENUS")
                                    .child(staffStatusMenuUpdate)
                                    .child(foodSetGetStaff.getMenuID());
                            updtMenuref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()){
                                        updtMenuref.child("statusMode").setValue(checkedText).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (staffStatusMenuUpdate.equals("Dinner")){
                                                    dialog3.dismiss();
                                                    breakfast.setBackgroundResource(R.drawable.viewbalance);
                                                    breakfast.setTextColor(getResources().getColor(R.color.black));
                                                    lunch.setBackgroundResource(R.drawable.viewbalance);
                                                    lunch.setTextColor(getResources().getColor(R.color.black));
                                                    dinner.setBackgroundResource(R.drawable.foodback);
                                                    dinner.setTextColor(getResources().getColor(R.color.white));

                                                } else if (staffStatusMenuUpdate.equals("Lunch")) {
                                                    dialog3.dismiss();
                                                    breakfast.setBackgroundResource(R.drawable.viewbalance);
                                                    breakfast.setTextColor(getResources().getColor(R.color.black));
                                                    lunch.setBackgroundResource(R.drawable.foodback);
                                                    lunch.setTextColor(getResources().getColor(R.color.white));
                                                    dinner.setBackgroundResource(R.drawable.viewbalance);
                                                    dinner.setTextColor(getResources().getColor(R.color.black));;
                                                }else if (staffStatusMenuUpdate.equals("Breakfast")){
                                                    dialog3.dismiss();
                                                    breakfast.setBackgroundResource(R.drawable.foodback);
                                                    breakfast.setTextColor(getResources().getColor(R.color.white));
                                                    lunch.setBackgroundResource(R.drawable.viewbalance);
                                                    lunch.setTextColor(getResources().getColor(R.color.black));
                                                    dinner.setBackgroundResource(R.drawable.viewbalance);
                                                    dinner.setTextColor(getResources().getColor(R.color.black));
                                                }
                                                foodListStaff.clear();
                                                DatabaseReference allRef = FirebaseDatabase.getInstance().getReference()
                                                        .child("MENUS")
                                                        .child(staffStatusMenuUpdate);

                                                allRef.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                            String menuPrice = dataSnapshot.child("price").getValue(String.class);
                                                            String menuName = dataSnapshot.child("foodName").getValue(String.class);
                                                            String menuUrl = dataSnapshot.child("menuImage").getValue(String.class);
                                                            String menustatus = dataSnapshot.child("statusMode").getValue(String.class);
                                                            String snapID=dataSnapshot.getKey().toString();

                                                            DatabaseReference dinnerRefsold = FirebaseDatabase.getInstance().getReference().child("Coupons")
                                                                    .child("Coupons Used")
                                                                    .child(dateOnly).child(menuName);
                                                            dinnerRefsold.addValueEventListener(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (snapshot.exists()) {
                                                                        String soldIdadi = snapshot.getValue(String.class);
                                                                        String[] sep = soldIdadi.split(" ");
                                                                        String idadi = sep[0];

                                                                        // Check if the menu item already exists in the list
                                                                        boolean found = false;
                                                                        for (FoodSetGetStaff item : foodListStaff) {
                                                                            if (item.getFoodName().equals(menuName)) {
                                                                                // Update the existing item
                                                                                item.setFoodPrice(menuPrice + " TZS");
                                                                                item.setFoodStatus(menustatus + "");
                                                                                item.setItemImage(menuUrl);
                                                                                item.setSoldNumber(idadi);
                                                                                found = true;
                                                                                break;
                                                                            }
                                                                        }

                                                                        // If the menu item is not found, add it to the list
                                                                        if (!found) {
                                                                            FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                                                            FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, idadi,snapID);
                                                                            foodList.add(foodSetGet);
                                                                            foodListStaff.add(foodSetGetStaff);
                                                                        }
                                                                    }else{
                                                                        FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuUrl,menustatus);
                                                                        FoodSetGetStaff foodSetGetStaff = new FoodSetGetStaff(menuPrice + " TZS", menuName, menustatus + "", menuUrl, "0",snapID);
                                                                        foodList.add(foodSetGet);
                                                                        foodListStaff.add(foodSetGetStaff);
                                                                    }
                                                                    adapter.updateData(foodList);
                                                                    adapterStaff.updateData(foodListStaff);
                                                                    adapterStaff.notifyDataSetChanged();
                                                                    Collections.reverse(foodList);
                                                                    Collections.reverse(foodListStaff);
                                                                    adapter.notifyDataSetChanged();
                                                                    progressBar.setVisibility(View.GONE);
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {

                                                                }
                                                            });
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        // Handle onCancelled event if needed
                                                    }
                                                });


                                            }
                                        });
//                                        Toast.makeText(DashBoard.this, foodSetGetStaff.getMenuID()+"", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(DashBoard.this, foodSetGetStaff.getMenuID()+"", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    });
                }
                // Use the checked text as needed
                // For example, you can show a Toast with the checked text
//                Toast.makeText(DashBoard.this, "Selected status: " + checkedText, Toast.LENGTH_SHORT).show();
            }
        });


        TextView food_name;
        TextView food_price;
        TextView food_status;
        ImageView foodPic;
        TextView soldCount;
        food_name=view.findViewById(R.id.fc_foodName);
        food_price = view.findViewById(R.id.fc_foodPrice);
        food_status = view.findViewById(R.id.fc_foodStatus);
        foodPic=view.findViewById(R.id.fc_foodImage);
        soldCount=view.findViewById(R.id.fc_soldAmount);
        ImageView cancel=view.findViewById(R.id.cancel_dialogue);

        food_name.setText(foodSetGetStaff.getFoodName());
        food_price.setText(foodSetGetStaff.getFoodPrice());
        food_status.setText(foodSetGetStaff.getFoodStatus());
        soldCount.setText(foodSetGetStaff.getSoldNumber());
        Glide.with(view.getContext())
                .load(foodSetGetStaff.getItemImage())
                .into(foodPic);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog3.dismiss();
            }
        });
        dialog3.setCancelable(false);

    }



    private void searchMenu(String query) {
        DatabaseReference menuRef = FirebaseDatabase.getInstance().getReference().child("MENUS");

        menuRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                foodList.clear();
                boolean foundMatch = false;

                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot menuItemSnapshot : mealSnapshot.getChildren()) {
                        // Retrieve data from Firebase
                        String menuName = menuItemSnapshot.child("foodName").getValue(String.class);
                        String menuPrice = menuItemSnapshot.child("price").getValue(String.class);
                        String menuImage = menuItemSnapshot.child("menuImage").getValue(String.class);
                        String menuStatus = menuItemSnapshot.child("statusMode").getValue(String.class);
                        String menuID = menuItemSnapshot.getKey();

                        // Check if menu name matches the query
                        if (menuName != null && menuName.toLowerCase().contains(query.toLowerCase())) {
                            foundMatch = true;
                            foodList.clear();
                            FoodSetGet foodSetGet = new FoodSetGet(menuPrice + " TZS", menuName, "VIP", menuImage,menuStatus);
                            foodList.add(foodSetGet);
                        }
                    }

                }

                if (modeController.equals("staff")){
                    // Update RecyclerView with search results
                    if (foodListStaff.isEmpty()) {
                        // No matching items found
                        adapterStaff.setClickable(false);
                        foodListStaff.clear();
                        showNoMatchingItemsMessage();
                        // Make adapter unclickable
                    } else {
                        adapterStaff.updateData(foodListStaff);
                        Collections.reverse(foodListStaff);
                        adapterStaff.setClickable(true); // Make adapter clickable
                        adapterStaff.notifyDataSetChanged();
                    }
                }else{
                    // Update RecyclerView with search results
                    if (foodList.isEmpty()) {
                        // No matching items found
                        adapter.setClickable(false);
                        foodList.clear();
                        showNoMatchingItemsMessage();
                        // Make adapter unclickable
                    } else {
                        adapter.updateData(foodList);
                        Collections.reverse(foodList);
                        adapter.setClickable(true); // Make adapter clickable
                        adapter.notifyDataSetChanged();
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }



    private void showNoMatchingItemsMessage() {

//        recyclerView.setVisibility(View.GONE);
        // Display a toast message indicating no matching items found
        Toast.makeText(DashBoard.this, "Item does not exist!", Toast.LENGTH_SHORT).show();
    }



}