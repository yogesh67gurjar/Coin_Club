package com.example.coinclubapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.coinclubapp.Adapters.TransactionAdapter;
import com.example.coinclubapp.InterFace.ApiInterface;
import com.example.coinclubapp.Response.ProfileResponse;
import com.example.coinclubapp.Response.TransactionHistory;
import com.example.coinclubapp.Retrofit.RetrofitService;
import com.example.coinclubapp.databinding.ActivityMyBankBinding;
import com.example.coinclubapp.result.Transc;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBankActivity extends AppCompatActivity {

    ActivityMyBankBinding binding;
    RecyclerView.LayoutManager LayoutManager;
    TransactionAdapter adapter;
    ApiInterface apiInterface;
    int Id;
    String walletAmount;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyBankBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final ProgressDialog progressDialog = new ProgressDialog(MyBankActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        apiInterface = RetrofitService.getRetrofit().create(ApiInterface.class);

        SharedPreferences sharedPreferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        Id = sharedPreferences.getInt("Id", 0);

        Call<ProfileResponse> call = apiInterface.getProfileItemById(Id);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful()) {

                    Call <List<TransactionHistory>> historyCall=apiInterface.getAllTransactionsHistory(Id);

                    historyCall.enqueue(new Callback<List<TransactionHistory>>() {
                        @Override
                        public void onResponse(Call<List<TransactionHistory>> call, Response<List<TransactionHistory>> response) {
                            if(response.isSuccessful())
                            {
                                TransactionHistory history=response.body().get(0);
                                List<Transc> transcsList=history.getTransc();

                                //                                allRecords.add();

                                binding.transactionRecyclerView.setVisibility(View.VISIBLE);
                                binding.noDataAvailableTv.setVisibility(View.GONE);
                                LayoutManager = new LinearLayoutManager(MyBankActivity.this, LinearLayoutManager.VERTICAL, false);
                                binding.transactionRecyclerView.setLayoutManager(LayoutManager);
                                adapter = new TransactionAdapter(MyBankActivity.this,transcsList);
                                binding.transactionRecyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                                adapter.getItemCount();
                                binding.transactionRecyclerView.setHasFixedSize(true);
                            }
                            else
                            {
                                binding.transactionRecyclerView.setVisibility(View.GONE);
                                binding.noDataAvailableTv.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<TransactionHistory>> call, Throwable t) {
                            binding.transactionRecyclerView.setVisibility(View.GONE);
                            binding.noDataAvailableTv.setVisibility(View.VISIBLE);
                        }
                    });

                    walletAmount=response.body().getWalletAmount();
                    binding.walletAmountTv.setText(walletAmount + " ₹");

                    progressDialog.dismiss();
                } else {
                    Toast.makeText(MyBankActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    Log.d("ERROR", response.message() + response.body());
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(MyBankActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("ERROR", t.getMessage() + t.hashCode());
            }
        });


        binding.backBtn.setOnClickListener(v -> {
            finish();
        });

        binding.addMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyBankActivity.this, AddMoneyActivity.class));
            }
        });

        binding.withdrawalMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyBankActivity.this, WithdrawActivity.class));
            }
        });
    }
}