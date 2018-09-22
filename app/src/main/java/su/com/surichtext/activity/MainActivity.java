package su.com.surichtext.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.bmob.v3.Bmob;
import su.com.richtext.activity.SuSelectImageActivity;
import su.com.surichtext.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bmob.initialize(this,"b252341bf26b9ac737d8e46cfa5ef66b");

    }

    public void write(View view){
        startActivity(new Intent(this,WriteActivity.class));
    }

    public void read(View view){
        startActivity(new Intent(this,ReadActivity.class));
    }
}
