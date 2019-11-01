# ScaleView
自定义的刻度尺选择的View，仿京东金融选择金钱刻度尺效果

![image](https://img-blog.csdnimg.cn/2019110116290463.gif)
##### 1.在XML中进行引用
```
<com.example.yanxu.scaleview.view.ScaleSelectView
        android:id="@+id/sv_scale_grade"
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:layout_marginTop="40dp"
        sv:isCanvasLine="true"
        sv:textDefaultSize="14sp"
        sv:unitLongLine="1"
        sv:unitSize="32dp" />
```
##### 2.在项目中的使用及选择结果的回调
```
    /**
     * 初始化data
     */
    private void initData() {
        mScaleView.setSelected(5, 7);
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
```
