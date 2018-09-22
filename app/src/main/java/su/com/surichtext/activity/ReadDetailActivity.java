package su.com.surichtext.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import su.com.richtext.SuRichText;
import su.com.surichtext.R;

public class ReadDetailActivity extends AppCompatActivity {

    SuRichText richText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_detail);

        try {
            richText = findViewById(R.id.richtext);
            richText.setContent(getIntent().getStringExtra("html"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        richText.onEnd();
    }
}
