package edgar.com.preapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.edgar.cjbutil.ui.BaseActivity;

import butterknife.Bind;
import butterknife.OnClick;
import edgar.com.preapp.R;
import edgar.com.preapp.service.FloatWindowService;

public class MainActivity extends BaseActivity {

    @Bind(R.id.btnTest)
    Button btnTest;
    @Bind(R.id.btnList)
    Button btnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @OnClick({R.id.btnTest, R.id.btnList})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnTest:
                Intent intent1 = new Intent(this, FloatWindowService.class);
                startService(intent1);
                finish();
                break;
            case R.id.btnList:
                Intent intent = new Intent(this,APPList.class);
                startActivity(intent);
                break;
        }
    }
}
