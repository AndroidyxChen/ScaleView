package com.example.yanxu.scaleview.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.yanxu.scaleview.R;
import com.example.yanxu.scaleview.view.ScaleSelectView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ScaleSelectView mScaleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    /**
     * 初始化view
     */
    private void initView() {
        mScaleView = findViewById(R.id.sv_scale_grade);
    }

    /**
     * 初始化data
     */
    private void initData() {
        mScaleView.setSelected(5, 5);
        List<String> listGrade = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            listGrade.add(i + "");
        }
        mScaleView.showValue(listGrade, new ScaleSelectView.onSelect() {
            @Override
            public void onSelectItem(String value) {
                //TODO 这里回调的刻度选择的结果
            }
        });
    }

}
