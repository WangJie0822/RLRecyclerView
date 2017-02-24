package com.wj.rlrecyclerviewtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    public static final String LAYOUT_LINEAR = "linear";
    public static final String LAYOUT_GRID = "grid";
    public static final String LAYOUT_STAGGERED = "staggered";

    public static final String MODE_NONE = "none";
    public static final String MODE_REFRESH = "refresh";
    public static final String MODE_LOAD_MORE = "load_more";
    public static final String MODE_BOTH = "both";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main2Activity.actionStart(MainActivity.this, getMode(), getLayout(), getAuto(), haveHeader(), haveFooter());
            }
        });


    }

    private String getLayout() {

        String layout = "";

        RadioGroup rgLayout = (RadioGroup) findViewById(R.id.rg_layout);

        switch (rgLayout.getCheckedRadioButtonId()) {
            case R.id.rb_linear:
                layout = LAYOUT_LINEAR;
                break;
            case R.id.rb_grid:
                layout = LAYOUT_GRID;
                break;
            case R.id.rb_staggeredgrid:
                layout = LAYOUT_STAGGERED;
                break;
            default:
                break;
        }

        return layout;
    }

    private String getMode() {

        String mode = "";

        RadioGroup rgMode = (RadioGroup) findViewById(R.id.rg_mode);

        switch (rgMode.getCheckedRadioButtonId()) {
            case R.id.rb_none:
                mode = MODE_NONE;
                break;
            case R.id.rb_refresh:
                mode = MODE_REFRESH;
                break;
            case R.id.rb_load_more:
                mode = MODE_LOAD_MORE;
                break;
            case R.id.rb_both:
                mode = MODE_BOTH;
                break;
            default:
                break;
        }

        return mode;
    }

    private boolean getAuto() {

        boolean auto = false;

        RadioGroup rgAuto = (RadioGroup) findViewById(R.id.rg_auto);

        switch (rgAuto.getCheckedRadioButtonId()) {
            case R.id.rb_on:
                auto = true;
                break;
            case R.id.rb_off:
                auto = false;
                break;
            default:
                break;
        }

        return auto;
    }

    private boolean haveHeader() {

        boolean header = false;

        RadioGroup rgHeader = (RadioGroup) findViewById(R.id.rg_header);

        switch (rgHeader.getCheckedRadioButtonId()) {
            case R.id.rb_header_on:
                header = true;
                break;
            case R.id.rb_header_off:
                header = false;
                break;
            default:
                break;
        }

        return header;
    }

    private boolean haveFooter() {

        boolean footer = false;

        RadioGroup rgFooter = (RadioGroup) findViewById(R.id.rg_footer);

        switch (rgFooter.getCheckedRadioButtonId()) {
            case R.id.rb_footer_on:
                footer = true;
                break;
            case R.id.rb_footer_off:
                footer = false;
                break;
            default:
                break;
        }

        return footer;
    }
}
